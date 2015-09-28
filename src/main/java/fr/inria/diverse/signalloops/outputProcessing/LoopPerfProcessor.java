package fr.inria.diverse.signalloops.outputProcessing;

import fr.inria.diverse.signalloops.loggers.loopPerforation.LoopPerforationLogger;
import fr.inria.diverse.signalloops.model.SignalLoop;
import fr.inria.diversify.syringe.processor.EntryLog;
import fr.inria.diversify.syringe.processor.EntryProcessor;
import fr.inria.diversify.syringe.processor.LoadingException;

import java.io.*;
import java.util.*;

import static fr.inria.diverse.signalloops.loggers.loopPerforation.LoopPerforationLogger.*;
import static java.lang.Math.*;

/**
 * Created by marodrig on 20/02/2015.
 */
public class LoopPerfProcessor implements EntryProcessor {


    /**
     * Path to the previous log when the processor is the "after" processor
     */
    private String previousLogPath;

    /**
     * Indicates if this processor is a processor reading the after log
     */
    private boolean afterProcessor = false;

    /**
     * Path to log data
     */
    private final String logPath;

    private double totalSignal = 0;

    private double totalAll = 0;

    List<String> errors;

    HashSet<Integer> beforeLoops = new HashSet<Integer>();

    HashSet<Integer> afterLoops = new HashSet<Integer>();

    private Map<Integer, PerforationMetrics> result = null;

    private Map<Integer, SignalLoop> loops = null;

    private boolean measureSpeed = false;

    private boolean measureAccuracy = false;

    public LoopPerfProcessor(String logPath) {
        this.logPath = logPath;
    }



    @Override
    public void process(Collection<EntryLog> entries) throws LoadingException {
        if (result == null) result = new HashMap<Integer, PerforationMetrics>();

        errors = new ArrayList<String>();

        int id = -1;
        for (EntryLog e : entries) {
            try {
                if (e.getType().equals(LOOP_DURATION)) {
                    id = Integer.parseInt(e.getParameters()[0]);
                    String s = e.getIdMap().get(id);
                    double millis = Double.valueOf(e.getParameters()[1]) / 1000000.0;
                    if (s.startsWith("SIGNAL")) {
                        totalSignal += millis;
                        PerforationMetrics metrics = result.get(id);
                        if (afterProcessor && measureSpeed) {
                            if (metrics == null) {
                                throw new RuntimeException("Unable to find the loop in the previous run");
                            }
                            metrics.addAfterDuration(millis);
                        } else {
                            if (metrics == null) {
                                metrics = new PerforationMetrics(id);
                                metrics.setPosition(s);
                                result.put(id, metrics);
                            }
                            metrics.addDuration(millis);
                        }
                    }
                    totalAll += millis;
                }
            } catch (Exception ex) {
                System.out.println("Loop: " + id + ex.getMessage());
                //throw new RuntimeException(ex);
                //errors.add(ex.getMessage());
            }
        }

        if (afterProcessor && measureAccuracy) {
            for (Map.Entry<Integer, PerforationMetrics> e : result.entrySet()) {

                List<File> beforeFiles = loopOutputFiles(previousLogPath, e.getKey());
                List<File> afterFiles = loopOutputFiles(logPath, e.getKey());

                if ((beforeFiles.size() != afterFiles.size()) || (beforeFiles.isEmpty() && afterFiles.isEmpty())) {
                    /*
                    System.out.println("LOOP SKIPPED. Mismatching output size (files) for loop: " + e.getKey() +
                            " Before: " + beforeFiles.size() + " After: " + afterFiles.size());
                            */
                    continue;
                }

                double accumulatedDiff = 0;
                double totalSize = 0;
                double maxVal = Double.NEGATIVE_INFINITY;
                double minVal = Double.POSITIVE_INFINITY;

                for (int k = 0; k < beforeFiles.size(); k++) {
                    //Calculate accuracy lost
                    try {
                        DataInputStream inBefore =
                                new DataInputStream(new BufferedInputStream(
                                        new FileInputStream(beforeFiles.get(k)), 2048));
                        DataInputStream inAfter =
                                new DataInputStream(new BufferedInputStream(
                                        new FileInputStream(afterFiles.get(k)), 2048));

                        int sizeBefore = inBefore.readInt();
                        int sizeAfter = inAfter.readInt();

                        if (sizeBefore != sizeAfter) {
                            System.out.println("WARNING: Mismatching output size (VALUES) for loop: " + e.getKey() +
                                    " Before: " + sizeBefore + " After: " + sizeAfter);
                        }

                        totalSize += sizeBefore;
                        for (int i = 0; i < min(sizeAfter, sizeBefore); i++) {
                            double beforeVal = inBefore.readDouble();
                            double afterVal = inAfter.readDouble();
                            accumulatedDiff = abs(beforeVal - afterVal);
                            maxVal = max(max(maxVal, beforeVal), afterVal);
                            minVal = min(min(minVal, beforeVal), afterVal);
                        }
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                accumulatedDiff /= totalSize;
                double normal = abs(maxVal - minVal);
                if (normal != 0) normal = accumulatedDiff / normal;
                e.getValue().setAccuracyLost(accumulatedDiff);
                e.getValue().setNormalized(normal);
                e.getValue().setMaxVal(maxVal);
                e.getValue().setMinVal(minVal);
            }
        }




    }

    private List<File> loopOutputFiles(String path, Integer loop) {
        List<File> files = Arrays.asList(new File(path).listFiles());
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        List<File> result = new ArrayList<File>();
        for (File f : files) {
            if (!f.getName().startsWith("output_" + String.valueOf(loop) + "_")) continue;
            result.add(f);
        }
        return result;
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }

    public Map<Integer, PerforationMetrics> getResult() {
        return this.result;
    }

    public void setResult(Map<Integer, PerforationMetrics> result) {
        this.result = result;
    }


    public boolean isAfterProcessor() {
        return afterProcessor;
    }

    public void setAfterProcessor(boolean afterProcessor) {
        this.afterProcessor = afterProcessor;
    }

    public double getTotalAll() {
        return totalAll;
    }

    public double getTotalSignal() {
        return totalSignal;
    }

    public String getPreviousLogPath() {
        return previousLogPath;
    }

    public void setPreviousLogPath(String previousLogPath) {
        this.previousLogPath = previousLogPath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setMeasureSpeed(boolean measureSpeed) {
        this.measureSpeed = measureSpeed;
    }

    public boolean isMeasureSpeed() {
        return measureSpeed;
    }

    public void setMeasureAccuracy(boolean measureAccuracy) {
        this.measureAccuracy = measureAccuracy;
    }

    public boolean isMeasureAccuracy() {
        return measureAccuracy;
    }

    public Map<Integer, SignalLoop> getLoops() {
        return loops;
    }

    public void setLoops(Map<Integer, SignalLoop> loops) {
        this.loops = loops;
    }
}
