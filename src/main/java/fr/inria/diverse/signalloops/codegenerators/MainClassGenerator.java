package fr.inria.diverse.signalloops.codegenerators;

import fr.inria.diverse.signalloops.model.SignalLoop;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by marodrig on 29/09/2015.
 */
public class MainClassGenerator extends BenchmarkGenerator {

    /**
     * Generates the main method to execute all benchmarks
     * @param packageName Name of the generated package
     * @param generationOutputPath Path where the generated file is going to be stored
     * @param inputDataPath Path of the input data
     * @param outputDBPath Path of the output data
     * @param loops Loops to generate
     */
    public void generate(String packageName, String generationOutputPath, String inputDataPath, String outputDBPath,
                         Collection<SignalLoop> loops) {
        HashMap<String, Object> input = new HashMap<String, Object>();
        input.put("package_name", packageName);
        input.put("signal_loops", loops);
        input.put("data_path", inputDataPath);
        input.put("db_path", outputDBPath.replace("\\", "/"));
        input.put("generator", this);
        generateOutput(input, "main-micro-benchmark.ftl", writeToFile, generationOutputPath + "/" + "Main.java");
    }

}
