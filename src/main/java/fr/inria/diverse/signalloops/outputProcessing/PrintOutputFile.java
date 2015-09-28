package fr.inria.diverse.signalloops.outputProcessing;

import java.io.*;
import java.util.*;

/**
 * Created by marodrig on 23/07/2015.
 */
public class PrintOutputFile {

    private static List<File> loopOutputFiles(String path, Integer loop) {
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

    public static void main(String[] args) throws Exception {

        String previousLogPath = "C:\\MarcelStuff\\DATA\\DIVERSE\\output_programs\\jsyn\\log\\7-22-15_11-26_PM";
        String logPath = "C:\\MarcelStuff\\DATA\\DIVERSE\\output_programs\\jsyn\\log\\7-22-15_11-26_PM-0";
        //int loop = 8;
        int loop = 215;

        System.out.println("--------------------------");
        System.out.println("LOOP: " + loop + " -> " + loop);
        //Locate output files for the loop
        //Before
        List<File> beforeFiles = loopOutputFiles(previousLogPath, loop);
        List<File> afterFiles = loopOutputFiles(logPath, loop);

        if (beforeFiles.size() != afterFiles.size()) {
            System.out.println("LOOP SKIPPED. Mismatching output size (files) for loop: " + loop +
                    " Before: " + beforeFiles.size() + " After: " + afterFiles.size());
            //return;
        }


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



                StringBuilder sb = new StringBuilder("index, before, after \n");
                for (int i = 0; i < Math.max(sizeAfter, sizeBefore); i++) {
                    double beforeVal = (i < sizeBefore) ? inBefore.readDouble() : 0.0;
                    double afterVal = (i < sizeAfter) ? inAfter.readDouble() : 0.0;
                    sb.append(i + ", " + beforeVal + "," + afterVal + "\n");
                }
                OutputStream outputStream = new FileOutputStream(loop + "1.cvs");
                outputStream.write(sb.toString().getBytes());
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
