package viabots.behaviours;

import viabots.Box;
import viabots.messageData.BoxMessage;

import java.util.ArrayList;
import java.util.Arrays;

public class ControlValueCalculator {
    static ConeType[] conesPossible = ConeType.values();

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
    }

    void increaseAskersVal(ConeType askersConeType) {
        cVals[askersConeType.ordinal()]++;

    }

    void addPrediction(double[] predictedAvgConeCount) {
        for (int i = 0; i < cVals.length; i++) {
            cVals[i] = (cVals[i] + predictedAvgConeCount[i]) / 2;
        }
    }


}
