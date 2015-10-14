package fr.inria.diverse.signalloops.dataflow;

import org.jgrapht.graph.DefaultEdge;

/**
 * Created by marodrig on 13/10/2015.
 */
public class ControlFlowEdge extends DefaultEdge {

    public ControlFlowNode getTargetNode() {
        return (ControlFlowNode)getTarget();
    }

    public ControlFlowNode getSourceNode() {
        return (ControlFlowNode)getSource();
    }

}
