package viabots.messageData;

import viabots.ManipulatorType;
import viabots.VSMRoles;
import viabots.behaviours.ConeType;

import java.io.Serializable;
import java.util.EnumSet;

public class S1ToS2Message implements Serializable {
    //    public int[] availableConeCount;// index is enum coneType order nr
    public int[] timeForConeInsertion;// index is enum coneType order nr, INT.MAX means cant insert particular type
    public ConeType currentConeType;
    public String agentName;

    public S1ToS2Message(int[] timeForConeInsertion, ConeType currentConeType) {
        this.timeForConeInsertion = timeForConeInsertion;
        this.currentConeType = currentConeType;
    }
}
