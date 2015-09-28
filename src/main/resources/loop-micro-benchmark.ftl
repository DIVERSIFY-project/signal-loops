package ${package_name};

import org.openjdk.jmh.annotations.*;
import java.io.DataInputStream;

${class_comments}
@State(Scope.Thread)
public class ${class_name} {

    static final String INPUT_ROOT_FOLDER = ${input_root_folder_path}
    static final String INPUT_DATA_FILE = ${input_data_file_path}


    <#list inputs_vars as input_var>
      ${input_var};
    </#list>

    @Setup(Level.Invocation)
    public void setup() {
        try {
            DataInputStream s = Loader.getStream(INPUT_ROOT_FOLDER, INPUT_DATA_FILE);

            <#list inputs_inits as input_init>
                  ${input_init};
            </#list>

            s.close();
        } catch(Exception e) { throw new RuntimeException(e); }
    }

    @Benchmark
    public void ${benchmark_name}() {
        ${benchmark_lines_of_code}
    }
}