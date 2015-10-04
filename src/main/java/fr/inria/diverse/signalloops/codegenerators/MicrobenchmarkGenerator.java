package fr.inria.diverse.signalloops.codegenerators;

import fr.inria.diverse.signalloops.detectors.LoopInputsDetector;
import fr.inria.diverse.signalloops.model.SignalLoop;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MicrobenchmarkGenerator extends BenchmarkGenerator {

    /**
     * Build the set of template wrappers for the input variables of the loop
     *
     * @return
     */
    private ArrayList<TemplateInputVariable> getInjectionInputVariables(SignalLoop loop) {
        ArrayList<TemplateInputVariable> result = new ArrayList<TemplateInputVariable>();
        for (CtVariableAccess access : loop.getAccesses()) {
            TemplateInputVariable var = new TemplateInputVariable();
            var.setInitialized(loop.getInitialized().contains(access));
            var.setVariableName(accessPrettyPrint(access));
            var.setVariableType(access.getVariable().getType().toString());
            if (access.getVariable().getType() instanceof CtArrayTypeReference) {
                CtArrayTypeReference ref = (CtArrayTypeReference) access.getVariable().getType();
                var.setLoadMethodName("Array" + ref.getComponentType().toString());
            } else var.setLoadMethodName(access.getType().toString());
            result.add(var);
        }
        return result;
    }

    /**
     * Pretty print a variable access
     *
     * @param access
     * @return
     */
    private String accessPrettyPrint(CtVariableAccess access) {
        return LoopInputsDetector.getCompilableName(access).replace(".", "_");
    }

    /**
     * Pretty print the loop, basically substitute static private method for our public of the methods
     * and static fields for our public copy of the static field
     *
     * @param loop
     * @param degraded
     * @return
     */
    private String loopPrettyPrint(SignalLoop loop, boolean degraded) {

        //An old logic copy and pasted. It works well...

        String loopStr = loop.getLoop().toString();
        loopStr = loopStr.replace("\r\n", "\r\n" + PAD_8);
        StringBuilder sb = new StringBuilder();
        if (degraded) {
            //TODO: Have into consideration the case where there is only one line (no final })
            sb.append(PAD_8).append(loopStr.substring(0, loopStr.length() - 1)).append("\n");//eliminate last "}"
            sb.append(PAD_8).append(loop.getDegradedSnippet()).append("}\n");
        } else sb.append(PAD_8).append(loopStr).append("\n");
        //sb.append(PAD_4).append("}\n}\n");

        loopStr = sb.toString();

        for (CtVariableAccess a : loop.getAccesses()) {
            if (a instanceof CtFieldAccess) {
                CtFieldAccess f = (CtFieldAccess) a;
                if (f.getVariable().isStatic() && loop.getInitialized().contains(f)) {
                    loopStr = loopStr.replace(f.toString(), accessPrettyPrint(f));
                }
            }
        }
        for (CtInvocation inv : loop.getLoop().getElements(new TypeFilter<CtInvocation>(CtInvocation.class))) {
            if (inv.getExecutable().isStatic() && inv.getExecutable().getDeclaration() != null) {
                String invStr = inv.toString();
                invStr = invStr.substring(0, invStr.lastIndexOf("(") - 1);
                loopStr = loopStr.replace(invStr, invStr.replace(".", "_"));
            }
        }

        return loopStr;
    }

    /**
     * Extract private static method out of an statement and copy its body to the microbenchmark
     *
     * @param sb        Output string builder that will contain the body of the method
     * @param statement Statement containing the method invocations.
     */
    private void extractStaticMethod(StringBuilder sb, CtStatement statement) {
        //Append all static methods
        List<CtInvocation> methods = statement.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));
        for (CtInvocation inv : methods) {
            //Find static and private methods
            if (inv.getExecutable().isStatic() &&
                    inv.getExecutable().getDeclaration() != null &&
                    !inv.getExecutable().getDeclaration().getModifiers().contains(ModifierKind.PUBLIC)) {

                //Prety print the static declaration of the method with a different name
                CtExecutable ref = inv.getExecutable().getDeclaration();
                sb.append(PAD_4).append("static ").
                        append(ref.getType().getQualifiedName()).
                        append(" ").
                        append(ref.getDeclaringType().getQualifiedName().replace(".", "_").replace("$", "_")).
                        append("_").
                        append(ref.getSimpleName()).
                        append("(");

                //Print parameters of the method
                CtParameter p = (CtParameter) ref.getParameters().get(0);
                sb.append(p.getType().getQualifiedName()).append(" ").append(p.getSimpleName());
                for (int i = 1; i < ref.getParameters().size(); i++) {
                    p = (CtParameter) ref.getParameters().get(i);
                    sb.append(", ").append(p.getType().getQualifiedName()).append(" ").append(p.getSimpleName());
                }
                sb.append(")");
                //Print the body
                String decStr = ref.getBody().toString();
                decStr = decStr.replace("\r\n", "\r\n" + PAD_4);
                sb.append(PAD_4).append(decStr);
                sb.append("\n\n");
                List<CtInvocation> deepMethods =
                        inv.getExecutable().getDeclaration().getBody().getElements(
                                new TypeFilter<CtInvocation>(CtInvocation.class));
                //Recursively add other static methods
                for (CtStatement m : deepMethods) {
                    extractStaticMethod(sb, m);
                }
            }
        }
    }

    /**
     * Generate a bechmark class out of a loop
     *
     * @param packageName    Name of the package
     * @param generationPath Path where the output is going to be generated
     * @param dataPath       Root path where is located all the input data for the microbenchmark
     * @param loop           Signal loop for wich we are going to generate the benchmark
     * @param degraded       Indicate to generate a microbenchmark for the degraded loop
     * @throws FileNotFoundException
     */
    public void generate(String packageName, String generationPath,
                         String dataPath, SignalLoop loop, boolean degraded) {

        if ( !existsDataFile(dataPath, loop.getMicrobenchmarkClassName()) ) return;

        HashMap<String, Object> input = new HashMap<String, Object>();
        input.put("package_name", packageName);
        input.put("class_comments", "Loop ID: " + loop.getId());
        input.put("input_root_folder_path", dataPath.replace("\\", "/"));
        input.put("input_data_file_path", loop.getMicrobenchmarkClassName().replace("_", "-"));
        input.put("class_name", loop.getMicrobenchmarkClassName());
        input.put("input_vars", getInjectionInputVariables(loop));
        //input.put("signal_array_type", loop.getSignalArray().getType() + "[]");
        //input.put("signal_array", loop.getSignalArray().getTarget().toString());

        //Get static methods
        StringBuilder staticMethodsPrint = new StringBuilder();
        extractStaticMethod(staticMethodsPrint, loop.getLoop());
        input.put("static_methods",staticMethodsPrint.toString());

        //Code of the loop
        input.put("loop_code", loopPrettyPrint(loop, degraded));


        String degradedType = degraded ? GRACEFULLY_BENCHMARK : ORIGINAL_BENCHMARK;
        input.put("degraded_type", degradedType);

        generateOutput(input, "loop-micro-benchmark.ftl",
                writeToFile, generationPath + "/" + loop.getMicrobenchmarkClassName() + "_" + degradedType + ".java");
    }
}

