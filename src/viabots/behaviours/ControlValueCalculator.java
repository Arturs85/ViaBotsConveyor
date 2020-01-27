package viabots.behaviours;

import viabots.Box;
import viabots.messageData.BoxMessage;

import java.util.ArrayList;

public class ControlValueCalculator {
    ConeType[] conesPossible = ConeType.values();
    public double[] cVals = new double[conesPossible.length];

    void processNewBox(BoxMessage boxMessage) {
        // value is proportional to nr of cones
        for (int i = 0; i < conesPossible.length; i++) {
            ArrayList<Integer> positions = Box.getPositions(conesPossible[i], boxMessage.boxType);
            cVals[i] = positions.size();
        }
    }

}
