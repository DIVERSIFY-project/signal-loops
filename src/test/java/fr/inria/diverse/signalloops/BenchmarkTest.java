package fr.inria.diverse.signalloops;

import fr.inria.diverse.signalloops.model.SignalLoop;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.QueueProcessingManager;

import java.util.List;

/**
 * Created by marodrig on 29/09/2015.
 */
public class BenchmarkTest {

    SignalLoop theLoop;

    protected SignalLoop buildSignalLoop() throws Exception {

        theLoop = new SignalLoop();

        //Initialize the CtElements

        Factory factory = new SpoonMetaFactory().buildNewFactory(this.getClass().getResource("/input_sources").toURI().getPath(), 5);
        ProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(new AbstractProcessor<CtLoop>() {
            @Override
            public void process(CtLoop element) {
                if ( theLoop.getLoop() != null ) return;
                theLoop.setLoop(element);
                List<CtVariableAccess> access = theLoop.getLoop().getElements(
                        new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
                theLoop.getAccesses().addAll(access);
                access.remove(0);//Make not all variables initialized
                theLoop.getInitialized().addAll(access);
            }
        });
        pm.process();

        theLoop.setDegradedSnippet("i++;");
        theLoop.setId(4);

        return theLoop;
    }

}
