package fr.inria.diverse.signalloops.detectors.logic;

import org.apache.log4j.Logger;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.*;

import java.util.List;
import java.util.Set;

import static fr.inria.diverse.signalloops.detectors.logic.MutableEvaluatorVisitor.*;

/**
 * Mutates a block by degrading it.
 *
 * <p/>
 * Created by marodrig on 07/10/2015.
 */
public class MutatorVisitor extends SignalLoopVisitors {

    private Logger log = Logger.getLogger(MutatorVisitor.class);



    private CycleDetector<CtVariableReference, DefaultEdge> cycleDetector;

    /**
     * Set of variables declared inside the block
     */
    private Set<CtVariableReference> declaredInsideBlock;

    /**
     * Indicates if an element contains no other elements inside
     *
     * @param statement
     * @return
     */
    private boolean isEmpty(CtElement statement) {
        boolean result = statement instanceof CtCodeSnippetStatement;
        result |= (statement instanceof CtBlock) && ((CtBlock) statement).getStatements().size() == 0;
        return result;
    }

    /**
     * Indicates if the element contains unary operators acting on variables declared outside the
     * block being degraded
     *
     * @param element Element to inspect
     * @return True if contains
     */
    private boolean containsNonLocalUnaryOperators(CtElement element) {
        //Handling unary operators
        for (CtUnaryOperator op :
                element.getElements(new TypeFilter<CtUnaryOperator>(CtUnaryOperator.class))) {
            for (CtVariableAccess a : accessOfExpression(op)) {
                //We don't care about cyclic dependencies to vars declared inside the block being degraded
                if (!declaredInsideBlock.contains(a.getVariable())) return true;
            }
        }
        return false;
    }

    /**
     * Indicates that contains operation assignment over variables non local to the element
     *
     * @param element
     * @return
     */
    private boolean containNonLocalOperatorAssignment(CtElement element) {
        //Handling operators assignment
        for (CtOperatorAssignment op :
                element.getElements(new TypeFilter<CtOperatorAssignment>(CtOperatorAssignment.class))) {
            for (CtVariableAccess a : accessOfExpression(op.getAssigned())) {
                //Add cyclic dependencies
                if (!declaredInsideBlock.contains(a.getVariable())) return true;
            }
        }
        return false;
    }


    /**
     * Replaces the element by an statement list containing all unary operators in the element
     *
     * @param element
     */
    private void replaceByUnaryBlock(CtElement element) {
        CtBlock<CtUnaryOperator> opBlock = new CtBlockImpl<CtUnaryOperator>();
        for (CtUnaryOperator s : element.getElements(new TypeFilter<CtUnaryOperator>(CtUnaryOperator.class)))
            if (s.getKind().compareTo(UnaryOperatorKind.PREINC) >= 0) {
                s.setParent(null);
                opBlock.addStatement(s);
                s.setParent(opBlock);
            }
        element.replace(opBlock);
    }

    /**
     * Indicate if the statement contains a mutable expression
     * <p/>
     * A mutability expression is an expression that assigns value to a variable in the left side
     * using that variable also in the right side, like this:
     * <p/>
     * a = a * b
     * <p/>
     * or like this:
     * c = a * 2
     * a = c + b
     * <p/>
     * <p/>
     * Also, all unary operators and are mutable:
     * a--;
     * a++;
     *
     * @param statement Statement to check whether is a mutability expression
     * @return True if it is a mutability expression
     */
    private Mutability mutability(CtElement statement) {
        Mutability result = Mutability.ERASABLE;
        if (statement instanceof CtAssignment) {
            CtAssignment e = (CtAssignment) statement;
            List<CtVariableAccess> left = accessOfLeftExpression(e.getAssigned());
            for (CtVariableAccess access : left) {
                CtVariableReference ref = access.getVariable();
                try {
                    if (!declaredInsideBlock.contains(ref) && cycleDetector.detectCyclesContainingVertex(ref)) {
                        return Mutability.IMMUTABLE;
                    }
                } catch (IllegalArgumentException ex) {
                    continue;
                }
            }
        }

        if (containNonLocalOperatorAssignment(statement)) return Mutability.IMMUTABLE;
        else if (containsNonLocalUnaryOperators(statement)) return Mutability.REPLACEABLE;
        else return Mutability.ERASABLE;
    }

    /**
     * Remove an statement
     *
     * @param statement
     */
    private void remove(CtStatement statement) {
        if (statement.getParent() instanceof CtBlock) {
            ((CtBlock) statement.getParent()).removeStatement(statement);
        } else {
            CtCodeSnippetStatementImpl comment = new CtCodeSnippetStatementImpl();
            comment.setValue("/*REMOVED*/");
            statement.replace(comment);
        }
    }

