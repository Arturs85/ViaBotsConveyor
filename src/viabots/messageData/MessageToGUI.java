package viabots.messageData;

import viabots.ManipulatorType;

import java.io.Serializable;

public class MessageToGUI implements Serializable {
    public boolean isHardwareReady = false;
    public ManipulatorType manipulatorType;
    public boolean isTakenDown = false;

    public MessageToGUI(boolean isHardwareReady, ManipulatorType type) {
        this.isHardwareReady = isHardwareReady;
        this.manipulatorType = type;
    }

    public MessageToGUI(boolean isHardwareReady, ManipulatorType type, boolean isTakenDown) {
        this.isHardwareReady = isHardwareReady;
        this.manipulatorType = type;
        this.isTakenDown = isTakenDown;
    }
}
