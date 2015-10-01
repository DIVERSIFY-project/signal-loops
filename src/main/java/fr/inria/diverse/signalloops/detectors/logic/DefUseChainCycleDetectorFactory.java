package fr.inria.diverse.signalloops.detectors.logic;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Set;

/**
 *
 * Class build detectors that detect cycles in def-use chains.
 *
 * Created by marodrig on 23/09/2015.
 */
public class DefUseChainCycleDetectorFactory {

    private List<CtVariableAccess> accessOfExpression(CtExpression expression) {
        //Detect all variables in the index expression of the array
        return expression.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
    }

    /**
     * Builds the Def - use graph to feed the cycle detector
     *
     * @param loopBody    Body in wich the cycles are going to be detected
     * @param localToLoop Set of variables that are local to the body [optional]
     * @return
     */
    private DirectedGraph<CtVariableReference, DefaultEdge> build(
            CtBlock loopBody, Set<CtVariableReference> localToLoop) {
        //Build the def-use graph. Add only local variables non defined within the loop
        DefaultDirectedGraph<CtVariableReference, DefaultEdge> g =
                new DefaultDirectedGraph<CtVariableReference, DefaultEdge>(DefaultEdge.class);
        List<CtStatement> sts = loopBody.getStatements();
        for (CtStatement st : sts) {
            //Watch assignments
            if (st instanceof CtAssignment) {
                //Obtain all variable access in the assignment expression
                CtAssignment assign = (CtAssignment) st;
                List<CtVariableAccess> left = accessOfExpression(assign.getAssigned());
                List<CtVariableAccess> right = accessOfExpression(assign.getAssignment());
                //Create a graph such as there is an edge when a variable depends of another in some point
                //in the code.
                for (CtVariableAccess l : left) {
                    if (!g.containsVertex(l.getVariable())) g.addVertex(l.getVariable());
                    for (CtVariableAccess r : right) {
                        if (!g.containsVertex(r.getVariable())) g.addVertex(r.getVariable());
                        g.addEdge(l.getVariable(), r.getVariable());
                    }
                }
            }
            if (st instanceof CtLocalVariable) {
                //Mark all variables local to the loop
                CtLocalVariable localVariable = (CtLocalVariable) st;
                localToLoop.add(localVariable.getReference());
                g.addVertex(localVariable.getReference());

                if (localVariable.getDefaultExpression() != null) {
                    List<CtVariableAccess> right = accessOfExpression(localVariable.getDefaultExpression());
                    for (CtVariableAccess r : right) {
                        if (!g.containsVertex(r.getVariable())) g.addVertex(r.getVariable());
                        g.addEdge(localVariable.getReference(), r.getVariable());
                    }
                }
            }
        }

        return g;
    }

    public CycleDetector<CtVariableReference, DefaultEdge> buildDetector(
            CtBlock loopBody, Set<CtVariableReference> localToLoop) {
        CycleDetector<CtVariableReference, DefaultEdge> cycleDetector
                = new CycleDetector<CtVariableReference, DefaultEdge>(build(loopBody, localToLoop));
        return cycleDetector;
    }
}
