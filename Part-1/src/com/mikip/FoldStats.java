package com.mikip;

/**
 * Created by mpokr on 3/19/2017.
 */
public class FoldStats {

    private int truePos;
    private int trueNeg;
    private int falsePos;
    private int falseNeg;

    public FoldStats() {
        truePos = 0;
        trueNeg = 0;
        falsePos = 0;
        falseNeg = 0;
    }

    public double getAccuracy() {
        return ((double) truePos + trueNeg) / (truePos + trueNeg + falsePos + falseNeg);
    }

    public double getPosPrecision() {
        return (double) truePos / (truePos + falsePos);
    }

    public double getPosRecall() {
        return (double) truePos / (truePos + falseNeg);
    }

    public double getNegPrecision() {
        return (double) trueNeg / (trueNeg + falseNeg);
    }

    public double getNegRecall() {
        return (double) trueNeg / (trueNeg + falsePos);
    }

    public double getPrecision() {
        return (getPosPrecision() + getNegPrecision()) / 2;
    }

    public double getRecall() {
        return (getPosRecall() + getNegRecall()) / 2;
    }

    public void incrementTruePos() {
        truePos++;
    }

    public void incrementTrueNeg() {
        trueNeg++;
    }

    public void incrementFalsePos() {
        falsePos++;
    }

    public void incrementFalseNeg() {
        falseNeg++;
    }

    public int getTotalTrue() {
        return truePos + trueNeg;
    }
}

