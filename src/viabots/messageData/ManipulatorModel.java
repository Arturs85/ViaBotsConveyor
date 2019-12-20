package viabots.messageData;

import viabots.behaviours.ConeType;

public class ManipulatorModel {
    String agentName;
    int[] conesAvailable;
    int[] timesForFirstInsertion;
    int[] timesForNextInsertion;
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
