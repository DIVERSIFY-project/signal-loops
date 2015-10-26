package fr.inria.diverse.signalloops.detectors.logic;

import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This visitor determines what statement can be degraded (i.e. transformed into something else or removed)
 *
 * <p/>
 * Created by marodrig on 07/10/2015.
 */
public class MutableEvaluatorVisitor extends SignalLoopVisitors {

    public static enum Mutability {IMMUTABLE, REPLACEABLE, ERASABLE}

    private HashMap<CtStatement, Mutability> mutability;

    public HashMap<CtStatement, Mutability> getMutability() {
        return mutability;
    }

    /**
     * Def-Chain resulting of the visits
     */
    DefChainGraph result = new DefChainGraph();

    /**
     * Set of variables local to statement
     */
    Set<CtVariableReference> localToStatement = new HashSet<CtVariableReference>();

    public CycleDetector<CtVariableReference, DefaultEdge> buildDetector(CtBlock loopBody, Set<CtVariableReference> localToLoop) {
        mutability = new HashMap<CtStatement, Mutability>();

        this.localToStatement = localToLoop;

        loopBody.accept(this);

        return new CycleDetector<CtVariableReference, DefaultEdge>(result);
    }


    public static class DefChainGraph extends DefaultDirectedGraph<CtVariableReference, DefaultEdge> {

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




    //-------------------------------------------------------------------------------------------------------

    @Override
    public <T, E extends CtExpression<?>> void visitCtArrayAccess(CtArrayAccess<T, E> arrayAccess) {
        arrayAccess.getIndexExpression().accept(this);
    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {
        //Obtain all variable access in the assignment expression
        List<CtVariableAccess> left = accessOfLeftExpression(assignement.getAssigned());
        List<CtVariableAccess> right = accessOfExpression(assignement.getAssignment());
        //Create a graph such as there is an edge when a variable depends of another in some point
        //in the code.
        /**
         * @branch-in: template1, configurationA
         */
        for (CtVariableAccess l : left) {
            if (!result.containsVertex(l.getVariable())) result.addVertex(l.getVariable());
            for (CtVariableAccess r : right) {
                if (!result.containsVertex(r.getVariable())) result.addVertex(r.getVariable());
                if ( !result.containsEdge(l.getVariable(), r.getVariable()))

                    //Avoid special case of local variables pointing themselves like in
                    //int a = a + 2
                    if ( l.getVariable().equals(r.getVariable()) &&
                            localToStatement.contains(l.getVariable()) ) continue;

                    result.addEdge(l.getVariable(), r.getVariable());
            }

            if (l.getVariable() instanceof CtLocalVariable) visitCtLocalVariable((CtLocalVariable) l.getVariable());
        }
        /**
         * @branch-out
         */
    }

    /**
     * Indicates if an edge exists already between a local var and a variable access
     * @return
     */
    private boolean existEdge(CtLocalVariable a, CtVariableAccess b) {
        return result.containsEdge(a.getReference(), b.getVariable());
    }

    /**
     * Indicates if a cyclic dependencies is to a non-local vars
     * @return
     */
    private boolean cycleToLocalToStatement(CtLocalVariable a, CtVariableAccess b) {
        return a.getReference().equals(b.getVariable()) &&
                localToStatement.contains(b.getVariable());
    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        if (!localToStatement.contains(localVariable)) localToStatement.add(localVariable.getReference());
        if (!result.containsVertex(localVariable.getReference())) result.addVertex(localVariable.getReference());

        if (localVariable.getDefaultExpression() != null) {
            List<CtVariableAccess> right = accessOfExpression(localVariable.getDefaultExpression());
            for (CtVariableAccess r : right) {
                //Avoid repeated edges
                if (!result.containsVertex(r.getVariable()))
                    result.addVertex(r.getVariable());

                //Avoid repeated edges and cycles to vars local to the block being analyzed
                if ( !existEdge(localVariable, r) && !cycleToLocalToStatement(localVariable, r) )
                        result.addEdge(localVariable.getReference(), r.getVariable());
            }
        }
    }

    @Override
    public <T, A extends T> void visitCtOperatorAssignement(CtOperatorAssignment<T, A> assignment) {
        this.visitCtAssignment(assignment);
    }

    //-------------------------------------------------------------------------------------------------------


}
