package fr.inria.juncoprovider.testproject;

import java.util.List;

/**
 * Created by marcel on 23/02/14.
 * <p/>
 * A class to test some coverage. In some method an "explosive" line is introduced
 * which will not be tested.
 */
public class ControlFlowArithmetic {

    //several constructions one after the other
    public int mixed(int a, int b) {
        if (b % 2 == 0) {
            return a + b;
        }

        for (int i = 0; i < a; i++) b += a * b;

        return a + b * 2;
    }

    //several constructions one after the other
    public int invocation(int a, int b) {
        if (b % 2 == 0) {
            return nestedIfs(a,b);
        }
        return a + b * 2;
    }

    //Some nested ifs
    public int nestedIfs(int a, int b) {
        if (a > 0) {
            if (b > 0) return a * b;
        } else {
            if (b < 0) return a * b * b;
            else {
                a = a * b;
                b = b * b;
                return b;
            }
        }
        return 0;
    }

    public void returnVoid(int a) {
        if (a > 0) return;
        else System.out.print("A < 0!");
    }

    public int nestedConditional(int a) {
        int k = a / 2;
        int b = a > 0 ? k < 4 ? a * a : 8 : -a * a;
        return b;
    }

    public int conditional(int a) {
        int b = a > 0 ? a * a : -a * a;
        return b;
    }

    /**
     * A method with a while to test the control flow
     */
    public int ctDoWhile(List<Integer> a) {
        int b = 0;
        int i = 0;
        do b += i++ * b; while (i < a.size());
        return b;
    }

    /**
     * A method with a while to test the control flow
     */
    public int ctDoWhileBlock(List<Integer> a) {
        int b = 0;
        int i = 0;
        do {
            int k = i * i;
            b += i++ * b;
        } while (i < a.size());
        return b;
    }


    /**
     * A method with a while to test the control flow
     */
    public int ctWhile(List<Integer> a) {
        int b = 0;
        int i = 0;
        while (i < a.size()) b += i++ * b;
        return b;
    }

    /**
     * A method with a while to test the control flow
     */
    public int ctWhileBlock(List<Integer> a) {
        int b = 0;
        int i = 0;
        while (i < a.size()) {
            b += i * b;
            i++;
        }
        return b;
    }


    /**
     * A method with a foreach to tes the control flow
     */
    public int ctForEach(List<Integer> a) {
        int b = 0;
        for (int i : a) b += i * b;
        return b;
    }

    /**
     * A method with a foreach to tes the control flow
     */
    public int ctForEachBlock(List<Integer> a) {
        int b = 0;
        for (int i : a) {
            int k = i * i;
            b += k * b;
        }
        return b;
    }

    //A For to test the control flow in a for
    public int ctFor(int a, int b) {
        for (int i = 0; i < a; i++) b += a * b;
        return b;
    }

    //A For to test the control flow in a for
    public int ctForBlock(int a, int b) {
        for (int i = 0; i < a; i++) {
            int k = i * i;
            b += a * b + i;
        }
        return b;
    }

    //Yet another dummy procedure to test some logic branches
    public int ifThen(int a, int b) {
        if (b % 2 == 0) return a - b;
        return 0;
    }

    //Yet another dummy procedure to test some logic branches
    public int ifThenElse(int a, int b) {
        if (b % 2 == 0) return a - b;
        else b = b + a;
        return b * b;
    }


    //Yet another dummy procedure to test some logic branches
    public int ifThenBlock(int a, int b) {
        if (b % 2 == 0) {
            a += b * b;
            return a - b;
        }
        return 0;
    }

    //Yet another dummy procedure to test some logic branches
    public int ifThenElseBlock(int a, int b) {
        if (b % 2 == 0) {
            a += b * b;
            return a - b;
        } else {
            return a - b * 2;
        }
    }

    //All lines will be tested in this method
    public int simple(int a) {
        a = a + a / 2;
        return 10 * a;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////


    public int testCase1(boolean armed, double inputs1, double inputs2, double THRESHOLD) {
        int count = 0;
        double value = inputs1;
        if (value < -THRESHOLD) {
            armed = true;
        } else if (armed & (value > THRESHOLD)) {
            ++count;
            armed = false;
        }
        value = inputs2;
        if (value < -THRESHOLD) {
            armed = true;
        } else if (armed & (value > THRESHOLD)) {
            ++count;
            armed = false;
        }
        return count;
    }

    double method1() {
        return 0;
    }

    double method2(double a) {
        return a;
    }

    boolean m3() {
        return true;
    }

    public void complex1(double phase, double source, double target, double baseIncrement, boolean starved, int i,
                         double current, double[] outputs,double[] amplitudes, double[] rates) {
        if ((phase) >= 1.0) {
            while ((phase) >= 1.0) {
                source = target;
                phase -= 1.0;
                baseIncrement = method1();
            }
        } else if ((i == 0) && ((starved) || (!(m3())))) {
            source = target = current;
            phase = 0.0;
            baseIncrement = method1();
        }
        current = (((target) - (source)) * (phase)) + (source);
        outputs[i] = (current) * (amplitudes[i]);
        double phaseIncrement = (baseIncrement) * (rates[i]);
        phase += method2(phaseIncrement);
    }

}
