package fr.inria.diverse.signalloops.detectors;

import fr.inria.diverse.signalloops.detectors.logic.DefChainCycleDetectorVisitor;
import fr.inria.diverse.signalloops.detectors.logic.DegradedBlockVisitor;
import fr.inria.diverse.signalloops.detectors.logic.StatementCounterVisitor;
import fr.inria.diverse.signalloops.model.SignalLoop;
import fr.inria.diversify.syringe.detectors.LoopDetect;
import fr.inria.diversify.syringe.injectors.Injector;
import fr.inria.diversify.syringe.signature.DefaultSignature;
import org.apache.log4j.Logger;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import spoon.reflect.code.*;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtBlockImpl;

import java.util.*;

/**
 * A signal generator loop detector.
 * <p/>
 * A signal generator loops modifies an array who's index depends on the loop expression.
 * <p/>
 * The type of the array is a numeric one "float, double, int, etc..."
 * <p/>
 * Created by marodrig on 10/07/2015.
 */
public class SignalLoopDetector extends LoopDetect {

    private Logger log = Logger.getLogger(SignalLoopDetector.class);

    public static class SignalLoopSignature extends DefaultSignature {
        @Override
        public String getSignature(CtElement e) {
            String sig = super.getSignature(e);
            return "SIGNAL" + sig;
        }
    }

    public static class ApproximatedRatio {

        public ApproximatedRatio(int totalLines, int linesKept) {
            this.totalLines = totalLines;
            this.linesKept = linesKept;
        }

        public int getFixedStmnt() {
            return linesKept;
        }

        public int getTotalLines() {

            return totalLines;
        }

        int totalLines;
        int linesKept;
    }

    private static class SignalLoopParams {
        String arrayIndex = "";
        String arrayName = "";
        int statementIndex = -1;
        CtArrayAccess access = null;
    }

    private static final HashSet<String> numericTypes;

    static {
        numericTypes = new HashSet<String>();
        numericTypes.add(Integer.class.getCanonicalName());
        numericTypes.add(int.class.getCanonicalName());

        numericTypes.add(Byte.class.getCanonicalName());
        numericTypes.add(byte.class.getCanonicalName());

        numericTypes.add(Float.class.getCanonicalName());
        numericTypes.add(float.class.getCanonicalName());

        numericTypes.add(Double.class.getCanonicalName());
        numericTypes.add(double.class.getCanonicalName());

        numericTypes.add(Long.class.getCanonicalName());
        numericTypes.add(long.class.getCanonicalName());

        numericTypes.add(Short.class.getCanonicalName());
        numericTypes.add(short.class.getCanonicalName());
    }


    private SignalLoop lastLoop;

    public SignalLoop getLastLoop() {
        return lastLoop;
    }

    //Injectors to inject just before the loop
    public static String SIGNAL_BEFORE_KEY = "@SIGNAL.Loop.BEFORE@";

    //Injector to inject just after the loop
    public static String SIGNAL_AFTER_KEY = "@SIGNAL.Loop.AFTER@";

    public static String ACCESS_KEY = "@ARRAY_ACCESS@";

    //Injectos for signal loops
    private Collection<Injector> signalBeforeInjectors;
    private Collection<Injector> signalAfterInjectors;

    //Injector for after the access of the signal array
    private Collection<Injector> accessInjectors;

    private HashMap<Integer, SignalLoop> signalLoops;

    public HashMap<Integer, SignalLoop> getSignalLoops() {
        return signalLoops;
    }
    //private boolean moreLoopsRemain;

    private int loopId = -1;

    private boolean processAllLoops = false;

    private boolean idFound = false;

    /**
     * Indicates whether more signal loops remain to be found
     *
     * @return True if more loops remains, false otherwise
     */
    public boolean getMoreLoopsRemain() {
        //If we found a previous loop then it must remain more.
        //If the last index found is greater than the loop and we haven´t found the ID,
        //it mean that there are no more loop
        return idFound || getIdMap().getLastIndex() > loopId;
    }

    /**
     * ID of the specific loop we want to find. -1 if none special loop is needed or if all are to be finded.
     * <p/>
     * This is usefull to process one loop at a time
     *
     * @param loopId
     */
    public void setLoopId(int loopId) {
        this.loopId = loopId;
    }

    public int getLoopId() {
        return loopId;
    }

    /**
     * Number of signal loops detected
     *
     * @return
     */
    public int getSignalElementsDetected() {
        return signalElementsDetected;
    }

    private int signalElementsDetected = 0;

    public boolean isDegrade() {
        return degrade;
    }

    public void setDegrade(boolean degrade) {
        this.degrade = degrade;
    }

