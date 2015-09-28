package fr.inria.diverse.signalloops.outputProcessing;

import java.util.ArrayList;

/**
 * Created by marodrig on 20/07/2015.
 */
public class PerforationMetrics {

    private String position;
    private double normalized;
    private double maxVal;
    private double minVal;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Parent loop
     */
    private final int parentLoop;
    /**
     * Duration of the loop normally
     */
    private double duration = 0;

    /**
     * Duration of the loop perforated
     */
    private double perforatedDuration = 0;

    /**
     * Lost of accuracy
     */
    private double accuracyLost;

    private ArrayList<Double> after;

    public PerforationMetrics(int parentLoop) {
        this.parentLoop = parentLoop;
    }

    public PerforationMetrics(PerforationMetrics metrics) {
        this.maxVal = metrics.maxVal;
        this.minVal = metrics.minVal;
        this.accuracyLost = metrics.accuracyLost;
        this.normalized = metrics.normalized;
        this.duration = metrics.duration;
        this.perforatedDuration = metrics.perforatedDuration;
        this.position = metrics.position;
        this.parentLoop = metrics.parentLoop;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getPerforatedDuration() {
        return perforatedDuration;
    }

    public void setPerforatedDuration(double perforatedDuration) {
        this.perforatedDuration = perforatedDuration;
    }

    public double getAccuracyLost() {
        return accuracyLost;
    }

    public void setAccuracyLost(double accuracyLost) {
        this.accuracyLost = accuracyLost;
    }


    private ArrayList<Double> previousResults;


    public void addDuration(double millis) {
        duration += millis;
    }

    public void addAfterDuration(double millis) {
        perforatedDuration += millis;
    }


    public double getDurationDiff() {
        return duration - perforatedDuration;
    }

    public void printResults(boolean accuracy, boolean speed) {

        if ( accuracy ) {
            System.out.println("--------------------------");
            System.out.println("LOOP: " + parentLoop + " -> " + getPosition());
            System.out.println("Mean absolute error for " + parentLoop + " : " + getAccuracyLost());
            System.out.println("Normalized Mean absolute error for " + parentLoop + " : " + getNormalizedAccuracyLost());
            System.out.println("Max/Min: " + maxVal + " / " + minVal);
        }
        if ( speed )
            System.out.println("Duration diff " + parentLoop + " : " + getDurationDiff());
    }

    public void setNormalized(double normalized) {
        this.normalized = normalized;
    }

    public double getNormalizedAccuracyLost() {
        return normalized;
    }

    public void setMaxVal(double maxVal) {
        this.maxVal = maxVal;
    }

    public double getMaxVal() {
        return maxVal;
    }

    public void setMinVal(double minVal) {
        this.minVal = minVal;
    }

    public double getMinVal() {
        return minVal;
    }
}
