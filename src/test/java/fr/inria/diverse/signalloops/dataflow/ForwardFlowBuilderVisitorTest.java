package fr.inria.diverse.signalloops.dataflow;

import fr.inria.diversify.syringe.SpoonMetaFactory;
import org.junit.Test;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.QueueProcessingManager;

import java.net.URISyntaxException;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by marodrig on 14/10/2015.
 */
public class ForwardFlowBuilderVisitorTest {



    @Test
    public void createGraph() throws Exception {

        final ForwardFlowBuilderVisitor visitor = new ForwardFlowBuilderVisitor();

        Factory factory = new SpoonMetaFactory().buildNewFactory(
                this.getClass().getResource("/input_sources").toURI().getPath(), 5);
        ProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(new AbstractProcessor<CtMethod>() {
            @Override
            public void process(CtMethod element) {
                if (element.getSimpleName().equals("addConditional")) {
                    element.accept(visitor);
                }
            }

        });
        pm.process();


        ControlFlowGraph graph = visitor.getResult();
        assertEquals(graph.vertexSet().size(), 7);
        assertEquals(graph.branchCount(), 2);
        assertEquals(graph.statementCount(), 5);

    }

}