    private boolean degrade;

    public boolean isPrepareMicroBenchMark() {
        return prepareMicroBenchMark;
    }

    /**
     * Indicate whether the processor should prepare a microbenchmark when a signal loop is detected
     *
     * @param prepareMicroBenchMark
     */
    public void setPrepareMicroBenchMark(boolean prepareMicroBenchMark) {
        this.prepareMicroBenchMark = prepareMicroBenchMark;
    }

    private boolean prepareMicroBenchMark;


    /**
     * Signature of signal loops
     */
    SignalLoopSignature signalSignature = new SignalLoopSignature();

    /**
     * Signature of regular loops
     */
    DefaultSignature defaultSignature = new DefaultSignature();


    public SignalLoopDetector() {
        super();
        signalLoops = new HashMap<Integer, SignalLoop>();
        idFound = false;
    }

    @Override
    public void collectInjectors(AbstractMap<String, Collection<Injector>> injectors) {
        super.collectInjectors(injectors);
        signalBeforeInjectors =
                injectors.containsKey(SIGNAL_BEFORE_KEY) ? injectors.get(SIGNAL_BEFORE_KEY) : new ArrayList<Injector>();
        signalAfterInjectors =
                injectors.containsKey(SIGNAL_AFTER_KEY) ? injectors.get(SIGNAL_AFTER_KEY) : new ArrayList<Injector>();
        accessInjectors =
                injectors.containsKey(ACCESS_KEY) ? injectors.get(ACCESS_KEY) : new ArrayList<Injector>();
    }

    @Override
    public boolean isToBeProcessed(CtLoop candidate) {
        return super.isToBeProcessed(candidate) && (processAllLoops || !idFound);
    }

    private void updateIdFound(String signature) {
        int thisLoopId = getIdMap().get(signature);
        //Skip loops
        idFound = thisLoopId >= loopId;
    }

