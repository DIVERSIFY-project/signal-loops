package fr.inria.diverse.signalloops;

import fr.inria.diverse.signalloops.codegenerators.MicrobenchmarkGenerator;
import fr.inria.diverse.signalloops.model.SignalLoop;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import org.junit.Before;
import org.junit.Test;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.QueueProcessingManager;
import spoon.support.reflect.code.CtVariableAccessImpl;
import spoon.support.reflect.reference.CtTypeReferenceImpl;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MicrobenchmarkGeneratorTest extends BenchmarkTest {

    SignalLoop theLoop;

    @Before
    public void setup() throws Exception {
        theLoop = buildSignalLoop();
    }

    @Test
    public void testBenchmarkOriginall() throws Exception {
        MicrobenchmarkGenerator generator = new MicrobenchmarkGenerator();
        generator.setWriteToFile(false);
        generator.initialize(Thread.currentThread().getContextClassLoader().getResource("templates").toURI().getPath());
        generator.generate("fr.mypackage", "/output_sources", "/generated/path", theLoop, false);


        //System.out.println(generator.getOutput());

        assertTrue(generator.getOutput().contains("package fr.mypackage"));
        assertTrue(generator.getOutput().contains("class fr_inria_juncoprovider_testproject_Arithmetic"));
        assertTrue(generator.getOutput().contains("static final String INPUT_DATA_FILE = \"fr-inria-juncoprovider-testproject-Arithmetic-18\";"));
        assertTrue(generator.getOutput().contains("int a ;"));
        assertTrue(generator.getOutput().contains("b = Loader.readint(s);"));
    }
}
