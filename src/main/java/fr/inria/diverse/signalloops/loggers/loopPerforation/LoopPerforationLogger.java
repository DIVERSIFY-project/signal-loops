package fr.inria.diverse.signalloops.loggers.loopPerforation;


import fr.inria.diverse.signalloops.loggers.LightLog;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by marodrig on 22/12/2014.
 */
public class LoopPerforationLogger extends LightLog {

    private final Boolean measuringAccuracy;

    public static class OutputDescription {
        private int parentLoop = -1;

        private ArrayList<Float> outputs = new ArrayList<Float>();

        public int getParentLoop() {
            return parentLoop;
        }

        public void setParentLoop(int parentLoop) {
            this.parentLoop = parentLoop;
        }

        public ArrayList<Float> getOutputs() {
            return outputs;
        }

        public void setOutputs(ArrayList<Float> outputs) {
            this.outputs = outputs;
        }

        public void clearOutputs() {
            outputs.clear();
            //outputs = new ArrayList<Float>();
        }
    }

    public static String ID_FILE_NAME = "loopPerforation.id";

    public static String LOOP_DURATION = "D";

    //Start of the loops
    private HashMap<Integer, Long> loopStart;

    //Total time spent on a loop
    private HashMap<Integer, Long> loopTotalDuration;

    //Number of instances running the same loop
    private HashMap<Integer, Long> loopsRunning;

    //The output indexed by the object producing it. NOT by the cycle.
    //This is because many instances can be executing the same cycle.
    private HashMap<Integer, OutputDescription> outputs;

    //Indicate to log in intervals of a logarithmic function of the size of the buffer to save mem
    private boolean useLogarithmicSpacingDataLog = false;

    //Number of samples that we are going to ignore till the next time we register
    private int samplesIgnored = 0;

    /**
     * Eventually initialize the maps for faster logging
     *
     * @throws java.io.IOException
     */
    private void initMaps() throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("file.txt"));
            loopStart = new HashMap<Integer, Long>();
            loopTotalDuration = new HashMap<Integer, Long>();

            String line = br.readLine();
            while (line != null) {
                int id = Integer.parseInt(line.split(" ")[0]);
                loopStart.put(id, -1L);
                loopTotalDuration.put(id, -1L);
                line = br.readLine();
            }
        } finally {
            if (br != null) br.close();
        }
    }

    public LoopPerforationLogger(Properties props) {
        super(props);
        loopStart = new HashMap<Integer, Long>();
        loopTotalDuration = new HashMap<Integer, Long>();
        loopsRunning = new HashMap<Integer, Long>();
        outputs = new HashMap<Integer, OutputDescription>();
        measuringAccuracy = Boolean.valueOf(props.getProperty("purpose", "false"));
        useLogarithmicSpacingDataLog = Boolean.valueOf(props.getProperty("logarithmic.spacing", "false"));

    }

    public void loopBefore(int id) {
        Long instances = loopsRunning.get(id);
        instances = instances == null ? 1 : instances++;
        loopsRunning.put(id, instances);

        Long start = loopStart.get(id);
        if (instances <= 0 && start != null && start != -1)
            throw new RuntimeException("Unexpected start of cycle");
        loopStart.put(id, System.nanoTime());
    }

    public <T> void arrayAccess(int id, T data) {
        OutputDescription description = outputs.get(id);
        if (description == null) {
            description = new OutputDescription();
            outputs.put(id, description);
            description.setParentLoop(id);
        }
        if ( samplesIgnored <= 0 ) {
            float f = ((Number) data).floatValue();
            description.getOutputs().add(f);
            samplesIgnored = useLogarithmicSpacingDataLog ?
                    (int) Math.ceil(Math.log10(description.getOutputs().size())) : 1;
        }
        samplesIgnored--;
    }

    public void loopAfter(int id) {
        long now = System.nanoTime();
        Long start = loopStart.get(id);
        if (start == null || start == -1 || start > now)
            throw new RuntimeException("Unexpected end of cycle");
        Long prev = loopTotalDuration.get(id);
        prev = prev == null ? (now - start) : prev + (now - start);
        loopTotalDuration.put(id, prev);

        Long instances = loopsRunning.get(id);
        instances = instances == null ? 0 : instances--;
        loopsRunning.put(id, instances);
        if (instances == 0) loopStart.put(id, -1L);

        if (measuringAccuracy) {
            flush();
            outputs.get(id).clearOutputs();
        }
    }

    @Override
    public void flush() {
        for (Map.Entry<Integer, Long> e : loopTotalDuration.entrySet()) {
            entryWithId(LOOP_DURATION, e.getKey(), e.getValue());
        }
        for (Map.Entry<Integer, OutputDescription> e : outputs.entrySet()) {
            try {
                String loopId = String.valueOf(e.getValue().getParentLoop());
                String path = dir.getAbsolutePath() + "/" + "output_" + loopId + "_" + logThreadName;

                FileOutputStream fileOut = new FileOutputStream(path);
                DataOutputStream objectOut = new DataOutputStream(new BufferedOutputStream(fileOut, 2048));
                objectOut.writeInt(e.getValue().getOutputs().size());
                for (Float d : e.getValue().getOutputs())
                    objectOut.writeDouble(d);
                objectOut.close();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }
        super.flush();
    }
}