    /**
     * Automatic approximation of loops for DSP applications
     * <p/>
     * This processor finds:
     * 1. If the loop is a signal loop. A loop is a signal loop if contains a signal array
     * 2. The approximable part of the loop. An approximable part of the loop is such as:
     * a. Contains the signal array.
     * b. Does not contains (assignments/passing as ref) of variables declared outside the loop
     *
     * @param loop
     */
    @Override
    public void process(CtLoop loop) {

        //Reset the last loop found
        lastLoop = null;

        //TODO: Have into consideration the method calls modifying stuff as reference parameters

        //Don't process empty loops
        if (!(loop.getBody() instanceof CtBlock)) return;

        //Finds the signal array within the loop (if any)
        SignalLoopParams signalParams = findSignalArray(loop);
        int signalStmntIndex = signalParams.statementIndex; // -1 if no signal array

        //Vars declarations non local to the loop
        Set<CtVariableReference> localToLoop = new HashSet<CtVariableReference>();

        CtBlock loopBody = (CtBlock) loop.getBody();
        //If this is an array loop
        if (loop instanceof CtFor && signalStmntIndex != -1 && loopBody.getStatements().size() > 0) {

            CtFor ctFor = (CtFor) loop;

            //Process the before and after normally
            setSignature(signalSignature);
            //Set id of the element to the id map
            String signature = getSignatureFromElement(loop);
            putSignatureIntoData(signature);

            //Update if we have updated the ids
            updateIdFound(signature);

            //if (block.getStatements().size() > 0) {
            log.info("SIGNAL LOOP ID: " + getIdMap().get(getSignatureFromElement(loop)));
            log.info("Pos: " + loop.getPosition().toString());
            log.info(loop);
            //}

            //If we are in a single-shoot mode, return if this is not the loop we are looking for
            if (signalElementsDetected > 10000000) return; //A switch in case
            if (!processAllLoops && loopId != getIdMap().get(signature)) return;


            log.info("Start loop instrumentation");

            String lastLoopPosition = loop.getPosition().getCompilationUnit().getMainType().getQualifiedName() +
                    ":" + loop.getPosition().getLine();

            idFound = true;
            //Count the number of signal loops detected
            signalElementsDetected++;

            CycleDetector<CtVariableReference, DefaultEdge> cycleDetector =
                    new DefChainCycleDetectorVisitor().buildDetector(loopBody, localToLoop);

            //Build a new degraded block
            DegradedBlockVisitor degradeVisitor = new DegradedBlockVisitor();
            degradeVisitor.setCycleDetector(cycleDetector);
            degradeVisitor.setLocalVariables(localToLoop);
            CtStatement clonedBody = loop.getFactory().Core().clone(loop.getBody());
            clonedBody.setParent(loop);
            try {
                clonedBody.accept(degradeVisitor);
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error("Unable to process " + loop);
                e.printStackTrace();
                return;
            }

            //appendDegradedBlock(loop.getBody(), clonedBody);
/*
            //Inmutable detector BEGIN
            int up = signalStmntIndex - 1; //Find the Upper avoidable frontier of the loop
            if (up > 0) {
                while (up >= 0 && !recursive(loopBody.getStatement(up), localToLoop, cycleDetector)) up--;
            } else up = -1;

            //Non avoidable statements before the array assign
            StringBuilder recUp = new StringBuilder();
            for (int i = 0; i <= up; i++) printStatement(loopBody.getStatement(i), recUp);

            int hi = loopBody.getStatements().size() - 1;
            int down = signalStmntIndex + 1; //Find the Lower avoidable frontier of the loop
            if (down < hi) {
                while (down <= hi && !recursive(loopBody.getStatement(down), localToLoop, cycleDetector)) down++;
                if (down > hi) down = hi;
            } else down = hi;


            StringBuilder recDown = new StringBuilder();
            for (int i = down + 1; i < loopBody.getStatements().size(); i++)
                printStatement(loopBody.getStatement(i), recDown);

            //Calculate the approximate ratio of the loop
            ApproximatedRatio ratio = countStatements(loopBody, up, down);
            */
            //INMUTABLE DETECTION END

            log.info("--------------------------------------");
            if (loopBody.getStatements().size() > 1) {
                log.info("Index:       " + signalStmntIndex);
                /*
                log.info("Up   :       " + up);
                log.info("Down :       " + down);
                log.info("Total Stmnt : " + ratio.getTotalLines());
                log.info("Kept  Stmnt : " + ratio.getFixedStmnt());
                */
            } else {
                log.info("Single line loop");
            }
            SignalLoop signalLoop = new SignalLoop();
            signalLoop.setId(getIdMap().get(getSignatureFromElement(loop)));
            signalLoop.setCode(ctFor.toString());
            //signalLoop.setTotalStmnt(ratio.getTotalLines());
            //signalLoop.setFixedStmnt(ratio.getFixedStmnt());
            signalLoop.setPosition(lastLoopPosition);
            //signalLoop.setUpFix(up);
            //signalLoop.setDownFix(down);
            signalLoop.setSignalLoop(true);
            signalLoop.setLoop(loop);
            signalLoop.setSignalArray(signalParams.access);
            if (prepareMicroBenchMark) {
                new LoopInputsDetector().prepareMicrobenchmarkData(signalLoop);
            }

            lastLoop = signalLoop;
            signalLoops.put(getIdMap().get(getSignatureFromElement(loop)), signalLoop);

            data.getParams().put("index_expr", signalParams.arrayIndex);
            data.getParams().put("array", signalParams.arrayName);
            data.getParams().put("recursive_up", prettyPrintBody(clonedBody));
            data.getParams().put("recursive_down", "/*DOWN*/");
            data.getParams().put("loop_condition", getLoopExpression(loop).toString());

            StringBuilder sbUpdate = new StringBuilder();
            for (CtStatement st : ctFor.getForUpdate()) {
                sbUpdate.append(st.toString() + ";\n");
            }
            data.getParams().put("loop_increment", sbUpdate.toString());


            SourcePosition sp = loopBody.getStatement(0).getPosition();
            int indexSp = sp.getSourceStart();
            CompilationUnit cu = sp.getCompilationUnit();
            //BEGIN LOOP BLOCK
            String snippet = getSnippet(beginInjectors, loop, data);
            cu.addSourceCodeFragment(new SourceCodeFragment(indexSp, snippet, 0));

            //AFTER LOOP BLOCK
            sp = loopBody.getLastStatement().getPosition();
            indexSp = sp.getSourceEnd() + 2;
            snippet = getSnippet(endInjectors, loop, data);
            if (degrade) cu.addSourceCodeFragment(new SourceCodeFragment(indexSp, snippet, 0));

            signalLoop.setDegradedSnippet(snippet);


            //ARRAY ACCESS
            data.getParams().put("array_access", signalParams.access.toString());
            sp = loopBody.getStatement(signalParams.statementIndex).getPosition();
            indexSp = sp.getSourceEnd();
            snippet = getSnippet(accessInjectors, loop, data);
            cu.addSourceCodeFragment(new SourceCodeFragment(indexSp, snippet, 0));

            processBeforeAfter(loop, signalBeforeInjectors, signalAfterInjectors);

        } else if (processAllLoops) {
            //Process the before and after normally
            setSignature(defaultSignature);
            //Process the before and after
            processBeforeAfter(loop, beforeInjectors, afterInjectors);
        } else {
            //Process the before and after normally
            setSignature(defaultSignature);
            //Set id of the element to the id map
            String signature = getSignatureFromElement(loop);
            putSignatureIntoData(signature);
            elementsDetected++;

            //Update if we have updated the ids
            updateIdFound(signature);
        }
    }


