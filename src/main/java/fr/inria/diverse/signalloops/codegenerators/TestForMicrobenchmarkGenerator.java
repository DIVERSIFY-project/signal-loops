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
        input.put("data_path", dataPath);
        input.put("generator", this);
        input.put("class_name", loop.getMicrobenchmarkClassName());
        input.put("loop", loop);
        generateOutput(input, "test-loop-micro-benchmark.ftl", writeToFile,
                generationOutputTestPath + "/" + loop.getMicrobenchmarkClassName() + "Test.java");
    }
}
