package fr.inria.diverse.signalloops.loggers.buildMicrobenchmark;


import fr.inria.diverse.signalloops.loggers.LightLog;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 * Created by marodrig on 22/12/2014.
 */
public class MicrobenchmarkLogger extends LightLog {

    static byte byte_type = 0;
    static byte short_type = 1;
    static byte int_type = 2;
    static byte long_type = 3;
    static byte float_type = 4;
    static byte double_type = 5;
    static byte char_type = 6;
    static byte String_type = 7;
    static byte boolean_type = 8;

    static byte byte_type_array = 10;
    static byte short_type_array = 11;
    static byte int_type_array = 12;
    static byte long_type_array = 13;
    static byte float_type_array = 14;
    static byte double_type_array = 15;
    static byte char_type_array = 16;
    static byte String_type_array = 17;
    static byte boolean_type_array = 18;


    HashSet<String> varRegistered = new HashSet<String>();
    HashMap<String, DataOutputStream> data = new HashMap<String, DataOutputStream>();

    public MicrobenchmarkLogger(Properties props) {
        super(props);
    }


    public void logbyte(byte data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeByte(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArraybyte(byte[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeByte(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logshort(short data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeShort(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArrayshort(short[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeShort(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logint(int data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArrayint(int[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeInt(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void loglong(long data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeLong(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArraylong(long[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeLong(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logfloat(float data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeFloat(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArrayfloat(float[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeFloat(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logdouble(double data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeDouble(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArraydouble(double[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeDouble(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logchar(char data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeChar(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArraychar(char[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeChar(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logString(String data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeChars(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArrayString(String[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeChars(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logboolean(boolean data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeBoolean(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public void logArrayboolean(boolean[] data, String name) {
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeBoolean(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DataOutputStream getStream(String name) {
        name = name.substring(0, name.lastIndexOf("-")) + "--" +
                Thread.currentThread().getName() + "-" +
                this.getClass().getClassLoader().toString();
        if (data.containsKey(name)) return data.get(name);
        try {
            DataOutputStream stream = new DataOutputStream(new FileOutputStream("log/" + name));
            data.put(name, stream);
            return stream;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flush() {
        super.flush();
        try {
            for (DataOutputStream s : data.values()) {
                s.flush();
                s.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        data.clear();
    }
}