    private String prettyPrintBody(CtStatement clonedBody) {
        String result = "";
        if ( clonedBody instanceof CtBlock ) {
            CtBlock block = (CtBlock) clonedBody;
            for (int i = 0; i < block.getStatements().size(); i++ ) {
                if ( block.getStatement(i) instanceof CtBlock ) result += prettyPrintBody(block.getStatement(i));
                else result += block.getStatement(i).toString() + ";\n";
            }
        } else result = clonedBody.toString();
        return result;
    }

    /**
     * Appends the cloned body to the body
     *
     * @param body
     * @param clonedBody
     */
    private void appendDegradedBlock(CtStatement body, CtStatement clonedBody) {

        CtBlock destBlock;
        if (body instanceof CtBlock) {
            destBlock = (CtBlock) body;
        } else {
            destBlock = new CtBlockImpl();
            destBlock.addStatement(body);
            destBlock.setParent(body.getParent());
            body.setParent(destBlock);
        }

        if (clonedBody instanceof CtBlock) {
            CtBlock srcBlock = (CtBlock) clonedBody;
            for (int i = 0; i < srcBlock.getStatements().size(); i++) {
                destBlock.addStatement(srcBlock.getStatement(i));
            }
        } else destBlock.addStatement(clonedBody);
    }


    /**
     * Counts how many statements can be avoided in a block
     *
     * @param b    Block to count
     * @param up   Up fist level statement of the avoidable frontier
     * @param down down first level statement of the avoidable frontier
     * @return
     */
    private ApproximatedRatio countStatements(CtBlock b, int up, int down) {
        StatementCounterVisitor v = new StatementCounterVisitor();
        v.scan(b, up, down);
        return new ApproximatedRatio(v.getTotalCount(), v.getTotalCount() - v.getSkipped());
    }


    /**
     * Indicate if the statement contains a recursive expression
     * <p/>
     * A recursive expression is an expression that assigns value to a variable in the left side
     * using that variable also in the right side, like this:
     * <p/>
     * a = a * b
     *
     * or like this:
     * c = a * 2
     * a = c + b
     *
     * <p/>
     * Also, all unary operators and are recursive:
     * a--;
     * a++;
     *
     * @param statement Statement to check whether is a recursive expression
     * @return True if it is a recursive expression
     */
    private boolean recursive(CtStatement statement, Set<CtVariableReference> localToLoop,
                              CycleDetector<CtVariableReference, DefaultEdge> cycleDetector) {

        if (statement instanceof CtAssignment) {
            CtAssignment e = (CtAssignment) statement;
            List<CtVariableAccess> left = accessOfExpression(e.getAssigned());
            for (CtVariableAccess access : left) {
                CtVariableReference ref = access.getVariable();
                try {
                    if (!localToLoop.contains(ref) && cycleDetector.detectCyclesContainingVertex(ref)) {
                        return true;
                    }
                } catch (IllegalArgumentException ex) {
                    continue;
                }
            }
        }

        //Handling unary operators
        for (CtUnaryOperator op :
                statement.getElements(new TypeFilter<CtUnaryOperator>(CtUnaryOperator.class))) {
            for (CtVariableAccess a : accessOfExpression(op)) {
                //Add cyclic dependencies to external variables
                if (!localToLoop.contains(a.getVariable())) return true;
            }
        }

        //Handling operators assignment
        for (CtOperatorAssignment op :
                statement.getElements(new TypeFilter<CtOperatorAssignment>(CtOperatorAssignment.class))) {
            for (CtVariableAccess a : accessOfExpression(op.getAssigned())) {
                //Add cyclic dependencies
                if (!localToLoop.contains(a.getVariable())) return true;
            }
        }

        return false;
    }


    private List<CtVariableAccess> accessOfExpression(CtExpression expression) {
        //Detect all variables in the index expression of the array
        return expression.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
    }

    /**
     * Indicate if the primitive type of the array (type of the 1D array) is of numerical type
     *
     * @param type: type of the array
     * @return Boolean if the access is of primitive type numeric
     */
    private boolean isNumericPrimitiveType(CtTypeReference type) {
        if (type instanceof CtArrayTypeReference) {
            return isNumericPrimitiveType(((CtArrayTypeReference) type).getComponentType());
        }
        return numericTypes.contains(type.getQualifiedName());
    }

