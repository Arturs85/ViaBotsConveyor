package GUI;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    SimpleBooleanProperty isAEnabledProperty = new SimpleBooleanProperty(this, "isAEnabledProperty", false);

    public boolean getIsAEnabledProperty() {
        if (enabledParts.contains(PartType.A)) return true;
        else return false;
    }

    public void setIsAEnabledProperty(boolean value) {
        if (value) enabledParts.add(PartType.A);
        else enabledParts.remove(PartType.A);

    }

    SimpleBooleanProperty isBEnabledProperty = new SimpleBooleanProperty(this, "isBEnabledProperty", false);

    public boolean getIsBEnabledProperty() {
        if (enabledParts.contains(PartType.B)) return true;
        else return false;
    }

    public void setIsBEnabledProperty(boolean value) {
        if (value) enabledParts.add(PartType.B);
        else enabledParts.remove(PartType.B);

    }

    SimpleBooleanProperty isCEnabledProperty = new SimpleBooleanProperty(this, "isCEnabledProperty", false);

    public boolean getIsCEnabledProperty() {
        if (enabledParts.contains(PartType.C)) return true;
        else return false;
    }

    public void setIsCEnabledProperty(boolean value) {
        if (value) enabledParts.add(PartType.C);
        else enabledParts.remove(PartType.C);

    }

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
