package fr.inria.diverse.signalloops.detectors.logic;

import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.CtVisitor;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the Def - use graph to feed the cycle detector
 * <p/>
 * Created by marodrig on 07/10/2015.
 */
public class DefChainCycleDetectorVisitor implements CtVisitor {


    /**
     * Def-Chain resulting of the visits
     */
    DefChainGraph result = new DefChainGraph();

    /**
     * Set of variables local to statement
     */
    Set<CtVariableReference> localToStatement = new HashSet<CtVariableReference>();

    public CycleDetector<CtVariableReference, DefaultEdge> buildDetector(CtBlock loopBody, Set<CtVariableReference> localToLoop) {
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


    private List<CtVariableAccess> accessOfExpression(CtExpression expression) {
        //Detect all variables in the index expression of the array
        return expression.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
    }

    @Override
    public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {

    }

    @Override
    public <T> void visitCtCodeSnippetExpression(CtCodeSnippetExpression<T> expression) {

    }

    @Override
    public void visitCtCodeSnippetStatement(CtCodeSnippetStatement statement) {

    }

    @Override
    public <A extends Annotation> void visitCtAnnotationType(CtAnnotationType<A> annotationType) {

    }

    @Override
    public void visitCtAnonymousExecutable(CtAnonymousExecutable anonymousExec) {
        anonymousExec.getBody().accept(this);
    }

    @Override
    public <T, E extends CtExpression<?>> void visitCtArrayAccess(CtArrayAccess<T, E> arrayAccess) {
        arrayAccess.getIndexExpression().accept(this);
    }

    @Override
    public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> reference) {

    }

    @Override
    public <T> void visitCtAssert(CtAssert<T> asserted) {
        asserted.getAssertExpression().accept(this);
        asserted.getExpression().accept(this);
    }

