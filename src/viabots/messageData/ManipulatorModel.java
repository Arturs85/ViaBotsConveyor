package viabots.messageData;

import viabots.behaviours.ConeType;

import java.io.Serializable;

public class ManipulatorModel implements Serializable {
    String agentName;
    int coneTypesCount = ConeType.values().length;
    int[] conesAvailable = new int[coneTypesCount];
    int[] timesForFirstInsertion = new int[coneTypesCount];
    int[] timesForNextInsertion = new int[coneTypesCount];
    ConeType currentCone;

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
            if (isFirstInsertionDone)
                return timesForNextInsertion[index];
            else {
                isFirstInsertionDone = true;
                return timesForFirstInsertion[index];
            }
        } else return Integer.MAX_VALUE;
    }


}
