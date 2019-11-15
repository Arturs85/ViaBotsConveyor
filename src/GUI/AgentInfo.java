package GUI;

import viabots.ManipulatorType;
import viabots.behaviours.PartType;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class AgentInfo {

    ManipulatorType type;
    boolean isHardwareReady = false;
    String agentName;
    final Set<PartType> enabledParts; 
    AgentInfo(String name, ManipulatorType type) {
        this.agentName = name;
        this.type = type;

        enabledParts = EnumSet.noneOf(PartType.class);
    }

    String getName() {
        return agentName;
    }

    public ManipulatorType getType() {
        return type;
    }
}
