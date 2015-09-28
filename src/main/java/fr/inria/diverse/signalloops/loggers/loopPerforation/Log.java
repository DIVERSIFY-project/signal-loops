package fr.inria.diverse.signalloops.loggers.loopPerforation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * User: Simon
 * Date: 7/23/13
 * Time: 10:07 AM
 */
public class Log {

    static Properties props;

    //Static variable to indicate we are measurin accuracy in this run
    static boolean measuringAccuracy = false;

    static {
        props = new Properties();
        File storeLog;
        try {
            props.load(new BufferedReader(new FileReader("log/perforation.properties")));
            String logFileName = props.getProperty("log.dir", "log");
            int i = 0;
            File f = new File(logFileName);
            while (f.exists()) {
                f = new File(logFileName + "-" + String.valueOf(i));
                i++;
            }

            props.put("log.dir", "log/"+f.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<Thread, LoopPerforationLogger> logs = null;

    private static Object init = init();

    protected static Object init() {
        ShutdownHookLog shutdownHook = new ShutdownHookLog();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        return null;
    }

    protected static LoopPerforationLogger getLog() {
        return getLog(Thread.currentThread());
    }

    protected static LoopPerforationLogger getLog(Thread thread) {
        if (logs == null) {
            logs = new HashMap<Thread, LoopPerforationLogger>();
        }
        if (logs.containsKey(thread)) {
            return logs.get(thread);
        } else {
            LoopPerforationLogger l = new LoopPerforationLogger(props);

            logs.put(thread, l);
            return l;
        }
    }

    public static <T> void arrayAccess(int id, T data) {
        getLog().arrayAccess(id, data);
    }

    public static void loopBefore(int id) {
        getLog().loopBefore(id);
    }

    public static void loopAfter(int id) {
        getLog().loopAfter(id);
    }


    public static void close() {
        for (LoopPerforationLogger l : logs.values()) {
            l.close();
        }
    }
}