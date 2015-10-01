package ${package_name};

import org.openjdk.jmh.annotations.*;
import java.io.DataInputStream;

/**
 *  ${class_comments}
 */
@State(Scope.Thread)
public class ${class_name}_${degraded_type} {

    static final String INPUT_ROOT_FOLDER = "${input_root_folder_path}";
    static final String INPUT_DATA_FILE = "${input_data_file_path}";

<#list input_vars as input_var>
    public ${input_var.variableType} ${input_var.variableName} ;
</#list>

    @Setup(Level.Invocation)
    public void setup() {
        try {
            DataInputStream s = Loader.getStream(INPUT_ROOT_FOLDER, INPUT_DATA_FILE);

    <#list input_vars as input_var>
        <#if input_var.initialized == true >
            ${input_var.variableName} = Loader.read${input_var.loadMethodName}(s);
        </#if>
    </#list>

            s.close();
        } catch(Exception e) { throw new RuntimeException(e); }
    }

    ${static_methods}

    @Benchmark
    public void doBenchmark() {
    ${loop_code}
    }
}