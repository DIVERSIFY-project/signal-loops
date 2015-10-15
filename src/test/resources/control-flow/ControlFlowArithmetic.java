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

    //Some nested ifs
    public int nestedIfs(int a, int b) {
        if (a > 0) {
            if (b > 0) return a * b;
            else return 0;
        } else {
            if (b < 0) return a * b * b;
            else {
                a = a * b;
                b = b * b;
                return b;
            }
        }
    }

    public void returnVoid(int a) {
        if (a > 0) return;
        else System.out.print("A < 0!");
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
}
