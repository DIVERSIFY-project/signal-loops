package fr.inria.diverse.signalloops.model;

import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtVariableAccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by marodrig on 02/09/2015.
 */
public class SignalLoop implements PersistentObject {

    private boolean signalLoop;

    private int id;
    private String code;//Code of the loop
    private String position;//Position of the loop
    private int project = 0;//Project the loop belongs to
    private int totalStmnt;//Number of statements in the loop
    private int fixedStmnt;//Number of statemetns that could not be moved
    private double timeGain;//Time gained by the loop
    private boolean testFails;//Indicate whether the test fails or not
    private String degradedSnippet;

    public boolean isTestTimeOut() {
        return testTimeOut;
    }

    public void setTestTimeOut(boolean testTimeOut) {
        this.testTimeOut = testTimeOut;
    }

    private boolean testTimeOut;//Indicate whether the test fails or not
    private int nbTestCover;//Number of test
    private int upFix;//Upper frontier of the fixed statements
    private int downFix;//Lower frontier of the fixed statements
    private double originalTime;//Original time taken by the loop
    private double distortion;//Min value of the logged values
    private double minVal;//Max value of the logged values
    private double maxVal;// Normalized distortion (i.e accuracy lost)
    private double normalizedDistortion;// Total accuracy lost


    private int sizeDiff;// Log size difference.

    public static String TABLE_NAME = "Loops";

    private static String SELECT_QUERY =
            "SELECT * FROM " + TABLE_NAME + " WHERE id == %d";

    private static String INSERT_QUERY =
            "INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES (%d,'%s','%s',%d,%d,%d,%f,%d, %d,%d,%d,%d,%f,%f,%f,%f,%f,%d);";

    CtLoop loop = null;

    List<CtVariableAccess> accesses;

    /**
     * Indicate if the variable has any value before the loop;
     */
    HashSet<CtVariableAccess> initialized = new HashSet<CtVariableAccess>();

    private String name;

    public boolean isSignalLoop() {
        return signalLoop;
    }

    public void setSignalLoop(boolean signalLoop) {
        this.signalLoop = signalLoop;
    }

    public void setDegradedSnippet(String degradedSnippet) {
        this.degradedSnippet = degradedSnippet;
    }

    public String getDegradedSnippet() {
        return degradedSnippet;
    }

    /**
     * Obtains the microbenchmark class name for this signal loop
     * @return
     */
    public String getMicrobenchmarkClassName() {
        return getPosition().replace(".", "_").replace(":", "_");
    }

    public static class Loader implements PersistentObjectLoader {
        public List<PersistentObject> buildFromResultSet(ResultSet set) throws SQLException {
            ArrayList<PersistentObject> loops = new ArrayList<PersistentObject>();
            while (set.next()) {
                SignalLoop loop = new SignalLoop();
                loop.setId(set.getInt("id"));
                loop.fromResultSet(set);
                loops.add(loop);
            }
            return loops;
        }
    }


    /**
     * @param set
     * @throws java.sql.SQLException
     */
    public void fromResultSet(ResultSet set) throws SQLException {
        code = set.getString("code");
        position = set.getString("position");
        project = set.getInt("project");
        totalStmnt = set.getInt("totalStmnt");
        fixedStmnt = set.getInt("fixedStmnt");
        timeGain = set.getInt("timeGain");
        testFails = set.getInt("testFails") == 1;
        testTimeOut = set.getInt("testTimeOut") == 1;
        nbTestCover = set.getInt("nbTestCover");
        upFix = set.getInt("upFix");
        downFix = set.getInt("downFix");
        originalTime = set.getInt("originalTime");
        distortion = set.getInt("distortion");
        minVal = set.getInt("minVal");
        maxVal = set.getInt("maxVal");
        normalizedDistortion = set.getInt("normalizedDistortion");
    }

    public String getRetrieveQuery() {
        return SELECT_QUERY.format(Locale.ENGLISH, SELECT_QUERY, id);
    }

