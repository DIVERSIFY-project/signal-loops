package ${package_name};

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        <#list signal_loops as loop>
            <#if generator.existsDataFile(data_path, loop.microbenchmarkClassName) >
                .include(${loop.microbenchmarkClassName}_ORIGINAL.class.getSimpleName())
                .include(${loop.microbenchmarkClassName}_GRACEFULLY.class.getSimpleName())
            </#if>
        </#list>
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .shouldFailOnError(true)
                .build();

        Collection<RunResult> results = new Runner(opt).run();

        HashMap<String, Integer> ids = new HashMap<String, Integer>();
<#list signal_loops as loop>
    <#if generator.existsDataFile(data_path, loop.microbenchmarkClassName) >
        ids.put(${loop.microbenchmarkClassName}_ORIGINAL.class.getSimpleName(), ${loop.id});
        ids.put(${loop.microbenchmarkClassName}_GRACEFULLY.class.getSimpleName(), ${loop.id});
    </#if>
</#list>
        BenchmarkResultExporter exporter = new BenchmarkResultExporter();
        exporter.exportToDB("${db_path}", ids, results);
    }
}