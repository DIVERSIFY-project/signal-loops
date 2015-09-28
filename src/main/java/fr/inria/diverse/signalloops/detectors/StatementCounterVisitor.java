package fr.inria.diverse.signalloops.detectors;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.CtVisitor;

import java.lang.annotation.Annotation;

/**
 * Visitor to count the number of statements within one block
 *
 * Created by marodrig on 02/09/2015.
 */
public class StatementCounterVisitor implements CtVisitor {

    int totalCount = 0;
    int count = 0;

    //Number of statements within the up and down
    public int getSkipped() {
        return skipped;
    }

    //Total number of statements
    public int getTotalCount() {
        return totalCount;
    }

    private int skipped = 0;

    boolean elementVisited = false;

    public void scan(CtBlock block, int up, int down) {
        up = up == -1 ? 0 : up;
        for (int j = 0; j < block.getStatements().size(); j++) {
            count = 0;
            block.getStatement(j).accept(this);
            if (j >= up && j <= down) skipped += count;
            totalCount += count;
        }
    }

    @Override
    public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtCodeSnippetExpression(CtCodeSnippetExpression<T> expression) {
        elementVisited = true;
    }

    @Override
    public void visitCtCodeSnippetStatement(CtCodeSnippetStatement statement) {
        elementVisited = true;
    }

    @Override
    public <A extends Annotation> void visitCtAnnotationType(CtAnnotationType<A> annotationType) {
        elementVisited = true;
    }

    @Override
    public void visitCtAnonymousExecutable(CtAnonymousExecutable anonymousExec) {
        elementVisited = true;
    }

    @Override
    public <T, E extends CtExpression<?>> void visitCtArrayAccess(CtArrayAccess<T, E> arrayAccess) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> reference) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtAssert(CtAssert<T> asserted) {
        count++;
    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {
        count++;
    }

    @Override
    public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
        elementVisited = true;
    }


    @Override
    public <R> void visitCtBlock(CtBlock<R> block) {
        for (CtStatement s : block.getStatements()) {
            s.accept(this);
            if (elementVisited) count++;
            elementVisited = false;
        }
    }

    @Override
    public void visitCtBreak(CtBreak breakStatement) {
        //breakStatement
        elementVisited = true;
    }


    @Override
    public <S> void visitCtCase(CtCase<S> caseStatement) {
        for (CtStatement s : caseStatement.getStatements()) {
            s.accept(this);
            if (elementVisited) count++;
            elementVisited = false;
        }
    }

    @Override
    public <R> void visitCtStatementList(CtStatementList<R> statements) {
        for (CtStatement s : statements.getStatements()) {
            s.accept(this);
            if (elementVisited) count++;
            elementVisited = false;
        }
    }

    @Override
    public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
        for (CtCase c : switchStatement.getCases()) {
            c.accept(this);
            if (elementVisited) count++;
            elementVisited = false;
        }
    }

    @Override
    public void visitCtCatch(CtCatch catchBlock) {
        catchBlock.getBody().accept(this);
        elementVisited = false;
    }

    @Override
    public <T> void visitCtClass(CtClass<T> ctClass) {

    }

    @Override
    public <T> void visitCtConditional(CtConditional<T> conditional) {
        count++;
    }

    @Override
    public <T> void visitCtConstructor(CtConstructor<T> c) {
        elementVisited = true;
    }

    @Override
    public void visitCtContinue(CtContinue continueStatement) {
        count++;
    }

    @Override
    public void visitCtDo(CtDo doLoop) {
        doLoop.getBody().accept(this);
        elementVisited = false;
    }

    @Override
    public <T extends Enum<?>> void visitCtEnum(CtEnum<T> ctEnum) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtExecutableReference(CtExecutableReference<T> reference) {
        count++;
    }

    @Override
    public <T> void visitCtField(CtField<T> f) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtTargetedAccess(CtTargetedAccess<T> targetedAccess) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtFieldReference(CtFieldReference<T> reference) {
        elementVisited = true;
    }

    @Override
    public void visitCtFor(CtFor forLoop) {
        forLoop.getBody().accept(this);
        elementVisited = false;
    }

    @Override
    public void visitCtForEach(CtForEach foreach) {
        foreach.getBody().accept(this);
        elementVisited = false;
    }

    @Override
    public void visitCtIf(CtIf ifElement) {
        count++; //The expression
        ifElement.getThenStatement().accept(this);
        if (ifElement.getElseStatement() != null) ifElement.getElseStatement().accept(this);
        elementVisited = false;
    }

    @Override
    public <T> void visitCtInterface(CtInterface<T> intrface) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        count++;
    }

    @Override
    public <T> void visitCtLiteral(CtLiteral<T> literal) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        count++;
    }

    @Override
    public <T> void visitCtLocalVariableReference(CtLocalVariableReference<T> reference) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtMethod(CtMethod<T> m) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtNewArray(CtNewArray<T> newArray) {
        count++;
    }

    @Override
    public <T> void visitCtNewClass(CtNewClass<T> newClass) {
        elementVisited = true;
    }

    @Override
    public <T, A extends T> void visitCtOperatorAssignement(CtOperatorAssignment<T, A> assignment) {
        elementVisited = true;
    }

    @Override
    public void visitCtPackage(CtPackage ctPackage) {
        elementVisited = true;
    }

    @Override
    public void visitCtPackageReference(CtPackageReference reference) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtParameter(CtParameter<T> parameter) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtParameterReference(CtParameterReference<T> reference) {
        elementVisited = true;
    }

    @Override
    public <R> void visitCtReturn(CtReturn<R> returnStatement) {
        count++;
    }


    @Override
    public void visitCtSynchronized(CtSynchronized synchro) {
        elementVisited = true;
    }

    @Override
    public void visitCtThrow(CtThrow throwStatement) {
        count++;
    }

    @Override
    public void visitCtTry(CtTry tryBlock) {
        if (tryBlock.getBody() != null) tryBlock.getBody().accept(this);
        elementVisited = false;

    }

    @Override
    public void visitCtTypeParameter(CtTypeParameter typeParameter) {
        elementVisited = true;
    }

    @Override
    public void visitCtTypeParameterReference(CtTypeParameterReference ref) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
        elementVisited = true;
    }

    @Override
    public <T> void visitCtVariableAccess(CtVariableAccess<T> variableAccess) {
        elementVisited = true;
    }

    @Override
    public void visitCtWhile(CtWhile whileLoop) {
        if (whileLoop.getBody() != null) whileLoop.getBody().accept(this);
        elementVisited = false;
    }

    @Override
    public <T> void visitCtAnnotationFieldAccess(CtAnnotationFieldAccess<T> annotationFieldAccess) {
        elementVisited = true;
    }
}
