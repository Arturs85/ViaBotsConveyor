package GUI;

import viabots.ManipulatorType;

public class AgentInfo {

    ManipulatorType type;
    boolean isHardwareReady = false;
    String agentName;

    AgentInfo(String name, ManipulatorType type) {
        this.agentName = name;
        this.type = type;

    }

    String getName() {
        return agentName;
    }

    public ManipulatorType getType() {
        return type;
    }
}
