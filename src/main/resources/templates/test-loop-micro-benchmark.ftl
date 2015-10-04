<#macro assertions>
<#list input_vars as input_var>
  <#if input_var.initialized == true >
        ${input_var.variableType} ${input_var.variableName} = Loader.read${input_var.loadMethodName}(s);
      <#if input_var.isArray == true >
        assertArraysEquals(${input_var.variableName}, benchmark.${input_var.variableName});
      <#else>
        assertEquals(${input_var.variableName}, benchmark.${input_var.variableName});
      </#if>
  </#if>
</#list>
</#macro>

package ${package_name};

import org.junit.Test;
import java.io.DataInputStream;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class ${class_name}Test {

    static final String DATA_ROOT_FOLDER = "${data_root_folder_path}";
    static final String DATA_FILE = "${data_file_path}";

    private void assertArraysEquals(double[] a, double[] b) { assertArrayEquals(a, b, 0.00001d); }
    private void assertArraysEquals(float[] a, float[] b) { assertArrayEquals(a, b, 0.00001f); }

    @Test
    public void testOriginal() throws Exception {
        DataInputStream s = Loader.getStream(DATA_ROOT_FOLDER, DATA_FILE);

        ${class_name}_ORIGINAL benchmark =
            new ${class_name}_ORIGINAL();
        benchmark.setup();
        benchmark.doBenchmark();

<@assertions/>

        s.close();

    }

    @Test
    public void testGracefully() throws Exception {

        DataInputStream s = Loader.getStream(DATA_ROOT_FOLDER, DATA_FILE);

        ${class_name}_GRACEFULLY benchmark =
            new ${class_name}_GRACEFULLY();
        benchmark.setup();
        benchmark.doBenchmark();

<@assertions/>

        s.close();
    }
}
