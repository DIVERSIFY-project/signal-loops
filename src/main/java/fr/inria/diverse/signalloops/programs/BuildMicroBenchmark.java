package fr.inria.diverse.signalloops.programs;

import fr.inria.diverse.signalloops.codegenerators.MainClassGenerator;
import fr.inria.diverse.signalloops.codegenerators.MicrobenchmarkGenerator;
import fr.inria.diverse.signalloops.codegenerators.TestForMicrobenchmarkGenerator;
import fr.inria.diverse.signalloops.detectors.LoopInputsDetector;
import fr.inria.diverse.signalloops.loggers.LightLog;
import fr.inria.diverse.signalloops.loggers.buildMicrobenchmark.Log;
import fr.inria.diverse.signalloops.loggers.buildMicrobenchmark.MicrobenchmarkLogger;
import fr.inria.diverse.signalloops.loggers.buildMicrobenchmark.ShutdownHookLog;
import fr.inria.diverse.signalloops.model.SignalLoop;
import fr.inria.diversify.syringe.Configuration;
import fr.inria.diversify.syringe.SyringeInstrumenter;
import fr.inria.diversify.syringe.SyringeInstrumenterImpl;
import fr.inria.diversify.syringe.injectors.GenericInjector;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Generates a set of benchmarks out of several loops
 * <p/>
 * Created by marodrig on 15/09/2015.
 */
public class BuildMicroBenchmark {

    private static Logger log = Logger.getLogger(BuildMicroBenchmark.class.getCanonicalName());

    public static void main(String[] args) throws Exception {
        Properties p = new Properties();
        p.load(new FileInputStream(BuildMicroBenchmark.class.getResource("/loop_perforation/jsyn.properties").toURI().getPath()));
        new BuildMicroBenchmark().execute(p);
    }

    public void execute(Properties p) throws Exception {
        String prj = p.getProperty("project.dir");//PROJECT_DIR;
        String prjSrc = p.getProperty("src.dir");//SRC_DIR + "/java";
        //String testSrc = p.getProperty("test.dir");//TEST_DIR + "/java";
        String instrumentedPrj = p.getProperty("out.dir");//TEST_DIR + "/java";
        boolean onlyCopyLogger = Boolean.parseBoolean(p.getProperty("only.copy.logger"));


        LoopInputsDetector inputs = new LoopInputsDetector();
        Configuration confSrc = new Configuration(prjSrc);
        confSrc.addDetector(inputs);
        //confSrc.addDetector(new MethodDetect());
        String injection = Log.class.getCanonicalName() + ".log%type%(%var%, \"%name%\", false);\n";
        confSrc.addInjector(LoopInputsDetector.INPUTS, new GenericInjector(injection));
        //injection = Log.class.getCanonicalName() + ".log%type%(%var%, \"%name%_after\", true);\n";
        //confSrc.addInjector(LoopInputsDetector.AFTER_INPUTS, new GenericInjector(injection));
        injection = Log.class.getCanonicalName() + ".close();\n";
        confSrc.addInjector(LoopInputsDetector.END_INPUTS, new GenericInjector(injection));

        confSrc.addLogger(MicrobenchmarkLogger.class);
        confSrc.addLogger(Log.class);
        confSrc.addLogger(ShutdownHookLog.class);
        confSrc.addLogger(LightLog.class);

        //Instrument
        SyringeInstrumenter l = new SyringeInstrumenterImpl(prj, prjSrc, instrumentedPrj);
        l.instrument(confSrc);
        l.setOnlyCopyLogger(onlyCopyLogger);
        //Run tests in order to make the magic happens!
        //l.runTests();
        l.writeIdFile("microbenchmarkProperties.id");

        log.info("Elements detected: " + inputs.getElementsDetected());

        log.info("***********************");
        log.info("Writing microbenchmarks");
        log.info("***********************");

        String generationOutputPath = "C:\\MarcelStuff\\PROJECTS\\preforation-benchmark\\src\\main\\java\\fr\\inria\\diverse\\perfbench";
        String generationOutputTestPath = "C:\\MarcelStuff\\PROJECTS\\preforation-benchmark\\src\\test\\java\\fr\\inria\\diverse\\perfbench";
        String dataInputPath = "C:\\MarcelStuff\\DATA\\DIVERSE\\logs\\input-data";
        String databaseOutputPath = "C:\\MarcelStuff\\DATA\\DIVERSE\\PREFORATION\\perforationresults.s3db";
        String packageName = "fr.inria.diverse.perfbench";
        generateMicrobenchmarks(packageName, generationOutputPath,
                generationOutputTestPath, dataInputPath, databaseOutputPath, inputs.getResults());
    }

    public void generateMicrobenchmarks(String packageName, String generationOutputPath,
                                        String generationOutputTestPath, String datainputPath,
                                        String databaseOutputPath,
                                        Collection<SignalLoop> loops) throws FileNotFoundException {
        try {
            String templatePath = Thread.currentThread().getContextClassLoader().getResource("templates").toURI().getPath();

            MicrobenchmarkGenerator benchmarkGen = new MicrobenchmarkGenerator();
            MainClassGenerator mainGen = new MainClassGenerator();
            TestForMicrobenchmarkGenerator testGen = new TestForMicrobenchmarkGenerator();

            log.info("Initializing templates");

            mainGen.initialize(templatePath);
            benchmarkGen.initialize(templatePath);
            testGen.initialize(templatePath);

            log.info("Building benchmarks");

            for (SignalLoop loop : loops) {
                benchmarkGen.generate(packageName, generationOutputPath, datainputPath, loop, false);
                benchmarkGen.generate(packageName, generationOutputPath, datainputPath, loop, true);
                testGen.generate(packageName, generationOutputTestPath, datainputPath, loop);
            }

            log.info("Building main files");

            //Generate main
            mainGen.generate(packageName, generationOutputPath, datainputPath, databaseOutputPath, loops);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
