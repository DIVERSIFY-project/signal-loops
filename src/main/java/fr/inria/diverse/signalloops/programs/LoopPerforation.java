package fr.inria.diverse.signalloops.programs;

import fr.inria.diverse.signalloops.detectors.SignalLoopDetector;
import fr.inria.diverse.signalloops.loggers.LightLog;
import fr.inria.diverse.signalloops.loggers.loopPerforation.Log;
import fr.inria.diverse.signalloops.loggers.loopPerforation.LoopPerforationLogger;
import fr.inria.diverse.signalloops.loggers.loopPerforation.ShutdownHookLog;
import fr.inria.diverse.signalloops.model.SQLLiteConnection;
import fr.inria.diverse.signalloops.model.SignalLoop;
import fr.inria.diversify.syringe.Configuration;
import fr.inria.diversify.syringe.SyringeInstrumenter;
import fr.inria.diversify.syringe.SyringeInstrumenterImpl;
import fr.inria.diversify.syringe.injectors.GenericInjectWithId;
import fr.inria.diversify.syringe.injectors.GenericInjector;
import fr.inria.juncoprovider.XMLCoverageFinder;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * An instrumenter to perforate the loop
 * <p/>
 * Created by marodrig on 10/07/2015.
 */
public class LoopPerforation {


    SignalLoopDetector signalDetector;

    ArrayList<SignalLoop> signalLoops = new ArrayList<SignalLoop>();

    public static String ID_FILE_NAME = "loopPerforation.id";

    /**
     * Configures the instrumentation
     *
     * @param prjSrc         Source where is the project
     * @param loopId         ID of the loop we want to instrument
     * @param registerOutput Indicates if we want to trace the array output
     * @param perforation    Indicates if we want degrade the loop
     * @return A configuration for the instrumenter
     * @throws java.io.FileNotFoundException
     */
    private Configuration configureInstrumenter(String prjSrc, int loopId,
                                                boolean registerOutput,
                                                boolean registerSpeed,
                                                boolean perforation,
                                                SignalLoopDetector signalLoop)
            throws FileNotFoundException {
        Configuration confSrc = new Configuration(prjSrc);
        //Logger classes
        confSrc.addLogger(LoopPerforationLogger.class);
        confSrc.addLogger(Log.class);
        confSrc.addLogger(ShutdownHookLog.class);
        confSrc.addLogger(LightLog.class);

        //Detections
        signalLoop.setDegrade(perforation);
        signalLoop.setPrepareMicroBenchMark(registerSpeed);
        signalLoop.setLoopId(loopId);
        confSrc.addDetector(signalLoop);

        String injection;
        if (registerSpeed || registerOutput) {
            injection = Log.class.getCanonicalName() + ".loopBefore";
            confSrc.addInjector(SignalLoopDetector.BEFORE_KEY, new GenericInjectWithId(injection));
            confSrc.addInjector(SignalLoopDetector.SIGNAL_BEFORE_KEY, new GenericInjectWithId(injection));
            injection = Log.class.getCanonicalName() + ".loopAfter";
            confSrc.addInjector(SignalLoopDetector.AFTER_KEY, new GenericInjectWithId(injection));
            confSrc.addInjector(SignalLoopDetector.SIGNAL_AFTER_KEY, new GenericInjectWithId(injection));
        }

        if (registerOutput) {
            injection = ";" + Log.class.getCanonicalName() + ".arrayAccess";
            confSrc.addInjector(SignalLoopDetector.ACCESS_KEY, new GenericInjectWithId(injection, "%array_access%"));
        }

        if (perforation) {
            injection = ";%loop_increment%;" +
                    "if ( %loop_condition% ) { " +
                    "%recursive_up%;  \n" +
                    "%array%[%index_expr%] = %array%[(%index_expr%)-1];";
            if (registerOutput) {
                injection += ";" + Log.class.getCanonicalName() + ".arrayAccess(%elementId%, %array%[%index_expr%]); ";
            }
            injection += "%recursive_down%}";
            confSrc.addInjector(SignalLoopDetector.END_KEY, new GenericInjector(injection));
        }
        return confSrc;
    }

