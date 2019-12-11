package GUI;

import javafx.beans.property.SimpleBooleanProperty;
import viabots.ManipulatorType;
import viabots.VSMRoles;
import viabots.behaviours.ConeType;

import java.util.EnumSet;

public class AgentInfo {

    ManipulatorType type;
    boolean isHardwareReady = false;
    String agentName;
    EnumSet<ConeType> enabledParts;
    EnumSet<VSMRoles> currentRoles;

    SimpleBooleanProperty isAEnabledProperty = new SimpleBooleanProperty(this, "isAEnabledProperty", false);

    public boolean getIsAEnabledProperty() {
        if (enabledParts.contains(ConeType.A)) return true;
        else return false;
    }

    public void setIsAEnabledProperty(boolean value) {
        if (value) enabledParts.add(ConeType.A);
        else enabledParts.remove(ConeType.A);

    }

    SimpleBooleanProperty isBEnabledProperty = new SimpleBooleanProperty(this, "isBEnabledProperty", false);

    public boolean getIsBEnabledProperty() {
        if (enabledParts.contains(ConeType.B)) return true;
        else return false;
    }

    public void setIsBEnabledProperty(boolean value) {
        if (value) enabledParts.add(ConeType.B);
        else enabledParts.remove(ConeType.B);

    }

    SimpleBooleanProperty isCEnabledProperty = new SimpleBooleanProperty(this, "isCEnabledProperty", false);

    public boolean getIsCEnabledProperty() {
        if (enabledParts.contains(ConeType.C)) return true;
        else return false;
    }

    public void setIsCEnabledProperty(boolean value) {
        if (value) enabledParts.add(ConeType.C);
        else enabledParts.remove(ConeType.C);

    }

    AgentInfo(String name, ManipulatorType type) {
        this.agentName = name;
        this.type = type;

        enabledParts = EnumSet.noneOf(ConeType.class);
        currentRoles = EnumSet.noneOf(VSMRoles.class);

    }

    String getName() {
        return agentName;
    }

    public ManipulatorType getType() {
        return type;
    }
}
