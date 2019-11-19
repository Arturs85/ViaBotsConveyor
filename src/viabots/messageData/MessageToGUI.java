package viabots.messageData;

import viabots.ManipulatorType;
import viabots.VSMRoles;
import viabots.behaviours.PartType;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

public class MessageToGUI implements Serializable {
    public boolean isHardwareReady = false;
    public ManipulatorType manipulatorType;
    public boolean isTakenDown = false;
    public EnumSet<PartType> enabledParts;
    public EnumSet<VSMRoles> currentRoles;

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

    public MessageToGUI(EnumSet<PartType> enabledParts) {//
        this.enabledParts = enabledParts;
    }
}
