package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import viabots.ManipulatorType;
import viabots.messageData.MessageContent;

import java.io.IOException;

public class AgentInfoListCell extends ListCell<AgentInfo> {
    @FXML
    public Label agentName;

    public Label agentType;
    @FXML
    public Label agentRoles;
    @FXML
    public HBox baseHBox;
    public Label labelAgentState;
    @FXML
    public Button insertPartAButton;
    @FXML
    public Button insertPartBButton;

    FXMLLoader mLLoader;
    static GUIAgent guiAgent;
    boolean buttonsInitialised = false;
    boolean additionalButtonsInitialised = false;
    public Button beltButton;
    public Button boxButton;

    static int cellId = 0;

    void initButton() {// TODO when to call this method?
        insertPartAButton.setOnAction(event -> {
            if (guiAgent != null) {
                guiAgent.sendUImessage(getItem().agentName, MessageContent.INSERT_PART_A.name());

            }
        });
        insertPartBButton.setOnAction(event -> {
            if (guiAgent != null) {
                guiAgent.sendUImessage(getItem().agentName, MessageContent.INSERT_PART_B.name());

            }
        });
    }


    @Override
    protected void updateItem(AgentInfo item, boolean empty) {
        // super.updateItem(item, empty);
        if (getItem() != null) { // get old item
            //remove all bidirectional bindings and listeners

            // getItem().speedAProperty().removeListener(textListener);

        }
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);

        } else {
            if (getId() == null)
                setId(item.agentName + cellId++);

            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("AgentInfoListCell.fxml"));
                mLLoader.setController(this);

                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!item.getType().equals(ManipulatorType.CONVEYOR)) {
                if (beltButton != null) {
                    baseHBox.getChildren().remove(beltButton);
                    if (boxButton != null) {
                        baseHBox.getChildren().remove(boxButton);
                    }
                    //beltButton=null;
                    additionalButtonsInitialised = false;
                }
            } else {
                if (!additionalButtonsInitialised) {
                    addConveyorButtons();
                    additionalButtonsInitialised = true;
                }
            }

            if (!buttonsInitialised) {
                initButton();
                buttonsInitialised = true;

            }


            agentName.setText(item.getName());
            agentType.setText(item.getType().name());
            if (item.isHardwareReady)
                labelAgentState.setText("Ready");
            else
                labelAgentState.setText("Connecting");

            setGraphic(baseHBox);

        }

    }

    void addConveyorButtons() {
        if (beltButton == null) {
            beltButton = new Button("Belt");
            beltButton.setOnAction(event -> guiAgent.sendUImessage(getItem().agentName, MessageContent.TOGGLE_BELT.name()));
            baseHBox.getChildren().add(beltButton);
        } else {
            baseHBox.getChildren().add(beltButton);
        }

        if (boxButton == null) {
            boxButton = new Button("Box");
            boxButton.setOnAction(event -> guiAgent.sendUImessage(getItem().agentName, MessageContent.PLACE_BOX.name()));
            baseHBox.getChildren().add(boxButton);
        } else {
            baseHBox.getChildren().add(boxButton);
        }

    }
}
