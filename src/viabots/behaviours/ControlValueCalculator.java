package viabots.behaviours;

import viabots.Box;
import viabots.messageData.BoxMessage;

import java.util.ArrayList;

public class ControlValueCalculator {
    static ConeType[] conesPossible = ConeType.values();

    public double[] cVals = new double[conesPossible.length];
    public static double[] zeroes = new double[conesPossible.length];
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
