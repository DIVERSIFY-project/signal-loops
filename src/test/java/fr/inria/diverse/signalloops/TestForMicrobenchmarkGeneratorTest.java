package fr.inria.diverse.signalloops;

import fr.inria.diverse.signalloops.codegenerators.MainClassGenerator;
import fr.inria.diverse.signalloops.codegenerators.TestForMicrobenchmarkGenerator;
import fr.inria.diverse.signalloops.model.SignalLoop;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by marodrig on 28/09/2015.
 */
public class TestForMicrobenchmarkGeneratorTest extends BenchmarkTest {

    @Before
    public void setup() throws Exception {
        theLoop = buildSignalLoop();
    }

    @Test
    public void testBenchmarkOriginall() throws Exception {
        TestForMicrobenchmarkGenerator generator = new TestForMicrobenchmarkGenerator();
        generator.setWriteToFile(false);
        generator.initialize(Thread.currentThread().getContextClassLoader().getResource("templates").toURI().getPath());
        generator.generate("fr.mypackage", "/output_sources", "/generated/path", theLoop);

        //System.out.println(generator.getOutput());
        boolean contains = generator.getOutput().contains(
                "new fr_inria_juncoprovider_testproject_Arithmetic_18_GRACEFULLY()");
        assertEquals(true, contains);
    }
}