    @Override
    public <T> void visitCtVariableAccess(CtVariableAccess<T> variableAccess) {

    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {
        //Obtain all variable access in the assignment expression
        List<CtVariableAccess> left = accessOfLeftExpression(assignement.getAssigned());
        List<CtVariableAccess> right = accessOfExpression(assignement.getAssignment());
        //Create a graph such as there is an edge when a variable depends of another in some point
        //in the code.
        for (CtVariableAccess l : left) {
            if (!result.containsVertex(l.getVariable())) result.addVertex(l.getVariable());
            for (CtVariableAccess r : right) {
                if (!result.containsVertex(r.getVariable())) result.addVertex(r.getVariable());
                if (!result.containsEdge(l.getVariable(), r.getVariable()))
                    result.addEdge(l.getVariable(), r.getVariable());
            }

            if ( l.getVariable() instanceof CtLocalVariable ) visitCtLocalVariable((CtLocalVariable)l.getVariable());
        }

    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
//Mark all variables local to the loop
        if ( !localToStatement.contains(localVariable) ) localToStatement.add(localVariable.getReference());
        if (!result.containsVertex(localVariable.getReference()) ) result.addVertex(localVariable.getReference());

        if (localVariable.getDefaultExpression() != null) {
            List<CtVariableAccess> right = accessOfExpression(localVariable.getDefaultExpression());
            for (CtVariableAccess r : right) {
                if (!result.containsVertex(r.getVariable())) result.addVertex(r.getVariable());
                if (!result.containsEdge(localVariable.getReference(), r.getVariable()))
                    result.addEdge(localVariable.getReference(), r.getVariable());
            }
        }
    }

    private <T> List<CtVariableAccess> accessOfLeftExpression(CtExpression<T> assigned) {
        if (assigned instanceof CtArrayAccess) return accessOfLeftExpression(((CtArrayAccess) assigned).getTarget());
        return accessOfExpression(assigned);
    }

    @Override
    public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {

    }

    @Override
    public <R> void visitCtBlock(CtBlock<R> block) {
        for (CtStatement s : block.getStatements()) s.accept(this);
    }

    @Override
    public void visitCtBreak(CtBreak breakStatement) {

    }

    @Override
    public <S> void visitCtCase(CtCase<S> caseStatement) {
        for (CtStatement s : caseStatement.getStatements()) s.accept(this);
    }

    @Override
    public void visitCtCatch(CtCatch catchBlock) {
        catchBlock.getBody().accept(this);
    }

    @Override
    public <T> void visitCtClass(CtClass<T> ctClass) {

    }

    @Override
    public <T> void visitCtConditional(CtConditional<T> conditional) {
        conditional.getCondition().accept(this);
        conditional.getThenExpression().accept(this);
        if (conditional.getElseExpression() != null) conditional.getElseExpression().accept(this);
    }

    @Override
    public <T> void visitCtConstructor(CtConstructor<T> c) {

    }

    @Override
    public void visitCtContinue(CtContinue continueStatement) {

    }

    @Override
    public void visitCtDo(CtDo doLoop) {
        doLoop.getLoopingExpression().accept(this);
        doLoop.getBody().accept(this);
    }

    @Override
    public <T extends Enum<?>> void visitCtEnum(CtEnum<T> ctEnum) {

    }

    @Override
    public <T> void visitCtExecutableReference(CtExecutableReference<T> reference) {

    }

    @Override
    public <T> void visitCtField(CtField<T> f) {

    }

    @Override
    public <T> void visitCtTargetedAccess(CtTargetedAccess<T> targetedAccess) {

    }

    @Override
    public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {

    }

    @Override
    public <T> void visitCtFieldReference(CtFieldReference<T> reference) {

    }

    @Override
    public void visitCtFor(CtFor forLoop) {
        for (CtStatement s : forLoop.getForInit()) s.accept(this);
        for (CtStatement s : forLoop.getForUpdate()) s.accept(this);
        forLoop.getBody().accept(this);
    }

    @Override
    public void visitCtForEach(CtForEach foreach) {
        foreach.getVariable().accept(this);
        foreach.getExpression().accept(this);
        foreach.getBody().accept(this);
    }

    @Override
    public void visitCtIf(CtIf ifElement) {
        ifElement.getCondition().accept(this);
        ifElement.getThenStatement().accept(this);
        if (ifElement.getElseStatement() != null) ifElement.getElseStatement().accept(this);
    }

    @Override
    public <T> void visitCtInterface(CtInterface<T> intrface) {

    }

    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {

    }

    @Override
    public <T> void visitCtLiteral(CtLiteral<T> literal) {

    }



    @Override
    public <T> void visitCtLocalVariableReference(CtLocalVariableReference<T> reference) {

    }

    @Override
    public <T> void visitCtMethod(CtMethod<T> m) {

    }

    @Override
    public <T> void visitCtNewArray(CtNewArray<T> newArray) {

    }

    @Override
    public <T> void visitCtNewClass(CtNewClass<T> newClass) {

    }

    @Override
    public <T, A extends T> void visitCtOperatorAssignement(CtOperatorAssignment<T, A> assignment) {
        this.visitCtAssignment(assignment);
    }

    @Override
    public void visitCtPackage(CtPackage ctPackage) {

    }

    @Override
    public void visitCtPackageReference(CtPackageReference reference) {

    }

    @Override
    public <T> void visitCtParameter(CtParameter<T> parameter) {

    }

    @Override
    public <T> void visitCtParameterReference(CtParameterReference<T> reference) {

    }

    @Override
    public <R> void visitCtReturn(CtReturn<R> returnStatement) {

    }

    @Override
    public <R> void visitCtStatementList(CtStatementList<R> statements) {
        for (CtStatement s : statements) s.accept(this);
    }

    @Override
    public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
        for (CtCase ctCase : switchStatement.getCases()) ctCase.accept(this);
    }

    @Override
    public void visitCtSynchronized(CtSynchronized synchro) {

    }

    @Override
    public void visitCtThrow(CtThrow throwStatement) {
        throwStatement.getThrownExpression().accept(this);
    }

    @Override
    public void visitCtTry(CtTry tryBlock) {
        tryBlock.getBody().accept(this);
        for (CtCatch ctCatch : tryBlock.getCatchers()) ctCatch.accept(this);
        tryBlock.getFinalizer().accept(this);
        for (CtLocalVariable v : tryBlock.getResources()) v.accept(this);
    }

    @Override
    public void visitCtTypeParameter(CtTypeParameter typeParameter) {

    }

    @Override
    public void visitCtTypeParameterReference(CtTypeParameterReference ref) {

    }

    @Override
    public <T> void visitCtTypeReference(CtTypeReference<T> reference) {

    }

    @Override
    public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {

    }


    @Override
    public void visitCtWhile(CtWhile whileLoop) {
        whileLoop.getLoopingExpression().accept(this);
        whileLoop.getBody().accept(this);
    }

    @Override
    public <T> void visitCtAnnotationFieldAccess(CtAnnotationFieldAccess<T> annotationFieldAccess) {

    }
}
