package GUI;

import javafx.beans.property.*;
import viabots.ManipulatorType;
import viabots.VSMRoles;
import viabots.behaviours.ConeType;
import viabots.messageData.MessageToGUI;

import java.util.EnumSet;

public class AgentInfo {

    ManipulatorType type;
    // boolean isHardwareReady = false;
    String agentName;
    EnumSet<ConeType> enabledParts;
    EnumSet<VSMRoles> currentRoles;
    static GUIAgent guiAgent;


    ObjectProperty<Integer> objectPropConeAvailA = new SimpleObjectProperty<>(0);
    IntegerProperty coneAvailableCountA = IntegerProperty.integerProperty(objectPropConeAvailA);

    ObjectProperty<Integer> objectPropConeAvailB = new SimpleObjectProperty<>(0);
    IntegerProperty coneAvailableCountB = IntegerProperty.integerProperty(objectPropConeAvailB);

    ObjectProperty<Integer> objectPropConeAvailC = new SimpleObjectProperty<>(0);
    IntegerProperty coneAvailableCountC = IntegerProperty.integerProperty(objectPropConeAvailC);



    SimpleBooleanProperty isHardwareReady = new SimpleBooleanProperty(this, "isHardwareReady", false);

    public boolean getIsHardwareReadyProperty() {
        return isHardwareReady.get();
    }

    public SimpleBooleanProperty isHardwareReadyProperty() {
        return isHardwareReady;
    }

    public void setIsHardwareReadyProperty(boolean isHardwareReady) {
        this.isHardwareReady.set(isHardwareReady);
    }


    private final StringProperty currentRolesString = new SimpleStringProperty();

    public String getCurrentRolesString() {
        return currentRolesString.get();
    }

    public StringProperty currentRolesStringProperty() {
        return currentRolesString;
    }

    public void setCurrentRolesString(String currentRolesString) {
        this.currentRolesString.set(currentRolesString);
    }



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

    void setEnabledCones(EnumSet<ConeType> enabledCones) {
        enabledParts = enabledCones;

        isAEnabledProperty.set(enabledCones.contains(ConeType.A));
        isBEnabledProperty.set(enabledCones.contains(ConeType.B));
        isCEnabledProperty.set(enabledCones.contains(ConeType.C));


    }

    AgentInfo(String name, ManipulatorType type) {
        this.agentName = name;
        this.type = type;

        currentRolesString.addListener((observable, oldValue, newValue) -> {
            System.out.println("cur roles changed " + agentName);
        });

        enabledParts = EnumSet.noneOf(ConeType.class);
        currentRoles = EnumSet.noneOf(VSMRoles.class);


        coneAvailableCountA.addListener((observable, oldValue, newValue) -> {
            System.out.println(agentName + " info - changed coneAvailableCount: " + oldValue + " to " + newValue);
            if (guiAgent != null) {
                guiAgent.sendUImessage(agentName, new MessageToGUI(new int[]{newValue.intValue(), -1, -1}));
            }
        });
        coneAvailableCountB.addListener((observable, oldValue, newValue) -> {
            System.out.println(agentName + " info - changed coneAvailableCount: " + oldValue + " to " + newValue);
            if (guiAgent != null) {
                guiAgent.sendUImessage(agentName, new MessageToGUI(new int[]{-1, newValue.intValue(), -1}));
            }
        });
        coneAvailableCountC.addListener((observable, oldValue, newValue) -> {
            System.out.println(agentName + " info - changed coneAvailableCount: " + oldValue + " to " + newValue);
            if (guiAgent != null) {
                guiAgent.sendUImessage(agentName, new MessageToGUI(new int[]{-1, -1, newValue.intValue()}));
            }
        });
    }

    String getName() {
        return agentName;
    }

    public ManipulatorType getType() {
        return type;
    }
}
