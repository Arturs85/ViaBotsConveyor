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
    @FXML
    public Label agentRoles;
    @FXML
    public HBox hBox;
    public Label labelAgentState;
    @FXML
    public Button insertPartButton;
    FXMLLoader mLLoader;
    static GUIAgent guiAgent;
    boolean buttonsInitialised = false;
    boolean additionalButtonsInitialised = false;
    public Button beltButton;
    static int cellId = 0;

    void initButton() {// TODO when to call this method?
        insertPartButton.setOnAction(event -> {
            if (guiAgent != null) {
                guiAgent.sendUImessage(getItem().agentName, MessageContent.INSERT_PART.name());

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
                    hBox.getChildren().remove(beltButton);
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
            if (item.isHardwareReady)
                labelAgentState.setText("Ready");
            else
                labelAgentState.setText("Connecting");

            setGraphic(hBox);

        }

    }

    void addConveyorButtons() {
        if (beltButton == null) {
            beltButton = new Button("Belt");
            beltButton.setOnAction(event -> guiAgent.sendUImessage(getItem().agentName, MessageContent.TOGGLE_BELT.name()));
            hBox.getChildren().add(beltButton);
        } else {
            hBox.getChildren().add(beltButton);
        }
    }
}
