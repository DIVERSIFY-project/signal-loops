package fr.inria.diverse.signalloops.dataflow;

import org.jgrapht.graph.DefaultDirectedGraph;

import static fr.inria.diverse.signalloops.dataflow.BranchKind.BRANCH;
import static fr.inria.diverse.signalloops.dataflow.BranchKind.STATEMENT;

/**
 * Created by marodrig on 13/10/2015.
 */
public class ControlFlowGraph extends DefaultDirectedGraph<ControlFlowNode, ControlFlowEdge> {
    public ControlFlowGraph(Class<? extends ControlFlowEdge> edgeClass) {
        super(edgeClass);
    }


    private int countNodes(BranchKind kind) {
        int result = 0;
        for (ControlFlowNode v : vertexSet())
            if (v.getKind().equals(kind)) result++;
        return result;
    }

    /**
     * Removes all convergence nodes
     */
    public void simplify() {

    }

    public int branchCount() {
        return countNodes(BRANCH);
    }

    public int statementCount() {
        return countNodes(STATEMENT);
    }
}
