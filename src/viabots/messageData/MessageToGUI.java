package viabots.messageData;

import viabots.ManipulatorType;
import viabots.VSMRoles;
import viabots.behaviours.ConeType;

import java.io.Serializable;
import java.util.EnumSet;

public class MessageToGUI implements Serializable {
    public boolean isHardwareReady = false;
    public ManipulatorType manipulatorType;
    public boolean isTakenDown = false;
    public EnumSet<ConeType> enabledParts;
    public EnumSet<VSMRoles> currentRoles;
    public int[] coneCount;// 0- A, 1-B, ...

    public MessageToGUI(boolean isHardwareReady, ManipulatorType type, EnumSet<VSMRoles> roles) {
        this.isHardwareReady = isHardwareReady;
        this.manipulatorType = type;
        this.currentRoles = roles;
    }
    public MessageToGUI(boolean isHardwareReady, ManipulatorType type) {
        this.isHardwareReady = isHardwareReady;
        this.manipulatorType = type;
    }

    public MessageToGUI(boolean isHardwareReady, ManipulatorType type, boolean isTakenDown) {
        this.isHardwareReady = isHardwareReady;
        this.manipulatorType = type;
        this.isTakenDown = isTakenDown;
    }

    public MessageToGUI(EnumSet<ConeType> enabledParts) {//
        this.enabledParts = enabledParts;
    }

    public MessageToGUI(int[] coneCount) {
        this.coneCount = coneCount;
    }
}