    public String getInsertQuery() {
        return String.format(Locale.ENGLISH, INSERT_QUERY,
                id, code, position, project, totalStmnt,
                fixedStmnt, timeGain, (testFails) ? 1 : 0, (testTimeOut) ? 1 : 0, nbTestCover,
                upFix, downFix, originalTime, distortion,
                minVal, maxVal, normalizedDistortion, sizeDiff);
    }

    private static String UPDATE_QUERY =
            "UPDATE Loops SET id= %d,code= '%s',project= %d," +
                    "totalStmnt=%d, fixedStmnt= %d,timeGain= %d,testFails= %d,nbTestCover= %d,distortion= %f";

    public String getUpdateQuery() {
        return String.format(UPDATE_QUERY, id, code, project, totalStmnt,
                fixedStmnt, timeGain, testFails, nbTestCover, distortion);
    }


    public CtLoop getLoop() {
        return loop;
    }

    public List<CtVariableAccess> getAccesses() {
        if (accesses == null) accesses = new ArrayList<CtVariableAccess>();
        return accesses;
    }

    public String getName() {
        return name;
    }

    public void setMicroBenchMarkName(String name) {
        this.name = name;
    }

    public void setAccesses(List<CtVariableAccess> accesses) {
        this.accesses = accesses;
    }

    public void setLoop(CtLoop loop) {
        this.loop = loop;
    }

    public Set<CtVariableAccess> getInitialized() {
        if (initialized == null) initialized = new HashSet<CtVariableAccess>();
        return initialized;
    }

    public int getSizeDiff() {
        return sizeDiff;
    }

    public void setSizeDiff(int sizeDiff) {
        this.sizeDiff = sizeDiff;
    }


    /**
     * @return
     */
    public double getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(double originalTime) {
        this.originalTime = originalTime;
    }

    public boolean isTestFails() {
        return testFails;
    }

    public void setMinVal(double minVal) {
        this.minVal = minVal;
    }

    public double getMinVal() {
        return minVal;
    }

    public void setMaxVal(double maxVal) {
        this.maxVal = maxVal;
    }

    public double getMaxVal() {
        return maxVal;
    }

    public void setNormalizedDistortion(double normalizedDistortion) {
        this.normalizedDistortion = normalizedDistortion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getProject() {
        return project;
    }

    public void setProject(int project) {
        this.project = project;
    }

    public int getTotalStmnt() {
        return totalStmnt;
    }

    public void setTotalStmnt(int totalStmnt) {
        this.totalStmnt = totalStmnt;
    }

    public int getFixedStmnt() {
        return fixedStmnt;
    }

    public void setFixedStmnt(int fixedStmnt) {
        this.fixedStmnt = fixedStmnt;
    }

    public double getTimeGain() {
        return timeGain;
    }

    public void setTimeGain(double timeGain) {
        this.timeGain = timeGain;
    }

    public boolean getTestFails() {
        return testFails;
    }

    public void setTestFails(boolean testFails) {
        this.testFails = testFails;
    }

    public int getNbTestCover() {
        return nbTestCover;
    }

    public void setNbTestCover(int nbTestCover) {
        this.nbTestCover = nbTestCover;
    }

    public double getDistortion() {
        return distortion;
    }

    public void setDistortion(float distortion) {
        this.distortion = distortion;
    }


    public void setPosition(String position) {
        this.position = position;
    }

    public String getPosition() {
        if ( position == null )
            position = getLoop().getPosition().getCompilationUnit().getMainType().getQualifiedName() +
                    ":"+ getLoop().getPosition().getLine();
        return position;
    }

    public void setUpFix(int upFix) {
        this.upFix = upFix;
    }

    public int getUpFix() {
        return upFix;
    }

    public void setDownFix(int downFix) {
        this.downFix = downFix;
    }

    public int getDownFix() {
        return downFix;
    }

    public double getNormalizedDistortion() {
        return normalizedDistortion;
    }

}
