package fr.inria.diverse.signalloops.dataflow;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.CtVisitor;

import java.lang.annotation.Annotation;
import java.util.List;

import static fr.inria.diverse.signalloops.dataflow.BranchKind.*;

/**
 *
 * Builds the control graph for a given snippet of code
 *
 * Created by marodrig on 13/10/2015.
 */
public class ForwardFlowBuilderVisitor implements CtVisitor {

    ControlFlowGraph result = new ControlFlowGraph(ControlFlowEdge.class);

    ControlFlowNode lastNode = null;

    public ControlFlowGraph getResult() {
        return result;
    }

    private void defaultAction(BranchKind kind, CtStatement st) {
        ControlFlowNode n = new ControlFlowNode(st, result, kind);
        tryAddEdge(lastNode, n);
        lastNode = n;
    }

    /**
     * Tries to add an edge. If source or target are not null and the vertex is unique
     *
     * @param source Source of the vertex
     * @param target Target of the vertex
     */
    private void tryAddEdge(ControlFlowNode source, ControlFlowNode target) {
        if (source != null && target != null && !result.containsEdge(source, target))
            result.addEdge(source, target);
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

    }

    @Override
    public <T, E extends CtExpression<?>> void visitCtArrayAccess(CtArrayAccess<T, E> arrayAccess) {

    }

    @Override
    public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> reference) {

    }

    @Override
    public <T> void visitCtAssert(CtAssert<T> asserted) {
        defaultAction(STATEMENT, asserted);
    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {
        defaultAction(STATEMENT, assignement);
    }

    @Override
    public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {

    }

    private void travelStatementList(ControlFlowNode firstNode, List<CtStatement> statements) {
        tryAddEdge(lastNode, firstNode);
        lastNode = firstNode;

        for (CtStatement s : statements) {
            ControlFlowNode before = lastNode;
            s.accept(this); // <- This should modify last node
            tryAddEdge(before, lastNode); //Probably the link is already added
        }
    }

    @Override
    public <R> void visitCtBlock(CtBlock<R> block) {
        ControlFlowNode begin = new ControlFlowNode(block, result, BLOCK_BEGIN);
        tryAddEdge(lastNode, begin);
        lastNode = begin;

        travelStatementList(begin, block.getStatements());

        ControlFlowNode end = new ControlFlowNode(block, result, BLOCK_END);
        tryAddEdge(lastNode, end);
        lastNode = end;
    }

    @Override
    public void visitCtBreak(CtBreak breakStatement) {
        //TODO: IMPLEMENT THIS!!!
        //breakStatement.
    }



    @Override
    public void visitCtCatch(CtCatch catchBlock) {

    }

    @Override
    public <T> void visitCtClass(CtClass<T> ctClass) {
        defaultAction(STATEMENT, ctClass);
    }

    @Override
    public <T> void visitCtConditional(CtConditional<T> conditional) {
        ControlFlowNode branch = new ControlFlowNode(conditional, result, BRANCH);
        tryAddEdge(lastNode, branch);

        ControlFlowNode convergenceNode = new ControlFlowNode(null, result, CONVERGE);
        conditional.getThenExpression().accept(this);
        tryAddEdge(lastNode, convergenceNode);

        conditional.getElseExpression().accept(this);
        tryAddEdge(lastNode, convergenceNode);
    }

    @Override
    public <T> void visitCtConstructor(CtConstructor<T> c) {

    }

    @Override
    public void visitCtContinue(CtContinue continueStatement) {
        //TODO: implement this
    }

    @Override
    public void visitCtDo(CtDo doLoop) {
        ControlFlowNode convergenceNode = new ControlFlowNode(null, result, CONVERGE);
        tryAddEdge(lastNode, convergenceNode);

        doLoop.getBody().accept(this);

        ControlFlowNode branch = new ControlFlowNode(doLoop, result, BRANCH);
        tryAddEdge(lastNode, branch);
        tryAddEdge(branch, convergenceNode);
        lastNode = branch;


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
        //Add the initialization code
        travelStatementList(lastNode, forLoop.getForInit());

        //Next the branch
        ControlFlowNode branch = new ControlFlowNode(forLoop.getExpression(), result, BRANCH);
        tryAddEdge(lastNode, branch);

        //Body
        lastNode = branch;
        forLoop.getBody().accept(this);

        //Append the update at the end
        travelStatementList(lastNode, forLoop.getForUpdate());

        //Link to the branch
        tryAddEdge(lastNode, branch);

        //Add a convergence node to quit the loop
        lastNode = new ControlFlowNode(null, result, CONVERGE);
        tryAddEdge(branch, lastNode);
    }

    @Override
    public void visitCtForEach(CtForEach foreach) {
        ControlFlowNode branch = new ControlFlowNode(foreach.getExpression(), result, BRANCH);
        tryAddEdge(lastNode, branch);

        //Body
        lastNode = branch;
        foreach.getBody().accept(this);

        tryAddEdge(lastNode, branch);

        //Exit node
        lastNode = new ControlFlowNode(null, result, CONVERGE);
        tryAddEdge(branch, lastNode);

    }

    @Override
    public void visitCtIf(CtIf ifElement) {
        ControlFlowNode branch = new ControlFlowNode(ifElement, result, BRANCH);
        tryAddEdge(lastNode, branch);

        ControlFlowNode convergenceNode = new ControlFlowNode(null, result, CONVERGE);
        if ( ifElement.getThenStatement() != null ) {
            ifElement.getThenStatement().accept(this);
            tryAddEdge(lastNode, convergenceNode);
        }

        if ( ifElement.getElseStatement() != null ) {
            ifElement.getElseStatement().accept(this);
            tryAddEdge(lastNode, convergenceNode);
            tryAddEdge(lastNode, convergenceNode);
        }
    }

    @Override
    public <T> void visitCtInterface(CtInterface<T> intrface) {

    }

    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        defaultAction(STATEMENT, invocation);
    }

    @Override
    public <T> void visitCtLiteral(CtLiteral<T> literal) {

    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        defaultAction(STATEMENT, localVariable);
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
        defaultAction(STATEMENT, assignment);
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
        defaultAction(STATEMENT, returnStatement);
    }

    @Override
    public <R> void visitCtStatementList(CtStatementList<R> statements) {

    }

    @Override
    public <S> void visitCtCase(CtCase<S> caseStatement) {
        travelStatementList(new ControlFlowNode(caseStatement, result, STATEMENT), caseStatement.getStatements());
    }

    @Override
    public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
        //Push the condition
        ControlFlowNode caseNode = new ControlFlowNode(switchStatement, result, BRANCH);
        tryAddEdge(lastNode, caseNode);

        //Create a convergence node for all the branches to converge after this
        ControlFlowNode convergenceNode = new ControlFlowNode(null, result, CONVERGE);
        for (CtStatement s : switchStatement.getCases()) {
            s.accept(this);
            tryAddEdge(lastNode, convergenceNode);
            lastNode = caseNode;
        }

        //Return as last node the convergence node
        lastNode = convergenceNode;
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
        ControlFlowNode convergenceNode = new ControlFlowNode(null, result, CONVERGE);
        ControlFlowNode branch = new ControlFlowNode(whileLoop, result, BRANCH);

        whileLoop.getBody().accept(this);

        tryAddEdge(lastNode, branch);

        tryAddEdge(branch, convergenceNode);
    }

    @Override
    public <T> void visitCtAnnotationFieldAccess(CtAnnotationFieldAccess<T> annotationFieldAccess) {

    }
}
