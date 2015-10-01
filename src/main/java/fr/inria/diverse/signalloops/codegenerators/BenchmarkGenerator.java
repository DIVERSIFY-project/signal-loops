package fr.inria.diverse.signalloops.codegenerators;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;

/**
 * Created by marodrig on 29/09/2015.
 */
public class BenchmarkGenerator {

    protected static final String GRACEFULLY_BENCHMARK = "GRACEFULLY";
    protected static final String ORIGINAL_BENCHMARK = "ORIGINAL";
    protected static final String PAD_8 = "        ";
    protected static final String PAD_4 = "    ";

    private static Logger log = Logger.getLogger(BenchmarkGenerator.class);

    /**
     * Indicates whether the template must be exported to file or not. True by default
     */
    protected boolean writeToFile = true;

    /**
     * Template output
     */
    protected String output;

    /**
     * Configuration of the FreeMarker engine
     */
    protected Configuration templateConf;

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
     * Indicates whether exist an input data file for the class name in the given data path
     * @param dataPath Data path to search for the data file
     * @param className Name of the micro-benchmark class
     * @return True if a file exists, false otherwise
     */
    public boolean existsDataFile(String dataPath, final String className) {
        try {
            final String fileName = className.replace("_", "-");

            File[] files = new File(dataPath).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(fileName);
                }
            });
            return files != null && files.length > 0;
        } catch (Exception e) {
            log.error("Unexpected exception at existDataFile. ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the benchmark generator
     *
     * @param templatePath Path to the templates
     * @throws java.io.IOException
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
     * Generates the output
     * @param input Input data for the template
     * @param templateName Name of the template
     * @param writeToFile Whether we should write to file the output or not
     * @param outputPath Output file path in the case writeToFile is true
     */
    protected void generateOutput(HashMap<String, Object> input, String templateName, boolean writeToFile, String outputPath) {
        try {
            PrintWriter out = null;
            try {
                Template template = templateConf.getTemplate(templateName);
                StringWriter writer = new StringWriter();
                template.process(input, writer);
                output = writer.getBuffer().toString();

                if (writeToFile) {
                    out = new PrintWriter(outputPath);
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
