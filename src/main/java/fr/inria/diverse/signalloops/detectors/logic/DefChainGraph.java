package fr.inria.diverse.signalloops.detectors.logic;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import spoon.reflect.reference.CtVariableReference;

/**
* Created by marodrig on 07/10/2015.
*/
public class DefChainGraph extends DefaultDirectedGraph<CtVariableReference, DefaultEdge> {

    public DefChainGraph(Class<? extends DefaultEdge> edgeClass) {
        super(edgeClass);
    }

    public DefChainGraph(EdgeFactory<CtVariableReference, DefaultEdge> ef) {
        super(ef);
    }

    public DefChainGraph() {
        super(DefaultEdge.class);
    }
}
