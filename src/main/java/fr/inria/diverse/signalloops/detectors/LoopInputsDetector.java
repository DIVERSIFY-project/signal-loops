package fr.inria.diverse.signalloops.detectors;

import fr.inria.diverse.signalloops.model.SignalLoop;
import fr.inria.diversify.syringe.detectors.DetectionData;
import fr.inria.diversify.syringe.detectors.Detector;
import fr.inria.diversify.syringe.injectors.Injector;
import spoon.reflect.code.*;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

/**
 * Class to detect the input of a particular loop
 * <p/>
 * Created by marodrig on 15/09/2015.
 */
public class LoopInputsDetector extends Detector<CtLoop> {


    //Injectors to inject just before the loop
    public static String INPUTS = "@LoopInputs.BEFORE@";

    public static final String END_INPUTS = "@@END_INPUTS@@";

    /**
     * Injectors to inject before the loop block
     */
    protected Collection<Injector> inputInjectors;


    private Collection<Injector> degradationInjectors;

    private List<SignalLoop> results = new ArrayList<SignalLoop>();

    private Collection<Injector> endInjectors;


    int loopIndex = 0;
    private SignalLoop signalLoop;

    public List<SignalLoop> getResults() {
        if (results == null) results = new ArrayList<SignalLoop>();
        return results;
    }

    /**
     * One var can be pointed by multiples accesses. This method leaves only one access per variable in the list
     *
     * @return
     */
    private List<CtVariableAccess> cleanRepeatedAccesses(List<CtVariableAccess> allAccess) {
        //Have only one access per variable
        HashSet<CtVariable> variables = new HashSet<CtVariable>();
        List<CtVariableAccess> access = new ArrayList<CtVariableAccess>();
        for (CtVariableAccess a : allAccess) {
            //if (!isInitialized(a, loop)) continue;
            if (isFieldOfPrimitiveArray(a)) continue;
            if (!variables.contains(a.getVariable().getDeclaration())) {
                variables.add(a.getVariable().getDeclaration());
                access.add(a);
            }
        }
        variables.clear();
        return access;
    }