    /**
     * Performs the default action over an statement
     *
     * @param e
     */
    private void defaultAction(CtStatement e) {
        Mutability m = mutability(e);
        if (m == Mutability.ERASABLE) remove(e);
        if (m == Mutability.REPLACEABLE) replaceByUnaryBlock(e);
    }

    /**
     * Indicate if a variable is involved in a cycle containing non-local variables
     * <p/>
     * These cycles are immutable.
     *
     * @param ref
     */
    private boolean immutableCycle(CtVariableReference ref) {
        Set<CtVariableReference> cycle = cycleDetector.findCyclesContainingVertex(ref);
        for (CtVariableReference r : cycle) {
            if (!declaredInsideBlock.contains(r)) return true;
        }
        return false;
    }

    /**
     * Pretty print for the resulting degraded block
     *
     * @param clonedBody
     * @return
     */
    public String prettyPrintBody(CtStatement clonedBody) {
        String result = "";
        if (clonedBody instanceof CtBlock) {
            CtBlock block = (CtBlock) clonedBody;
            try {
                for (int i = 0; i < block.getStatements().size(); i++) {
                    if (block.getStatement(i) instanceof CtBlock) result += prettyPrintBody(block.getStatement(i));
                    else if (block.getStatement(i) != null) result += block.getStatement(i).toString() + ";\n";
                }
            } catch (NullPointerException ex) {
              log.error("Unable to print the degraded loop!");
            }
        } else result = clonedBody.toString();
        return result;
    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {
        defaultAction(assignement);
    }

    @Override
    public <R> void visitCtBlock(CtBlock<R> block) {
        int i = 0;
        while (i < block.getStatements().size()) {
            int size = block.getStatements().size();
            CtStatement s = block.getStatement(i);
            s.accept(this);
            if (block.getStatements().size() >= size) i++;
        }
    }


    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        if (immutableCycle(localVariable.getReference())) {
            //Is immutable, need to be replaced by an assignment
            CtAssignment<T, T> replacement =
                    localVariable.getFactory().Code().createVariableAssignment(
                            localVariable.getReference(), false, localVariable.getDefaultExpression());
            localVariable.replace(replacement);
        } else defaultAction(localVariable);
    }

    @Override
    public void visitCtDo(CtDo doLoop) {
        //Make background copy of the loop
        CtDo clone = doLoop.getFactory().Core().clone(doLoop);

        //Work over the copy
        clone.getBody().accept(this);
        clone.getLoopingExpression().accept(this);

        //If the copy is erasable, so the original
        if (isEmpty(clone.getLoopingExpression()) && isEmpty(clone.getBody())) {
            remove(doLoop);
        }
    }


    @Override
    public <T, A extends T> void visitCtOperatorAssignement(CtOperatorAssignment<T, A> assignment) {
        for (CtVariableAccess a : accessOfExpression(assignment.getAssigned())) {
            //Add cyclic dependencies
            if (!declaredInsideBlock.contains(a.getVariable())) {
                remove(assignment);
                return;
            }
        }
    }

    @Override
    public void visitCtIf(CtIf ifElement) {
        //super.visitCtIf(ifElement);

        CtStatement ctThen = ifElement.getThenStatement();
        CtStatement ctElse = ifElement.getElseStatement();

        Mutability condMut = mutability(ifElement.getCondition());
        if (ctThen != null) ifElement.getThenStatement().accept(this);
        if (ctElse != null) ifElement.getElseStatement().accept(this);

        if (condMut == Mutability.ERASABLE && isEmpty(ctThen)) {
            //if ( - ) {  } else {  } <- Remove the whole if
            if (ctElse == null || isEmpty(ctElse)) remove(ifElement);
                //else if case: if ( - ) {  } else if { doSomething() } <-- pull the else if element up
            else if (ctElse instanceof CtIf) ctElse.setParent(ifElement.getParent());
        }
    }

    @Override
    public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
        for (CtVariableAccess a : accessOfExpression(operator)) {
            //Add cyclic dependencies to external variables
            if (declaredInsideBlock.contains(a.getVariable())) {
                remove(operator);
                return;
            }
        }
    }


    public void setCycleDetector(CycleDetector<CtVariableReference, DefaultEdge> cycleDetector) {
        this.cycleDetector = cycleDetector;
    }

    public CycleDetector<CtVariableReference, DefaultEdge> getCycleDetector() {
        return cycleDetector;
    }

    public void setDeclaredInsideBlock(Set<CtVariableReference> declaredInsideBlock) {
        this.declaredInsideBlock = declaredInsideBlock;
    }

    public Set<CtVariableReference> getDeclaredInsideBlock() {
        return declaredInsideBlock;
    }
}
