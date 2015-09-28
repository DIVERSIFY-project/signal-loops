package fr.inria.diverse.signalloops.codegenerators;

import fr.inria.diverse.signalloops.model.SignalLoop;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MicrobenchmarkGenerator {

    private String output;

    /**
     * Initializes the benchmark generator
     * @param templatePath Path to the templates
     * @throws IOException
     */
    public void initialize(String templatePath) throws IOException {
        // Create your Configuration instance, and specify if up to what FreeMarker
        // version (here 2.3.22) do you want to apply the fixes that are not 100%
        // backward-compatible. See the Configuration JavaDoc for details.
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);

        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:
        cfg.setDirectoryForTemplateLoading(new File(templatePath));

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        cfg.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        //cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    /**
     * Generate a bechmark class out of a loop
     * @param packageName Name of the package
     * @param generationPath Path where the output is going to be generated
     * @param dataPath Root path where is located all the input data for the microbenchmark
     * @param loop Signal loop for wich we are going to generate the benchmark
     * @param degraded Indicate to generate a microbenchmark for the degraded loop
     * @throws FileNotFoundException
     */
    public void generateBenchmark(String packageName, String generationPath,
                                  String dataPath, SignalLoop loop, boolean degraded) throws FileNotFoundException {


}

    public String getOutput() {
        return output;
    }
