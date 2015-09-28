package fr.inria.diverse.signalloops.outputProcessing;

import fr.inria.diverse.signalloops.loggers.loopPerforation.LoopPerforationLogger;
import fr.inria.diverse.signalloops.model.PersistentObject;
import fr.inria.diverse.signalloops.model.SQLLiteConnection;
import fr.inria.diverse.signalloops.model.SignalLoop;
import fr.inria.diversify.syringe.processor.LoadingException;
import fr.inria.diversify.syringe.processor.SyringeDataReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by marodrig on 05/05/2015.
 */
public class LoopPerforationProgram {

    public static void main(String[] args) throws Exception {
        new LoopPerforationProgram().execute(
                "C:\\MarcelStuff\\PROJECTS\\DIVERSE\\instrumentation\\src\\main\\resources\\loop_perforation\\jsyn.properties");
    }

    private String loop(String name) {
        int i1 = name.indexOf("_");
        int i2 = name.indexOf("_", i1 + 1);
        return name.substring(i1 + 1, i2);
    }

    private LoopPerfProcessor processBefore(File logDir, final String prefix)
            throws FileNotFoundException, LoadingException {

        File[] all = logDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        });

        String logDirName = all[0].getAbsolutePath();
        LoopPerfProcessor processor = new LoopPerfProcessor(logDirName);
        SyringeDataReader reader = new SyringeDataReader(processor);
        reader.read(logDir.getAbsolutePath() + "/" + LoopPerforationLogger.ID_FILE_NAME, logDirName);
        return processor;
    }

    public void execute(String path) throws Exception {
        Properties p = new Properties();
        p.load(new FileInputStream(path));
        String logDirName = p.getProperty("log.dir");
        int maxLoop = Integer.valueOf(p.getProperty("max.loops", "1000"));

        File logDir = new File(logDirName);

        //Read unmodified results for speed
        LoopPerfProcessor speed_all = processBefore(logDir, "speed_all");
        LoopPerfProcessor accuracy_all = processBefore(logDir, "accuracy_all");

        Map<Integer, PerforationMetrics> resultsSpeeds = new HashMap<Integer, PerforationMetrics>();
        Map<Integer, PerforationMetrics> resultsAccuracy = new HashMap<Integer, PerforationMetrics>();

        for (File f : logDir.listFiles()) {
            LoopPerfProcessor beforeProcessor;
            String n = f.getName();

            LoopPerfProcessor afterProcessor = new LoopPerfProcessor(f.getAbsolutePath());

            if (n.startsWith("accuracy") && !n.startsWith("accuracy_all")) {
                System.out.println(" -> " + n);
                beforeProcessor = accuracy_all;
                afterProcessor.setMeasureAccuracy(true);
            } else if (n.startsWith("speed") && !n.startsWith("speed_all")) {
                beforeProcessor = speed_all;
                afterProcessor.setMeasureSpeed(true);
            } else {
                continue;
            }


            afterProcessor.setAfterProcessor(true);
            afterProcessor.setPreviousLogPath(beforeProcessor.getLogPath());
            afterProcessor.setResult(beforeProcessor.getResult());
            SyringeDataReader afterReader = new SyringeDataReader(afterProcessor);
            afterReader.read(logDirName + "/" + LoopPerforationLogger.ID_FILE_NAME, f.getAbsolutePath());
/*
            if (n.startsWith("accuracy")) {
                int loop = Integer.valueOf(loop(n));
                if (resultsAccuracy.containsKey(loop))
                    throw new RuntimeException("Loop already processed");

                resultsAccuracy.put(loop, new PerforationMetrics(afterProcessor.getResult().get(loop)));
            } else if (n.startsWith("speed")) {
                int loop = Integer.valueOf(loop(n));
                if (resultsSpeeds.containsKey(loop))
                    throw new RuntimeException("Loop already processed");
                resultsSpeeds.put(loop, new PerforationMetrics(afterProcessor.getResult().get(loop)));
            }
*/
        }

        String dbPath = p.getProperty("loopDB");
        SQLLiteConnection c = new SQLLiteConnection(dbPath);
        List<PersistentObject> objects = c.retrieveAll(SignalLoop.TABLE_NAME, new SignalLoop.Loader());
        HashMap<Integer, SignalLoop> loops = new HashMap<Integer, SignalLoop>();
        for (PersistentObject pObject : objects) loops.put(((SignalLoop) pObject).getId(), (SignalLoop) pObject);
        for (Map.Entry<Integer, PerforationMetrics> e : speed_all.getResult().entrySet()) {
            PerforationMetrics accuracy = accuracy_all.getResult().get(e.getKey());
            PerforationMetrics speed = speed_all.getResult().get(e.getKey());
            accuracy.printResults(true, false);
            speed.printResults(false, true);
            //This is the easy HACK. TODO: Perhaps improve sometime by writing directly in the loops?
            if (loops != null) {
                SignalLoop s = loops.get(e.getKey());
                if (s != null) {
                    s.setDistortion((float) accuracy.getAccuracyLost());
                    s.setNormalizedDistortion(accuracy.getNormalizedAccuracyLost());
                    s.setMaxVal(accuracy.getMaxVal());
                    s.setMinVal(accuracy.getMaxVal());

                    s.setOriginalTime(speed.getDuration());
                    s.setTimeGain(speed.getDurationDiff());
                }
            }
        }
    }
}
