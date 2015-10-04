package fr.inria.diverse.signalloops.programs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * Input configuration for the Loop perforation
 * <p/>
 * Created by marodrig on 01/10/2015.
 */
public class PerforationConfiguration extends Properties {

    private String prj;
    private String prjSrc;
    private String resDir;
    private String instrumentedPrj;
    private String coverage;

    private String generationOutputPath;
    private String generationOutputTestPath;
    private String dataInputPath;
    private String databaseOutputPath;
    private String packageName;

    public String getProjectRoot() {
        return prj;
    }

    public String getProjectSource() {
        return prjSrc;
    }

    public String getResDir() {
        return resDir;
    }

    public String getInstrumentedProjectRoot() {
        return instrumentedPrj;
    }

    /**
     * Path to the junco coverage files
     * @return
     */
    public String getCoverage() {
        return coverage;
    }

    /**
     * Path where the micro-benchmarks are going to be stored
     * @return
     */
    public String getGenerationOutputPath() {
        return generationOutputPath;
    }

    /**
     * Path where the micro-benchmarks unit tests are going to be stored
     * @return
     */
    public String getGenerationOutputTestPath() {
        return generationOutputTestPath;
    }

    /**
     * Path where the intermediary log data file is going to be stored
     * @return
     */
    public String getDataInputPath() {
        return dataInputPath;
    }

    /**
     * Path to the db where all the data regarding loops is going to be stored
     * @return
     */
    public String getDatabaseOutputPath() {
        return databaseOutputPath;
    }

    /**
     * Microbenchmark package name
     * @return
     */
    public String getPackageName() {
        return packageName;
    }



    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        super.loadFromXML(in);
        configure();
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
        configure();
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
        configure();
    }

    private void configure() {
        prj = this.getProperty("project.dir");//PROJECT_DIR;
        prjSrc = this.getProperty("src.dir");//SRC_DIR + "/java";
        resDir = this.getProperty("res.dir");//SRC_DIR + "/java";
        instrumentedPrj = this.getProperty("out.dir");//TEST_DIR + "/java";
        coverage = this.getProperty("coverage");//TEST_DIR + "/java";

        generationOutputPath = this.getProperty("generationOutputPath");    //   "C:\\MarcelStuff\\PROJECTS\\preforation-benchmark\\src\\main\\java\\fr\\inria\\diverse\\perfbench";
        generationOutputTestPath = this.getProperty("generationOutputTestPath");//   "C:\\MarcelStuff\\PROJECTS\\preforation-benchmark\\src\\test\\java\\fr\\inria\\diverse\\perfbench";
        dataInputPath = this.getProperty("dataInputPath");           //   "C:\\MarcelStuff\\DATA\\DIVERSE\\logs\\input-data";
        databaseOutputPath = this.getProperty("databaseOutputPath");      //   "C:\\MarcelStuff\\DATA\\DIVERSE\\PREFORATION\\perforationresults.s3db";
        packageName = this.getProperty("packageName");             //   "fr.inria.diverse.perfbench";
    }
}
