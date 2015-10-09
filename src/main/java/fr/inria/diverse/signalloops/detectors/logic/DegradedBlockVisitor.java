package fr.inria.diverse.signalloops.detectors.logic;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;

import java.util.List;
import java.util.Set;

/**
 * Creates a new block by visiting an old one maintaining only the immutable statements
 * <p/>
 * Created by marodrig on 07/10/2015.
 */
public class DegradedBlockVisitor extends SignalLoopVisitors {

    private static enum Mutability {IMMUTABLE, REPLACEABLE, ERASABLE}

    private CycleDetector<CtVariableReference, DefaultEdge> cycleDetector;

    private Set<CtVariableReference> localVariables;

    private boolean isEmpty(CtElement statement) {
        boolean result = statement instanceof CtCodeSnippetStatement;
        result |= (statement instanceof CtBlock) && ((CtBlock) statement).getStatements().size() == 0;
        return result;
    }

    /**
     * Indicates if the element contains unary operators acting on variables non-local to itself
     *
     * @param element Element to inspect
     * @return True if contains
     */
    private boolean containsNonLocalUnaryOperators(CtElement element) {
        //Handling unary operators
        for (CtUnaryOperator op :
                element.getElements(new TypeFilter<CtUnaryOperator>(CtUnaryOperator.class))) {
            for (CtVariableAccess a : accessOfExpression(op)) {
                //Add cyclic dependencies to external variables
                if (!localVariables.contains(a.getVariable())) return true;
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
                if (!localVariables.contains(a.getVariable())) return true;
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
            if (s.getKind().compareTo(UnaryOperatorKind.POSTDEC) >= 0)
                opBlock.addStatement(s);
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
                    if (!localVariables.contains(ref) && cycleDetector.detectCyclesContainingVertex(ref)) {
                        result = Mutability.IMMUTABLE;
                    }
                } catch (IllegalArgumentException ex) {
                    continue;
                }
            }
        }
        if (result == Mutability.ERASABLE) {
            if (containNonLocalOperatorAssignment(statement)) return Mutability.IMMUTABLE;
            else if (containsNonLocalUnaryOperators(statement)) return Mutability.REPLACEABLE;
        }
        return Mutability.ERASABLE;
    }

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
        if (mutability(e) == Mutability.ERASABLE) remove(e);
        if (mutability(e) == Mutability.REPLACEABLE) replaceByUnaryBlock(e);

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
    public void visitCtDo(CtDo doLoop) {
        doLoop.getBody().accept(this);
        doLoop.getLoopingExpression().accept(this);
        if (isEmpty(doLoop.getLoopingExpression()) && isEmpty(doLoop.getBody())) remove(doLoop);
    }


    @Override
    public <T, A extends T> void visitCtOperatorAssignement(CtOperatorAssignment<T, A> assignment) {
        for (CtVariableAccess a : accessOfExpression(assignment.getAssigned())) {
            //Add cyclic dependencies
            if (!localVariables.contains(a.getVariable())) {
                remove(assignment);
                return;
            }
        }
    }

    @Override
    public void visitCtIf(CtIf ifElement) {
        //super.visitCtIf(ifElement);

        Mutability condMut = mutability(ifElement.getCondition());
        if (ifElement.getThenStatement() != null) ifElement.getThenStatement().accept(this);
        if (ifElement.getElseStatement() != null) ifElement.getElseStatement().accept(this);

        if (condMut == Mutability.ERASABLE && isEmpty(ifElement.getThenStatement())) {
            //if ( - ) {  } else {  } <- Remove the whole if
            if (isEmpty(ifElement.getElseStatement())) remove(ifElement);
                //if ( - ) {  } else { do }
            else ifElement.getElseStatement().setParent(ifElement.getParent());
        }

        //if ( - ) {  } else { do }
    }

    @Override
    public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
        for (CtVariableAccess a : accessOfExpression(operator)) {
            //Add cyclic dependencies to external variables
            if (localVariables.contains(a.getVariable())) {
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

    public void setLocalVariables(Set<CtVariableReference> localVariables) {
        this.localVariables = localVariables;
    }

    public Set<CtVariableReference> getLocalVariables() {
        return localVariables;
    }
}