    /**
     * Preprares the data needed for build a microbenchark of the loop.
     *
     * @return
     */
    public boolean prepareMicrobenchmarkData(SignalLoop inputs) {
        //Check some preconditions needed for the processor to run
        //1. The loop is a signal loop
        //2. All invocations within the body are statics

        if (!checkPreconditions(inputs)) return false;

        CtLoop loop = inputs.getLoop();
        //All variable access made inside the body
        List<CtVariableAccess> access =
                loop.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
        access = cleanRepeatedAccesses(access);

        //Check more preconditions for the processor to run
        if (!allVariablesArePrimitive(access)) return false;

        System.out.println("PRECONDITIONS MEET");
        elementsDetected++;

        inputs.setLoop(loop);
        inputs.setAccesses(access);
        inputs.setMicroBenchMarkName(loop.getPosition().getCompilationUnit().
                getMainType().getQualifiedName().replace(".", "_") + "_" + loop.getPosition().getLine());
        for (CtVariableAccess a : access) {
            if (isInitialized(a, loop)) inputs.getInitialized().add(a);
        }
        getResults().add(inputs);

        System.out.println("*****************************");
        System.out.println("LOOP:" + loopIndex + ". POS: " + loop.getPosition().toString());
        System.out.println("*****************************");
        System.out.println(loop.toString());

        //All local variables inside the body
        List<CtLocalVariable> localVars =
                loop.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class));

        //Build the injection of each variable
        //Add to the injection only initialized and Non-local variable
        for (CtVariableAccess a : access) {
            if ( inputs.getInitialized().contains(a) && isLocalVariable(a, localVars)) {
                inputs.getInitialized().remove(a);
            }
        }
        return true;
    }

    @Override
    public void process(CtLoop loop) {

        loopIndex++;

        SignalLoop input = getSignalLoop();
        input.setLoop(loop);
        if (!prepareMicrobenchmarkData(input)) return;

        //
        StringBuilder sb = new StringBuilder(buildSnippet(input, inputInjectors));
        //Build the injection at the end
        String snippet = getSnippet(endInjectors, loop, data);
        if (snippet != null && !snippet.isEmpty()) sb.append(snippet);

        //Puts the signature in the ID
        String signature = getSignatureFromElement(loop);
        putSignatureIntoData(signature);

        //Finally inject all
        SourcePosition sp = loop.getPosition();
        int indexSp = sp.getSourceStart();
        CompilationUnit cu = sp.getCompilationUnit();
        cu.addSourceCodeFragment(new SourceCodeFragment(indexSp, sb.toString(), 0));

        /*
        SignalLoopDetector detector = new SignalLoopDetector();
        detector.setInjectors(degradationInjectors);
        detector.setProcessAllLoops(true);

        detector.process(loop);*/
    }

    /**
     * Builds the injection snippet for a particular variable
     *
     * @return
     */
    private String buildSnippet(SignalLoop inputs, Collection<Injector> injectors) {
        StringBuilder sb = new StringBuilder();
        for (CtVariableAccess a : inputs.getAccesses()) {
            data = new DetectionData();
            data.getParams().put("var", getCompilableName(a));
            data.getParams().put("name", getSignatureOfVar(a, inputs.getLoop()));
            //if ( a.getVariable().getType().is )
            if (a.getVariable().getType() instanceof CtArrayTypeReference) {
                CtArrayTypeReference ref = (CtArrayTypeReference) a.getVariable().getType();
                data.getParams().put("type", "Array" + ref.getComponentType().toString());
            } else data.getParams().put("type", a.getVariable().getType().toString());
            sb.append(getSnippet(injectors, inputs.getLoop(), data));
        }
        return sb.toString();
    }

    /**
     * Indicate whether 'a' references a local variable
     *
     * @param a
     * @param localVars
     * @return
     */
    private boolean isLocalVariable(CtVariableAccess a, List<CtLocalVariable> localVars) {
        for (CtLocalVariable lv : localVars) {
            if (lv.getReference().equals(a.getVariable())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether all variables are primitives or not
     *
     * @param access
     * @return
     */
    private boolean allVariablesArePrimitive(List<CtVariableAccess> access) {
        //Verify they are of type primitive or array
        for (CtVariableAccess a : access) {
            if (a.getVariable().getType() == null) {
                System.out.println("Yeah");
            } else if (a.getVariable().getType() instanceof CtArrayTypeReference) {
                CtArrayTypeReference ref = (CtArrayTypeReference) a.getVariable().getType();
                if (!ref.getComponentType().isPrimitive()) {
                    System.out.print(a + " IS NOT PRIMITIVE");
                    return false;
                }
            } else if (!(a.getVariable().getType().isPrimitive())) {
                System.out.print(a + " IS NOT PRIMITIVE");
                return false;
            }
        }
        return true;
    }

    /**
     * Indicate if the variable access is an access of a field of a primitive array
     *
     * @param a
     * @return
     */
    private boolean isFieldOfPrimitiveArray(CtVariableAccess a) {
        boolean result = false;
        if (a instanceof CtFieldAccess) {
            CtFieldAccess field = (CtFieldAccess) a;
            result = field.getTarget() instanceof CtVariableAccess;
            result = result && ((CtVariableAccess) field.getTarget()).getVariable().getType() instanceof CtArrayTypeReference;
            if (result) {
                CtArrayTypeReference arrayRef =
                        (CtArrayTypeReference) ((CtVariableAccess) field.getTarget()).getVariable().getType();
                result = arrayRef.getComponentType().isPrimitive();
            }
        }
        return result;
    }

    /**
     * Gets a name that can be placed in the code and compiled.
     * <p/>
     * For example myOject.myVar will generate a CtVarAccess named "myVar". However, this will be not recognized by the
     * compiler, being myObject.myVar needed
     *
     * @param access Access who's compilable name is required
     * @return
     */
    public static String getCompilableName(CtVariableAccess access) {
        if (access instanceof CtFieldAccess) {
            StringBuilder sb = new StringBuilder();
            CtFieldAccess field = (CtFieldAccess) access;
            if (field.getTarget() instanceof CtVariableAccess) {
                sb.append(getCompilableName((CtVariableAccess) field.getTarget())).append(".");
            }
            sb.append(field.getVariable().toString());
            return sb.toString();
        }
        return access.getVariable().toString();
    }

    /**
     * Indicate if a variable access is initialized
     *
     * @param a
     * @param loop
     * @return
     */
    private boolean isInitialized(CtVariableAccess a, CtLoop loop) {
        if ((a.getVariable().getDeclaration() != null &&
                a.getVariable().getDeclaration().getDefaultExpression() != null)
                || !(a.getVariable() instanceof CtLocalVariableReference)) return true;
        CtMethod m = a.getParent(CtMethod.class);
        if (m == null) return false;
        List<CtAssignment> assignments = m.getElements(new TypeFilter<CtAssignment>(CtAssignment.class));
        for (CtAssignment ctA : assignments) {
            for (CtVariableAccess access :
                    ctA.getAssigned().getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class)))
                if (access.getVariable().equals(a.getVariable()) &&
                        ctA.getAssigned().getPosition().getSourceStart() < loop.getPosition().getSourceStart())
                    return true;
        }
        return false;
    }

    /**
     * Obtains the name signature of a CtVariableAccess for logging purposes
     *
     * @param a
     * @param loop
     * @return
     */
    private String getSignatureOfVar(CtVariableAccess a, CtLoop loop) {
        SourcePosition pos = loop.getPosition();
        return "\"" + pos.getCompilationUnit().getMainType().getQualifiedName().replace(".", "-") + "-" + pos.getLine() +
                "-" + a.getVariable().toString() + "\"";
    }

    /**
     * Indicate whether we can extract the input of the loop or not.
     * <p/>
     * Basically we check for non-static method calls
     *
     * @param loop
     * @return
     */
    private boolean checkPreconditions(SignalLoop loop) {
        boolean signal = loop.isSignalLoop() || new SignalLoopDetector().isSignalLoop(loop.getLoop());
        return signal && loop.getLoop().getBody() != null &&
                !containsNonStaticInvocations(loop.getLoop().getBody(), 3);
    }

    /**
     * Indicate if the element contains non-static invocations to the nth level
     *
     * @param element Invocation to inspect
     * @param levels  Levels to explore
     * @return True if the method contains non static invocations
     */
    private boolean containsNonStaticInvocations(CtElement element, int levels) {
        if (levels <= 0) return true;
        levels--;
        List<CtInvocation> invocations = element.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));
        for (CtInvocation inv : invocations) {
            if (!inv.getExecutable().isStatic() && containsNonStaticInvocations(inv, levels)) {
                System.out.println("Invocation " + inv.toString() + " is not static to the " + levels + "th level");
                return true;
            }
        }
        return false;
    }

    @Override
    public void collectInjectors(AbstractMap<String, Collection<Injector>> injectors) {
        inputInjectors = injectors.containsKey(INPUTS) ? injectors.get(INPUTS) : new ArrayList<Injector>();
        endInjectors = injectors.containsKey(END_INPUTS) ? injectors.get(END_INPUTS) : new ArrayList<Injector>();
    }

    public SignalLoop getSignalLoop() {
        signalLoop = signalLoop == null ? new SignalLoop() : signalLoop;
        return signalLoop;
    }

    public void setSignalLoop(SignalLoop signalLoop) {
        this.signalLoop = signalLoop;
    }
}
