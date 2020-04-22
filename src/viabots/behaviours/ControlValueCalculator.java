package viabots.behaviours;

import viabots.Box;
import viabots.BoxType;
import viabots.messageData.BoxMessage;

import java.util.ArrayList;
import java.util.Arrays;

public class ControlValueCalculator {
    static ConeType[] conesPossible = ConeType.values();
    BoxType previousBoxType;
    BoxType currentBoxType;
    double[] previousCvals;

    public double[] cVals = new double[conesPossible.length];
    public static double[] zeroes = new double[conesPossible.length];

    public ControlValueCalculator() {
       Arrays.fill(zeroes,5);// positive value, larger than manipulators count
    }

    void processNewBox(BoxMessage boxMessage) {
        // value is proportional to nr of cones
        for (int i = 0; i < conesPossible.length; i++) {
            ArrayList<Integer> positions = Box.getPositions(conesPossible[i], boxMessage.boxType);
            cVals[i] = positions.size();
        }
        currentBoxType = boxMessage.boxType;
    }

    void increaseAskersVal(ConeType askersConeType) {
        cVals[askersConeType.ordinal()]++;

    }

    void addPrediction(double[] predictedAvgConeCount) {
        for (int i = 0; i < cVals.length; i++) {
            cVals[i] = (cVals[i]*0.2 + predictedAvgConeCount[i]*0.8) ;
        }
    }

    void addPredictionOneStepForward(BoxType curBox, BoxType nextBox, BoxType thirdBox) { // todo - test
        if (currentBoxType != curBox) {//checking whether prediction is at right offset
            System.out.println("--!!--!!--!!-- prediction does not match actual --!!--!!--!!--");
        }
        if (nextBox.equals(previousBoxType) && !curBox.equals(previousBoxType)) {
            cVals = previousCvals;
        }

        previousCvals = Arrays.copyOf(cVals,cVals.length);
        previousBoxType = currentBoxType;
    }
    //for one different box dont change values compleatly, but use one for positions in actula box that are needed to fill, so that s2 dont need to ask for increase
    void mergeCVals(){


    }

}
