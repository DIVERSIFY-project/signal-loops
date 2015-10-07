package fr.inria.diverse.signalloops.detectors.logic;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.CtVisitor;

import java.lang.annotation.Annotation;

/**
 * Creates a new block by visiting an old one maintaining only the immutable statements
 *
 * Created by marodrig on 07/10/2015.
 */
public class DegradedBlockVisitor implements CtVisitor {
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

    }

    @Override
    public <T, E extends CtExpression<?>> void visitCtArrayAccess(CtArrayAccess<T, E> arrayAccess) {

    }

    @Override
    public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> reference) {

    }

    @Override
    public <T> void visitCtAssert(CtAssert<T> asserted) {

    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {

    }

    @Override
    public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {

    }

    @Override
    public <R> void visitCtBlock(CtBlock<R> block) {

    }

    @Override
    public void visitCtBreak(CtBreak breakStatement) {

    }

    @Override
    public <S> void visitCtCase(CtCase<S> caseStatement) {

    }

    @Override
    public void visitCtCatch(CtCatch catchBlock) {

    }

    @Override
    public <T> void visitCtClass(CtClass<T> ctClass) {

    }

    @Override
    public <T> void visitCtConditional(CtConditional<T> conditional) {

    }

    @Override
    public <T> void visitCtConstructor(CtConstructor<T> c) {

    }

    @Override
    public void visitCtContinue(CtContinue continueStatement) {

    }

    @Override
    public void visitCtDo(CtDo doLoop) {

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

    }

    @Override
    public void visitCtForEach(CtForEach foreach) {

    }

    @Override
    public void visitCtIf(CtIf ifElement) {

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
    public <R> void visitCtStatementList(CtStatementList<R> statements) {

    }

    @Override
    public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {

    }

    @Override
    public void visitCtSynchronized(CtSynchronized synchro) {

    }

    @Override
    public void visitCtThrow(CtThrow throwStatement) {

    }

    @Override
    public void visitCtTry(CtTry tryBlock) {

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
    public void visitCtWhile(CtWhile whileLoop) {

    }

    @Override
    public <T> void visitCtAnnotationFieldAccess(CtAnnotationFieldAccess<T> annotationFieldAccess) {

    }
}
