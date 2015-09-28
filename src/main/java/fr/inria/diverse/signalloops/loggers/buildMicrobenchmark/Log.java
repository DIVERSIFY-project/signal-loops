package fr.inria.diverse.signalloops.loggers.buildMicrobenchmark;

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

            props.put("log.dir", "log/" + f.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<Thread, MicrobenchmarkLogger> logs = null;

    private static Object init = init();

    protected static Object init() {
        ShutdownHookLog shutdownHook = new ShutdownHookLog();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        return null;
    }

    protected static MicrobenchmarkLogger getLog() {
        return getLog(Thread.currentThread());
    }

    protected static MicrobenchmarkLogger getLog(Thread thread) {
        if (logs == null) logs = new HashMap<Thread, MicrobenchmarkLogger>();
        if (logs.containsKey(thread)) return logs.get(thread);
        else {
            MicrobenchmarkLogger l = new MicrobenchmarkLogger(props);
            logs.put(thread, l);
            return l;
        }
    }

    public static void logbyte(byte data, String name) {
        getLog().logbyte(data, name);
    }

    public static void logshort(short data, String name) {
        getLog().logshort(data, name);
    }

    public static void logint(int data, String name) {
        getLog().logint(data, name);
    }

    public static void loglong(long data, String name) {
        getLog().loglong(data, name);
    }

    public static void logfloat(float data, String name) {
        getLog().logfloat(data, name);
    }

    public static void logdouble(double data, String name) {
        getLog().logdouble(data, name);
    }

    public static void logchar(char data, String name) {
        getLog().logchar(data, name);
    }

    public static void logString(String data, String name) {
        getLog().logString(data, name);
    }

    public static void logboolean(boolean data, String name) {
        getLog().logboolean(data, name);
    }

    public static void logArraybyte(byte[] data, String name) {
        getLog().logArraybyte(data, name);
    }

    public static void logArrayshort(short[] data, String name) {
        getLog().logArrayshort(data, name);
    }

    public static void logArrayint(int[] data, String name) {
        getLog().logArrayint(data, name);
    }

    public static void logArraylong(long[] data, String name) {
        getLog().logArraylong(data, name);
    }

    public static void logArrayfloat(float[] data, String name) {
        getLog().logArrayfloat(data, name);
    }

    public static void logArraydouble(double[] data, String name) {
        getLog().logArraydouble(data, name);
    }

    public static void logArraychar(char[] data, String name) {
        getLog().logArraychar(data, name);
    }

    public static void logArrayString(String[] data, String name) {
        getLog().logArrayString(data, name);
    }

    public static void logArrayboolean(boolean[] data, String name) {
        getLog().logArrayboolean(data, name);
    }

    public static void close() {
        getLog().close();
        /*
        for (MicrobenchmarkLogger l : logs.values()) {
            l.close();
        }*/
    }
}