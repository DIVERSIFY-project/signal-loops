package fr.inria.diverse.signalloops;

import fr.inria.diverse.signalloops.codegenerators.MicrobenchmarkGenerator;
import fr.inria.diverse.signalloops.model.SignalLoop;
import org.junit.Test;
import spoon.reflect.code.CtVariableAccess;
import spoon.support.reflect.code.CtVariableAccessImpl;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static junit.framework.Assert.assertTrue;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MicrobenchmarkGeneratorTest {

    private SignalLoop buildSignalLoop() {
        SignalLoop result = new SignalLoop();
        result.setId(4);

        ArrayList<CtVariableAccess> accesses = new ArrayList<CtVariableAccess>();
        CtVariableAccess access = new CtVariableAccessImpl();
        access.setType(new CtTypeReferenceImpl<Double>());
        accesses.add(new CtVariableAccessImpl());
        result.setAccesses(accesses);
        return result;
    }

    @Test
    public void testBenchmarkOriginall() throws URISyntaxException, IOException {
        MicrobenchmarkGenerator generator = new MicrobenchmarkGenerator();
        generator.initialize(MicrobenchmarkGenerator.class.getResource("/").toURI().toString());
        generator.generateBenchmark("fr.mypackage", "/output/path", "/generated/path", buildSignalLoop(), false);

        assertTrue(generator.getOutput().contains(""))
    }



}
