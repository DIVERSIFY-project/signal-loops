package fr.inria.diverse.signalloops.loggers.loopPerforation;


/**
 * User: Simon
 * Date: 7/23/13
 * Time: 10:10 AM
 */
public class ShutdownHookLog extends Thread {
    public void run() {
        Log.close();
    }
}
