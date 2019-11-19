package GUI;

import viabots.ManipulatorType;
import viabots.VSMRoles;
import viabots.behaviours.PartType;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class AgentInfo {

    ManipulatorType type;
    boolean isHardwareReady = false;
    String agentName;
    EnumSet<PartType> enabledParts;
    EnumSet<VSMRoles> currentRoles;

    AgentInfo(String name, ManipulatorType type) {
        this.agentName = name;
        this.type = type;

        enabledParts = EnumSet.noneOf(PartType.class);
        currentRoles = EnumSet.noneOf(VSMRoles.class);

    }

    String getName() {
        return agentName;
    }

    public ManipulatorType getType() {
        return type;
    }
}
