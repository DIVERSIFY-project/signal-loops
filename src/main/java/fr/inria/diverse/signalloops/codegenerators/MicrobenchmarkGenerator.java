package fr.inria.diverse.signalloops.codegenerators;

import fr.inria.diverse.signalloops.detectors.LoopInputsDetector;
import fr.inria.diverse.signalloops.model.SignalLoop;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MicrobenchmarkGenerator {

    private static final String GRACEFULLY_BENCHMARK = "GRACEFULLY";
    private static final String ORIGINAL_BENCHMARK = "ORIGINAL";
    private static final String PAD_8 = "        ";
    private static final String PAD_4 = "    ";


    /**
     * Indicates whether the template must be exported to file or not. True by default
     */
    private boolean writeToFile = true;

    /**
     * Template output
     */
    private String output;

    /**
     * Configuration of the FreeMarker engine
     */
    Configuration templateConf;

    /**
     * Generation output
     *
     * @return
     */
    public String getOutput() {
        return output;
    }

    public boolean isWriteToFile() {
        return writeToFile;
    }

    public void setWriteToFile(boolean writeToFile) {
        this.writeToFile = writeToFile;
    }


    /**
     * Initializes the benchmark generator
     *
     * @param templatePath Path to the templates
     * @throws IOException
     */
    public void initialize(String templatePath) throws IOException {
        // Create your Configuration instance, and specify if up to what FreeMarker
        // version (here 2.3.22) do you want to apply the fixes that are not 100%
        // backward-compatible. See the Configuration JavaDoc for details.
        templateConf = new Configuration(Configuration.VERSION_2_3_22);

        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:
        templateConf.setDirectoryForTemplateLoading(new File(templatePath));

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        templateConf.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        templateConf.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        //templateConf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

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
        sb.append(PAD_4).append("}\n}\n");

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
     * Generate a bechmark class out of a loop
     *
     * @param packageName    Name of the package
     * @param generationPath Path where the output is going to be generated
     * @param dataPath       Root path where is located all the input data for the microbenchmark
     * @param loop           Signal loop for wich we are going to generate the benchmark
     * @param degraded       Indicate to generate a microbenchmark for the degraded loop
     * @throws FileNotFoundException
     */
    public void generateBenchmark(String packageName, String generationPath,
                                  String dataPath, SignalLoop loop, boolean degraded) {
        HashMap<String, Object> input = new HashMap<String, Object>();
        input.put("package_name", packageName);
        input.put("class_comments", "Loop ID: " + loop.getId());
        input.put("input_root_folder_path", dataPath);
        input.put("input_data_file_path", loop.getMicrobenchmarkClassName().replace("_", "-"));
        input.put("class_name", loop.getMicrobenchmarkClassName());
        input.put("input_vars", getInjectionInputVariables(loop));
        input.put("loop_code", loopPrettyPrint(loop, degraded));

        String degradedType = degraded ? GRACEFULLY_BENCHMARK : ORIGINAL_BENCHMARK;
        input.put("degraded_type", degradedType);

        try {
            PrintWriter out = null;
            try {
                Template template = templateConf.getTemplate("loop-micro-benchmark.ftl");
                StringWriter writer = new StringWriter();
                template.process(input, writer);
                output = writer.getBuffer().toString();

                if ( writeToFile ) {
                    out = new PrintWriter(generationPath + "/" + loop.getMicrobenchmarkClassName() + ".java");
                    out.println(output);
                }

            } finally {
                if (out != null) out.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

