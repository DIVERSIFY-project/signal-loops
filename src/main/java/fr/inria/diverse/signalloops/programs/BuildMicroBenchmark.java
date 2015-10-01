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
        String injection = Log.class.getCanonicalName() + ".log%type%(%var%, %name%);\n";
        confSrc.addInjector(LoopInputsDetector.INPUTS, new GenericInjector(injection));
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

    public void generateMicrobenchmarkAndTest(String packageName, String generationOutputPath,
                                              String generationOutputTestPath, String dataOutputPath,
                                              SignalLoop signalLoop) throws FileNotFoundException {
        generateBenchMark(packageName, generationOutputPath, dataOutputPath, signalLoop, false);
        generateBenchMark(packageName, generationOutputPath, dataOutputPath, signalLoop, true);
        generateUnitTests(generationOutputTestPath, dataOutputPath, signalLoop);
    }

    private boolean existsDataFile(String dataPath, final String className) {
        final String fileName = className.replace("_", "-");
        return new File(dataPath).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(fileName);
            }
        }).length > 0;
    }

    private void generateUnitTests(String generationOutputTestPath, String dataPath, SignalLoop loop) throws FileNotFoundException {
        final String className = getInputClassName(loop);
        if (!(existsDataFile(dataPath, className))) return;
        StringBuilder sb = new StringBuilder();
        sb.append("package fr.inria.diverse.perfbench; \n\n");
        sb.append("import org.junit.Test;\n\n");

        sb.append("public class ").append(className).append("Test {\n\n");
        sb.append(pad(4)).append("@Test \n");
        sb.append(pad(4)).append("public void ").append("testOriginal() {\n");
        sb.append(pad(8)).append(className).append(" benchmark = new ").append(className).append("(); \n");
        sb.append(pad(8)).append("benchmark.setup();\n");
        sb.append(pad(8)).append("benchmark.").append(className).append("_ORIGINAL();\n");
        sb.append(pad(4)).append("}\n\n");

        sb.append(pad(4)).append("@Test \n");
        sb.append(pad(4)).append("public void ").append("testGracefully() {\n");
        sb.append(pad(8)).append(className).append("Gracefully").append(" benchmark = new ").append(className).append("Gracefully(); \n");
        sb.append(pad(8)).append("benchmark.setup();\n");
        sb.append(pad(8)).append("benchmark.").append(className).append("_GRACEFULLY();\n");
        sb.append(pad(4)).append("}\n}\n");

        PrintWriter out = new PrintWriter(generationOutputTestPath + "/" + className + "Test.java");
        out.write(sb.toString());
        out.close();
    }

    /*
     * Get the name of the file for the loop input
       @para  input
     * @return
     */
    private String getInputClassName(SignalLoop loop) {
        return loop.getLoop().getPosition().
                getCompilationUnit().getMainType().getQualifiedName().replace(".", "_") +
                "_" + loop.getLoop().getPosition().getLine();
    }

    /**
     * Creates the main class file to launch the microbenchmark set
     */
    public void generateMainClass(String packageName, String generationOutputPath, String dataPath,
                                  Collection<SignalLoop> loops) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import org.openjdk.jmh.runner.Runner;\n");
        sb.append("import org.openjdk.jmh.runner.RunnerException;\n");
        sb.append("import org.openjdk.jmh.runner.options.Options;\n");
        sb.append("import org.openjdk.jmh.runner.options.OptionsBuilder;\n");

        sb.append("public class Main {");
        sb.append(pad(4)).append("public static void main(String[] args) throws RunnerException {\n");
        sb.append(pad(4)).append("Options opt = new OptionsBuilder()\n");
        for (SignalLoop a : loops) {
            String className = getInputClassName(a);
            if (existsDataFile(dataPath, className)) {
                sb.append(pad(8)).append(".include(").append(className).append(".class.getSimpleName())\n");
                sb.append(pad(8)).append(".include(").append(className).append("Gracefully").append(".class.getSimpleName())\n");
            }
        }
        sb.append(pad(8)).append(".warmupIterations(5)\n");
        sb.append(pad(8)).append(".measurementIterations(5)\n");
        sb.append(pad(8)).append(".forks(1)\n");
        sb.append(pad(8)).append(".shouldFailOnError(true)\n");
        sb.append(pad(8)).append(".build();\n");
        sb.append(pad(4)).append("new Runner(opt).run();");
        sb.append(pad(4)).append("}\n}");

        PrintWriter out = new PrintWriter(generationOutputPath + "/Main.java");
        out.write(sb.toString());
        out.close();
                    /*
                .include(BrownNoiseBenchmark.class.getSimpleName())
                .include(EdgeDetectorBenchmark.class.getSimpleName())
                .include(FilterFourPolesBenchmark.class.getSimpleName())
                .include(FilterOneZeroBenchmark.class.getSimpleName())
                .include(FourWayFadeBenchmark.class.getSimpleName())
                .include(FunctionOscillatorBenchmark.class.getSimpleName())
                //.include(GrainFarmBenchmark.class.getSimpleName())
                .include(IntegrateBenchmark.class.getSimpleName())
                .include(LatchZeroCrossingBenchmark.class.getSimpleName())
                .include(PeakFollowerBenchmark.class.getSimpleName())
                .include(PulseOscillatorBenchmark.class.getSimpleName())
                .include(SawtoothOscillatorDPWBenchmark.class.getSimpleName())
                .include(SchmidtTriggerBenchmark.class.getSimpleName())
                .include(SineOscillatorPhaseModulatedBenchmark.class.getSimpleName())
                .include(SquareOscillatorBenchmark.class.getSimpleName())
                .include(MixerStereoBenchmark.class.getSimpleName())
                .include(MixerStereoRampedBenchmark.class.getSimpleName())*/
    }

    /**
     * Generate a bechmark class out of a loop
     *
     * @param packageName
     * @param generationPath
     * @param dataPath
     * @throws java.io.FileNotFoundException
     */
    public void generateBenchMark(String packageName, String generationPath,
                                  String dataPath, SignalLoop loop, boolean degraded) throws FileNotFoundException {

        String className = getInputClassName(loop);
        if (!existsDataFile(dataPath, className)) return;

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import org.openjdk.jmh.annotations.*;\n")
                .append("import java.io.DataInputStream;\n\n");

        sb.append("// Microbenchmark for loop: ").append(loop.getId()).append("\n");

        sb.append("@State(Scope.Thread)\n");
        sb.append("public class ").append(className);
        if (degraded) sb.append("Gracefully");
        sb.append(" {\n\n");
        for (CtVariableAccess a : loop.getAccesses()) {
            sb.append(pad(4)).append("public ").append(a.getVariable().getType().toString()).append(" ").append(accessPrettyPrint(a)).append(";\n");
        }

        //Create the setup method
        sb.append("\n").append(pad(4)).append("@Setup(Level.Invocation)\n");
        sb.append(pad(4)).append("public void setup() {\n");
        sb.append(pad(8)).append("try {\n");
        //Print the path to the input file
        sb.append(pad(12)).append("String input_file = \"").append(dataPath.replace("\\", "/")).append("\";\n");
        sb.append(pad(12)).append("DataInputStream s = Loader.getStream(input_file, \"").append(className.replace("_", "-")).append("\");\n");

        //Initialize variables
        for (CtVariableAccess a : loop.getAccesses()) {
            //If the variable is not initialized in the original code, then don't initialize
            if (!loop.getInitialized().contains(a)) continue;
            if (a.getVariable().getType() instanceof CtArrayTypeReference) {
                CtArrayTypeReference ref = (CtArrayTypeReference) a.getVariable().getType();
                sb.append(pad(12)).append(accessPrettyPrint(a)).append(" = Loader.readArray").append(ref.getComponentType().toString());
            } else
                sb.append(pad(12)).append(accessPrettyPrint(a)).append(" = Loader.read").append(a.getType().toString());
            sb.append("(s);\n");
        }
        sb.append(pad(12)).append("s.close();\n");
        sb.append(pad(8)).append("} catch(Exception e) { throw new RuntimeException(e); }\n");
        sb.append(pad(4)).append("}\n\n");

        //Print the static method
        extractStaticMethod(sb, loop.getLoop());

        //Create the benchmark method
        sb.append(pad(4)).append("@Benchmark\n");
        if (degraded) sb.append(pad(4)).append("public void ").append(className).append("_GRACEFULLY() {\n");
        else sb.append(pad(4)).append("public void ").append(className).append("_ORIGINAL() {\n");

        String loopStr = getLoopPrettyPrint(loop, degraded);
        sb.append(loopStr);

        //log.info(sb.toString());
        //log.info("-----------");

        String path = degraded ? generationPath + "/" + className + "Gracefully.java" :
                generationPath + "/" + className + ".java";
        PrintWriter out = new PrintWriter(path);
        out.write(sb.toString());
        out.close();
    }

    /**
     * Extract private static method out of an statement and copy its body to the microbenchmark
     *
     * @param sb        Output string builder that will contain the body of the method
     * @param statement Statement containing the method invocations.
     */
    private void extractStaticMethod(StringBuilder sb, CtStatement statement) {
        //Append all static methods
        List<CtInvocation> methods = statement.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));
        for (CtInvocation inv : methods) {
            //Find static and private methods
            if (inv.getExecutable().isStatic() &&
                    inv.getExecutable().getDeclaration() != null &&
                    !inv.getExecutable().getDeclaration().getModifiers().contains(ModifierKind.PUBLIC)) {

                //Prety print the static declaration of the method with a different name
                CtExecutable ref = inv.getExecutable().getDeclaration();
                sb.append(pad(4)).append("static ").
                        append(ref.getType().getQualifiedName()).
                        append(" ").
                        append(ref.getDeclaringType().getQualifiedName().replace(".", "_").replace("$", "_")).
                        append("_").
                        append(ref.getSimpleName()).
                        append("(");

                //Print parameters of the method
                CtParameter p = (CtParameter) ref.getParameters().get(0);
                sb.append(p.getType().getQualifiedName()).append(" ").append(p.getSimpleName());
                for (int i = 1; i < ref.getParameters().size(); i++) {
                    p = (CtParameter) ref.getParameters().get(i);
                    sb.append(", ").append(p.getType().getQualifiedName()).append(" ").append(p.getSimpleName());
                }
                sb.append(")");
                //Print the body
                String decStr = ref.getBody().toString();
                decStr = decStr.replace("\r\n", "\r\n" + pad(4));
                sb.append(pad(4)).append(decStr);
                sb.append("\n\n");
                List<CtInvocation> deepMethods =
                        inv.getExecutable().getDeclaration().getBody().getElements(
                                new TypeFilter<CtInvocation>(CtInvocation.class));
                //Recursively add other static methods
                for (CtStatement m : deepMethods) {
                    extractStaticMethod(sb, m);
                }
            }
        }
    }

    /**
     * Pretty print the loop, basically substitute static private method for our public of the methods
     * and static fields for our public copy of the static field
     *
     * @param loop
     * @param degraded
     * @return
     */
    private String getLoopPrettyPrint(SignalLoop loop, boolean degraded) {

        String loopStr = loop.getLoop().toString();
        loopStr = loopStr.replace("\r\n", "\r\n" + pad(8));
        StringBuilder sb = new StringBuilder();
        if (degraded) {
            //TODO: Have into consideration the case where there is only one line (no final })
            sb.append(pad(8)).append(loopStr.substring(0, loopStr.length() - 1)).append("\n");//eliminate last "}"
            sb.append(pad(8)).append(loop.getDegradedSnippet()).append("}\n");
        } else sb.append(pad(8)).append(loopStr).append("\n");
        sb.append(pad(4)).append("}\n}\n");

        loopStr = sb.toString();

        for (CtVariableAccess a : loop.getAccesses()) {
            if (a instanceof CtFieldAccess) {
                CtFieldAccess f = (CtFieldAccess) a;
                if (f.getVariable().isStatic() && loop.getInitialized().contains(f)) {
                    loopStr = loopStr.replace(f.toString(), accessPrettyPrint(f));
                }
            }
        }
        for (CtInvocation inv : loop.getLoop().getElements(new TypeFilter<CtInvocation>(CtInvocation.class))) {
            if (inv.getExecutable().isStatic() && inv.getExecutable().getDeclaration() != null) {
                String invStr = inv.toString();
                invStr = invStr.substring(0, invStr.lastIndexOf("(") - 1);
                loopStr = loopStr.replace(invStr, invStr.replace(".", "_"));
            }
        }

        return loopStr;
    }

    /**
     * Preety print a variable acces
     *
     * @param access
     * @return
     */
    private String accessPrettyPrint(CtVariableAccess access) {
        return LoopInputsDetector.getCompilableName(access).replace(".", "_");
    }

    private String pad(int p) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p; i++) sb.append(" ");
        return sb.toString();
    }


}