    private void buildTheProgram(SyringeInstrumenter l, int loopId, String purpose,
                                 String outPrjSrc, String loopPosition, SignalLoop loop) throws IOException {
        //Run tests in order to make the magic happens!
        //Write the properties
        System.out.println("+ + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + ");
        System.out.println("B U I L D :");
        System.out.println("Building the program for *" + purpose.toUpperCase() + "* measurements ");
        System.out.println("Loop " + loopId + " modified ");
        System.out.println("+ + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + ");


        Properties props = new Properties();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "EN"));
        if (loopId != -1) {
            props.put("log.dir", "log/" + purpose + "_" + loopId + "_" +
                    df.format(new Date()).replace('/', '-').replace(':', '-').replace(' ', '_'));
        } else {
            props.put("log.dir", "log/" + purpose + "_all_" +
                    df.format(new Date()).replace('/', '-').replace(':', '-').replace(' ', '_'));
        }
        props.put("purpose", purpose);
        props.put("logarithmic.spacing", "false");
        l.writeLoggerProperties("perforation.properties", props);
        //Write the IDs file
        l.writeIdFile(ID_FILE_NAME);

        try {
            JSONObject ob = new JSONObject();
            ob.put("Position", loopPosition);
            PrintWriter out = new PrintWriter(outPrjSrc + "/transplant.json");
            out.write(ob.toString());
            out.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean testFails = true;
        try {
            l.setBuildTimeOut(500);
            l.runTests(true, new String[]{"test"});
            testFails = false;
        } catch (RuntimeException e) {
            System.out.println("[ERROR] Run test: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (loop != null) loop.setTestFails(testFails);

    }

    private void saveLoopToDB(SignalLoop loop, String path) {
        //Save the loop to DB
        if (loop != null) {
            try {
                SQLLiteConnection c = new SQLLiteConnection(path);
                c.insert(loop);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Instrument only one loop in the code and run tests to compare
     *
     * @param loopId         The ID of the loop to be instrumented. There is no way of knowing beforehand the ID of a particular
     *                       loop. However, the ID remains the same (unless the instrumented program changes) between runs
     *                       of the instrumenter. That way, we may run each loop separately
     * @param properties     Properties of the project
     * @param registerOutput Indicates if should register the outputs
     * @param perforate      Indicates if we should perforate the loop
     * @return
     * @throws Exception
     */
    private Boolean instrumentAndRun(final int loopId, Properties properties,
                                     boolean registerOutput, boolean registerSpeed,
                                     boolean perforate, boolean build) throws Exception {
        Boolean loopFound = false;
        try {
            String prj = properties.getProperty("project.dir");//PROJECT_DIR;
            String prjSrc = properties.getProperty("src.dir");//SRC_DIR + "/java";
            String resDir = properties.getProperty("res.dir");//SRC_DIR + "/java";
            final String instrumentedPrj = properties.getProperty("out.dir");//TEST_DIR + "/java";
            String dbPath = properties.getProperty("loopDB");//TEST_DIR + "/java";
            String coverage = properties.getProperty("coverage");//TEST_DIR + "/java";

            //Configure instrumentation
            signalDetector = new SignalLoopDetector();
            signalDetector.setPrepareMicroBenchMark(true);
            signalDetector.setProcessAllLoops(loopId == -1);
            Configuration confSrc = configureInstrumenter(prjSrc, loopId, registerOutput,
                    registerSpeed, perforate, signalDetector);

            //Perform the instrumentation
            final SyringeInstrumenter l = new SyringeInstrumenterImpl(prj, prjSrc, instrumentedPrj);
            l.setOnlyCopyLogger(false);
            l.instrument(confSrc);

            //The detector is programed to process only one loop at the time.
            //Here we indicate whether more loops remains to be instrumented
            loopFound = signalDetector.getMoreLoopsRemain();

            //
            final String loopPosition = signalDetector.getLoopPosition();

            //If no signal loop where found, there is no need to build the program, its very expensive
            if (signalDetector.getSignalElementsDetected() <= 0) return loopFound;
            signalLoops.add(signalDetector.getLastLoop());


            System.out.println("Number of signal loops: " + signalDetector.getSignalElementsDetected());
            System.out.println("Number of loops: " + signalDetector.getElementsDetected());
            final String intention = registerOutput ? "accuracy" : "speed";


            if ( registerSpeed && perforate ) {
                //Build the microbenchmars
                //TODO: Instrument the input values and execute the code!!!!!
                //TODO: option this
                if ( loopId == -1 ) {
                    BuildMicroBenchmark benchmark = new BuildMicroBenchmark();
                    String generationOutputPath = "C:\\MarcelStuff\\PROJECTS\\preforation-benchmark\\src\\main\\java\\fr\\inria\\diverse\\perfbench";
                    String generationOutputTestPath = "C:\\MarcelStuff\\PROJECTS\\preforation-benchmark\\src\\test\\java\\fr\\inria\\diverse\\perfbench";
                    String dataOutputPath = "C:\\MarcelStuff\\DATA\\DIVERSE\\logs\\input-data";
                    String packageName = "fr.inria.diverse.perfbench";
                    benchmark.generateMicrobenchmarks(packageName, generationOutputPath,
                            generationOutputTestPath, dataOutputPath, signalDetector.getSignalLoops().values());
                    //Keep generating progressively
                    //benchmark.generateMainClass(packageName, generationOutputPath, dataOutputPath, signalLoops);
                } else {
                    BuildMicroBenchmark benchmark = new BuildMicroBenchmark();
                    String generationOutputPath = "C:\\MarcelStuff\\PROJECTS\\preforation-benchmark\\src\\main\\java\\fr\\inria\\diverse\\perfbench";
                    String generationOutputTestPath = "C:\\MarcelStuff\\PROJECTS\\preforation-benchmark\\src\\test\\java\\fr\\inria\\diverse\\perfbench";
                    String dataOutputPath = "C:\\MarcelStuff\\DATA\\DIVERSE\\logs\\input-data";
                    String packageName = "fr.inria.diverse.perfbench";
                    benchmark.generateMicrobenchmarkAndTest(packageName, generationOutputPath,
                            generationOutputTestPath, dataOutputPath, signalDetector.getLastLoop());
                    //Keep generating progressively
                    benchmark.generateMainClass(packageName, generationOutputPath, dataOutputPath, signalLoops);
                }
            }

            if (!build) return loopFound;

            //BUILD with time out:
            if (loopId != -1 && signalDetector.getLastLoop() != null) {
                String className = signalDetector.getLastLoop().getPosition().split(":")[0];
                signalDetector.getLastLoop().setNbTestCover(getNbTestCovering(coverage, className));
                if (signalDetector.getLastLoop().getNbTestCover() > 0) {
                    Callable<SignalLoop> buildCall = new Callable<SignalLoop>() {
                        @Override
                        public SignalLoop call() throws Exception {
                            buildTheProgram(l, loopId, intention, instrumentedPrj, loopPosition, signalDetector.getLastLoop());
                            return null;
                        }
                    };

                    final ExecutorService service = Executors.newSingleThreadExecutor();
                    try {
                        final Future<SignalLoop> f = service.submit(buildCall);
                        f.get(300, TimeUnit.SECONDS);
                    } catch (final TimeoutException ex) {
                        signalDetector.getLastLoop().setTestTimeOut(true);
                        signalDetector.getLastLoop().setTestFails(true);
                        System.out.println("*********************************************");
                        System.out.println("TIME OUT: " + loopId);
                        System.out.println("*********************************************");
                    } finally {
                        service.shutdown();
                    }
                }
            } else buildTheProgram(l, loopId, intention, instrumentedPrj, "", signalDetector.getLastLoop());

            //Saving the Loop to the DB
            saveLoopToDB(signalDetector.getLastLoop(), dbPath);

        } catch (Exception e) {
            e.printStackTrace();
            loopFound = false;
        }

        return loopFound;
    }

    private int getNbTestCovering(String coverage, String className) throws ParserConfigurationException, SAXException, IOException {
        int testCovered = 0;
        XMLCoverageFinder finder = new XMLCoverageFinder();
        for (File f : new File(coverage).listFiles()) {
            if (f.getName().toLowerCase().endsWith("xml")) {
                if (finder.isCovered(f.getAbsolutePath(), className)) testCovered++;
            }
        }
        System.out.println("--------------------------------------");
        System.out.println("[ INFO ] COVERAGE:  " + testCovered);
        System.out.println("--------------------------------------");
        return testCovered;
    }

    private void execute(String[] args, boolean all) throws Exception {

        //Obtaining parameters from the property file
        Properties properties = new Properties();
        properties.load(new FileInputStream(
                LoopPerforation.class.getResource("/loop_perforation/" + args[0]).toURI().getPath()));

        //Boolean loopFound =
                instrumentAndRun(-1, properties, DONT_MEASURE_ACCURACY, MEASURE_TIME, PERFORATE, false); // Measure time
        /*
        int i = 0;
        while (loopFound) {
            i++;
            System.out.println("--------------------------------------");
            System.out.println(" N E X T  L O O P: " + i);
            System.out.println("--------------------------------------");
            loopFound = instrumentAndRun(i, properties, MEASURE_ACCURACY, DONT_MEASURE_TIME, DONT_PERFORATE, true); // Measure Accuracy 1
            if (signalDetector.getSignalElementsDetected() > 0) {
                instrumentAndRun(i, properties, MEASURE_ACCURACY, DONT_MEASURE_TIME, DONT_PERFORATE, true); // Measure Accuracy 2
                //Measure the accuracy of the degraded
                instrumentAndRun(i, properties, MEASURE_ACCURACY, DONT_MEASURE_TIME, PERFORATE, true); // Measure Accuracy 3
            }
        }*/
    }

    public static void main(String[] args) throws Exception {
        //new LoopPerforation().accuracyExtended(new String[]{"jsyn.properties"}, 0);
        new LoopPerforation().execute(new String[]{"jsyn.properties"}, true);
        //new LoopPerforation().sandbox(args, 46);
    }


    private static final boolean DONT_MEASURE_TIME = false;
    private static final boolean MEASURE_TIME = true;
    private static final boolean DONT_MEASURE_ACCURACY = false;
    private static final boolean MEASURE_ACCURACY = true;
    private static final boolean DONT_PERFORATE = false;
    private static final boolean PERFORATE = true;

    private void sandbox(String[] args, int loopId) throws Exception {
        //Obtaining parameters from the property file
        Properties properties = new Properties();
        properties.load(new FileInputStream(LoopPerforation.class.getResource("/" + args[0]).toURI().getPath()));
        //instrumentAndRun(loopId, properties, DONT_MEASURE_ACCURACY, DONT_MEASURE_TIME, PERFORATE); // Measure time// Measure time
        instrumentAndRun(loopId, properties, MEASURE_ACCURACY, DONT_MEASURE_TIME, PERFORATE, true); // Measure time// Measure time
    }

}