    private CtExpression getLoopExpression(CtLoop element) {
        if (element instanceof CtWhile) {
            return ((CtWhile) element).getLoopingExpression();
        } else if (element instanceof CtFor) {
            return ((CtFor) element).getExpression();
        } else if (element instanceof CtForEach) {
            return ((CtForEach) element).getExpression();
        } else if (element instanceof CtDo) {
            return ((CtDo) element).getLoopingExpression();
        }
        return null;
    }

    /**
     * Indicates if the access is a signal array.
     * <p/>
     * A signal array is an array of numeric primitive data who's index can be connected to
     * the expression of the loop in a def-use data flow
     * <p/>
     * Example:
     * <p/>
     * for (int i = 0; i < size; i++) {
     * //Do stuff
     * // ....
     * numericArray[i] = someCalculation();
     * }
     *
     * @param arrayAccess Array access for what we want to know if it is a signal array
     * @param element
     * @return True if it is a numerical array
     */
    private boolean isSignalArray(CtArrayAccess arrayAccess, CtLoop element) {

        //Detect if the array is of numerical type
        if (isNumericPrimitiveType(arrayAccess.getType())) {
            //Detect all variables in the index expression of the array
            List<CtVariableAccess> indexAccesses = accessOfExpression(arrayAccess.getIndexExpression());

            //Detect all variables in the loop expression
            CtExpression exp = getLoopExpression(element);
            List<CtVariableAccess> loopExpAccesses = accessOfExpression(exp);

            //See if it is directly relate to the loop expression.
            //In the future calculate the def-use data flow from the index to the loop expression
            for (CtVariableAccess varAccess : indexAccesses) {
                for (CtVariableAccess a : loopExpAccesses) {
                    if (a.getVariable().equals(varAccess.getVariable())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isSignalLoop(CtLoop loop) {
        SignalLoopParams signalParams = findSignalArray(loop);
        return signalParams.statementIndex != -1;
    }

    /**
     * Finds the signal array within the loop.
     *
     * @param loop Loop being ispected
     * @return The index of the signal array in the list of statements (if any), -1 otherwise.
     */
    private SignalLoopParams findSignalArray(CtLoop loop) {
        //Detect array assignment
        if (loop.getBody() instanceof CtBlock) {
            CtBlock block = (CtBlock) loop.getBody();
            for (int i = 0; i < block.getStatements().size(); i++) {
                CtStatement st = block.getStatement(i);
                if (st instanceof CtAssignment && !(st instanceof CtOperatorAssignment)) {
                    CtExpression left = ((CtAssignment) st).getAssigned();
                    if (left instanceof CtArrayAccess && isSignalArray((CtArrayAccess) left, loop)) {
                        CtArrayAccess arrayAccess = (CtArrayAccess) left;
                        SignalLoopParams params = new SignalLoopParams();
                        params.statementIndex = i;
                        params.arrayIndex = arrayAccess.getIndexExpression().toString();
                        params.arrayName = arrayAccess.getTarget().toString();
                        params.access = arrayAccess;
                        return params;
                    }
                }
            }
        }
        return new SignalLoopParams();
    }

    private void printStatement(CtStatement s, StringBuilder sb) {
        if (s instanceof CtLocalVariable) {
            if (((CtLocalVariable) s).getDefaultExpression() != null) {
                sb.append(((CtLocalVariable) s).getReference().toString()).
                        append("=").
                        append(((CtLocalVariable) s).getDefaultExpression()).append(";");
            }
        } else sb.append(s.toString()).append(";\n");
    }

    /**
     * Indicates if all loops are to be processed, whether they are signals or not.
     */
    public boolean isProcessAllLoops() {
        return processAllLoops;
    }

    /**
     * Indicates if all loops are to be processed, whether they are signals or not.
     *
     * @param processAllLoops
     */
    public void setProcessAllLoops(boolean processAllLoops) {
        this.processAllLoops = processAllLoops;
    }

    /**
     * Indicate if the detector must find avoidable statements
     * <p/>
     * Avoidable statements are those that can be skipped without risk for the application
     *
     * @return True if must find avoidable, false otherwise
     */
    /*
    public boolean getFindAvoidable() {
        return findAvoidable;
    }*/

    /**
     * Indicate if the detector must find avoidable statements
     * <p/>
     * Avoidable statements are those that can be skipped without risk for the application
     */
    /*
    public void setFindAvoidable(boolean findAvoidable) {
        this.findAvoidable = findAvoidable;
    }*/

}
