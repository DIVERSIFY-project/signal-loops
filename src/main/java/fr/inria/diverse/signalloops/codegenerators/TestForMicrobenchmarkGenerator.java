package fr.inria.diverse.signalloops.codegenerators;

import fr.inria.diverse.signalloops.model.SignalLoop;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by marodrig on 29/09/2015.
 */
public class TestForMicrobenchmarkGenerator extends BenchmarkGenerator {

    public void generate(String packageName, String generationOutputTestPath, String dataPath, SignalLoop loop) {

        if ( !existsDataFile(dataPath, loop.getMicrobenchmarkClassName()) ) return;

        HashMap<String, Object> input = new HashMap<String, Object>();
        input.put("package_name", packageName);
        input.put("data_root_folder_path", dataPath.replace("\\", "/"));
        input.put("data_file_path", loop.getMicrobenchmarkClassName().replace("_", "-"));
        input.put("generator", this);
        input.put("class_name", loop.getMicrobenchmarkClassName());
        input.put("loop", loop);
        input.put("input_vars", getInjectionInputVariables(loop));
        generateOutput(input, "test-loop-micro-benchmark.ftl", writeToFile,
                generationOutputTestPath + "/" + loop.getMicrobenchmarkClassName() + "Test.java");
    }
}
