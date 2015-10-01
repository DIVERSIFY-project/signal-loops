package fr.inria.diverse.signalloops;

import fr.inria.diverse.signalloops.codegenerators.MainClassGenerator;
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

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MainClassGeneratorTest extends BenchmarkTest{

    @Before
    public void setup() throws Exception {
        theLoop = buildSignalLoop();
    }

    @Test
    public void testBenchmarkOriginall() throws Exception {
        MainClassGenerator generator = new MainClassGenerator();
        generator.setWriteToFile(false);
        generator.initialize(Thread.currentThread().getContextClassLoader().getResource("templates").toURI().getPath());
        ArrayList<SignalLoop> loops = new ArrayList<SignalLoop>();
        loops.add(theLoop);
        generator.generate("fr.mypackage", "/output_sources", "/generated/path", "/dbpath.sb3", loops);

        //System.out.println(generator.getOutput());
        boolean contains = generator.getOutput().contains(
                ".include(fr_inria_juncoprovider_testproject_Arithmetic_18_ORIGINAL");
        assertEquals(true, contains);
    }
}
