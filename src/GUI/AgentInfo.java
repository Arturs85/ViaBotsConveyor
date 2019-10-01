package GUI;

import viabots.ManipulatorType;

public class AgentInfo {

    ManipulatorType type;
    boolean isHardwareReady = false;

    AgentInfo(ManipulatorType type) {
        this.type = type;

    }

    String getName() {
        return type.name();
    }

}
