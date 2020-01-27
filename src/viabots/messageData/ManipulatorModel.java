package viabots.messageData;

import viabots.behaviours.ConeType;

import java.io.Serializable;
import java.util.Arrays;

public class ManipulatorModel implements Serializable {
    String agentName;
    int coneTypesCount = ConeType.values().length;
    public int[] conesAvailable = new int[coneTypesCount];
    int[] timesForFirstInsertion = new int[coneTypesCount];
    public int[] timesForNextInsertion = new int[coneTypesCount];
    public ConeType currentCone;

    public ManipulatorModel(String agentName, ConeType currentCone) {
        this.agentName = agentName;
        this.currentCone = currentCone;
        Arrays.fill(conesAvailable, 0);// for testing
        Arrays.fill(timesForFirstInsertion, 1000);// for testing
        Arrays.fill(timesForNextInsertion, 5000);// for testing

    }

    boolean isFirstInsertionDone = false;

    public int peekInsertionTime(ConeType coneType) {
        int index = coneType.ordinal();

        if (conesAvailable[index] > 0) {
            if (isFirstInsertionDone)
                return timesForNextInsertion[index];
            else
                return timesForFirstInsertion[index];
        } else
            return Integer.MAX_VALUE;
    }

    public int insert(ConeType coneType) {
        int index = coneType.ordinal();

        if (conesAvailable[index] > 0) {
            conesAvailable[index]--;//decrese cone count
            if (isFirstInsertionDone)
                return timesForNextInsertion[index];

            else {
                isFirstInsertionDone = true;
                return timesForFirstInsertion[index];
            }
        } else return Integer.MAX_VALUE;
    }


}
