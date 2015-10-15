package fr.inria.diverse.signalloops.dataflow;

import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static fr.inria.diverse.signalloops.dataflow.BranchKind.BRANCH;
import static fr.inria.diverse.signalloops.dataflow.BranchKind.CONVERGE;
import static fr.inria.diverse.signalloops.dataflow.BranchKind.STATEMENT;

/**
 * Created by marodrig on 13/10/2015.
 */
public class ControlFlowGraph extends DefaultDirectedGraph<ControlFlowNode, ControlFlowEdge> {

    /**
     * Description of the graph
     */
    private String name;

    public ControlFlowGraph(Class<? extends ControlFlowEdge> edgeClass) {
        super(edgeClass);
    }

    public ControlFlowGraph() {
        super(ControlFlowEdge.class);
    }

    private int countNodes(BranchKind kind) {
        int result = 0;
        for (ControlFlowNode v : vertexSet())
            if (v.getKind().equals(kind)) result++;
        return result;
    }

    public String toGraphVisText() {
        GraphVisPrettyPrinter p = new GraphVisPrettyPrinter(this);
        return p.print();
    }

    /**
     * Find all nodes of a given kind
     *
     * @param kind
     * @return
     */
    private List<ControlFlowNode> findNodesOfKind(BranchKind kind) {
        ArrayList<ControlFlowNode> result = new ArrayList<ControlFlowNode>();
        for (ControlFlowNode n : vertexSet())
            if (n.getKind().equals(kind)) result.add(n);
        return result;
    }

    @Override
    public ControlFlowEdge addEdge(ControlFlowNode source, ControlFlowNode target) {
        if (!containsVertex(source)) addVertex(source);
        if (!containsVertex(target)) addVertex(target);
        return super.addEdge(source, target);
    }

    /**
     * Returns all statements
     */
    public List<ControlFlowNode> statements() {
        return findNodesOfKind(STATEMENT);
    }

    /**
     * Returns all branches
     */
    public List<ControlFlowNode> branches() {
        return findNodesOfKind(BRANCH);
    }

    /**
     * Removes all convergence nodes
     */
    public void simplify() {
        List<ControlFlowNode> convergence = findNodesOfKind(CONVERGE);
        for (ControlFlowNode n : convergence) {
            Set<ControlFlowEdge> incoming = incomingEdgesOf(n);
            Set<ControlFlowEdge> outgoing = outgoingEdgesOf(n);
            for (ControlFlowEdge in : incoming)
                for (ControlFlowEdge out : outgoing)
                    addEdge(in.getSourceNode(), out.getTargetNode());

            for (ControlFlowEdge e : edgesOf(n)) removeEdge(e);
            removeVertex(n);
        }

    }

    public int branchCount() {
        return countNodes(BRANCH);
    }

    public int statementCount() {
        return countNodes(STATEMENT);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
