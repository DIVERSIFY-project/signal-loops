package fr.inria.diverse.signalloops.detectors.logic;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.CtVisitor;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtBreakImpl;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Created by marodrig on 08/10/2015.
 */
public class SignalLoopVisitors implements CtVisitor {

    protected List<CtVariableAccess> accessOfExpression(CtExpression expression) {
        //Detect all variables in the index expression of the array
        return expression.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
    }


    @Override
    public void visitCtAnonymousExecutable(CtAnonymousExecutable anonymousExec) {
        anonymousExec.getBody().accept(this);
    }


    @Override
    public <T> void visitCtAssert(CtAssert<T> asserted) {
        asserted.getAssertExpression().accept(this);
        asserted.getExpression().accept(this);
    }

    @Override
    public <S> void visitCtCase(CtCase<S> caseStatement) {
        for (CtStatement s : caseStatement.getStatements()) s.accept(this);
    }

    @Override
    public <R> void visitCtBlock(CtBlock<R> block) {
        for (CtStatement s : block.getStatements())
            s.accept(this);
    }


    protected  <T> List<CtVariableAccess> accessOfLeftExpression(CtExpression<T> assigned) {
        if (assigned instanceof CtArrayAccess) return accessOfLeftExpression(((CtArrayAccess) assigned).getTarget());
        return accessOfExpression(assigned);
    }

    @Override
    public void visitCtCatch(CtCatch catchBlock) {
        catchBlock.getBody().accept(this);
    }

    @Override
    public <T> void visitCtConditional(CtConditional<T> conditional) {
        conditional.getCondition().accept(this);
        conditional.getThenExpression().accept(this);
        if (conditional.getElseExpression() != null) conditional.getElseExpression().accept(this);
    }

    @Override
    public void visitCtDo(CtDo doLoop) {
        doLoop.getLoopingExpression().accept(this);
        doLoop.getBody().accept(this);
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
    public <R> void visitCtStatementList(CtStatementList<R> statements) {
        for (CtStatement s : statements) s.accept(this);
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
    public <T, E extends CtExpression<?>> void visitCtArrayAccess(CtArrayAccess<T, E> arrayAccess) {

    }

    @Override
    public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> reference) {

    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {

    }

    @Override
    public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {

    }

    @Override
    public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
        for (CtCase ctCase : switchStatement.getCases()) ctCase.accept(this);
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
    public void visitCtWhile(CtWhile whileLoop) {
        whileLoop.getLoopingExpression().accept(this);
        whileLoop.getBody().accept(this);
    }

    @Override
    public void visitCtBreak(CtBreak breakStatement) {

    }

    @Override
    public <T> void visitCtClass(CtClass<T> ctClass) {

    }

    @Override
    public <T> void visitCtConstructor(CtConstructor<T> c) {

    }

    @Override
    public void visitCtContinue(CtContinue continueStatement) {

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
    public <T> void visitCtInterface(CtInterface<T> intrface) {

    }

    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {

    }

    @Override
    public <T> void visitCtLiteral(CtLiteral<T> literal) {

    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {

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
    public void visitCtSynchronized(CtSynchronized synchro) {

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
    public <T> void visitCtVariableAccess(CtVariableAccess<T> variableAccess) {

    }



    @Override
    public <T> void visitCtAnnotationFieldAccess(CtAnnotationFieldAccess<T> annotationFieldAccess) {

    }


}
