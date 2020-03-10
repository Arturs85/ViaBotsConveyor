package viabots.messageData;

import viabots.ManipulatorType;
import viabots.behaviours.ConeType;

import java.io.Serializable;
import java.util.Arrays;

public class ManipulatorModel implements Serializable {

    static int manipTypesCount = ManipulatorType.values().length;
   public static int[][] firstInsertionTimes= {{},{2000,2000},{1000,1000},{1100,1100},{}};//first index - manip type ordinal, second index- cone type ordinal
   public static int[][] nextInsertionTimes= {{},{5000,5000},{4000,4000},{4100,4100},{}};//first index - manip type ordinal, second index- cone type ordinal


    String agentName;
   static int coneTypesCount = ConeType.values().length;
    public int[] conesAvailable = new int[coneTypesCount];
    int[] timesForFirstInsertion = new int[coneTypesCount];
    public int[] timesForNextInsertion = new int[coneTypesCount];
    public ConeType currentCone;
ManipulatorType manipulatorType;
    public ManipulatorModel(String agentName, ConeType currentCone,ManipulatorType manipulatorType) {
        this.agentName = agentName;
        this.currentCone = currentCone;
        this.manipulatorType = manipulatorType;
        Arrays.fill(conesAvailable, 0);// for testing

        timesForFirstInsertion= firstInsertionTimes[manipulatorType.ordinal()];
       timesForNextInsertion = nextInsertionTimes[manipulatorType.ordinal()];

//        Arrays.fill(timesForFirstInsertion, 1000);// for testing
//        Arrays.fill(timesForNextInsertion, 5000);// for testing

    }
public String toString(){
        return " ConesAvail: "+conesAvailable[0]+" "+conesAvailable[1]+" "+conesAvailable[2]+" CurType: "+currentCone;
}
    boolean isFirstInsertionDone = false;

public void reset(){
    isFirstInsertionDone=false;
}
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
