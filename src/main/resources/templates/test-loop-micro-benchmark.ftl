package ${package_name};

import org.junit.Test;
<#if generator.existsDataFile(data_path, loop.microbenchmarkClassName) == false >
import org.junit.Ignore;
</#if>

public class ${class_name}Test {

    @Test
    <#if generator.existsDataFile(data_path, loop.microbenchmarkClassName) == false >
    @Ignore
    </#if>
    public void testOriginal() {
        ${class_name}_ORIGINAL benchmark =
            new ${class_name}_ORIGINAL();
        benchmark.setup();
        benchmark.doBenchmark();
    }

    @Test
    <#if generator.existsDataFile(data_path, loop.microbenchmarkClassName) == false >
    @Ignore
    </#if>
    public void testGracefully() {
        ${class_name}_GRACEFULLY benchmark =
            new ${class_name}_GRACEFULLY();
        benchmark.setup();
        benchmark.doBenchmark();
    }
}
