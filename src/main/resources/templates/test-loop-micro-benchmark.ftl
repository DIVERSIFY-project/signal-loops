package ${package_name};

import org.junit.Test;

public class ${class_name}Test {

    @Test
    public void testOriginal() {
        ${class_name} benchmark = new ${class_name}();
        benchmark.setup();
        benchmark.${class_name}_ORIGINAL();
    }

    @Test
    public void testGracefully() {
        ${class_name}Gracefully benchmark = new ${class_name}Gracefully();
        benchmark.setup();
        benchmark.${class_name}_GRACEFULLY();
    }
}
