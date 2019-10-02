package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

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

    void initButton() {// TODO when to call this method?
        insertPartButton.setOnAction(event -> {
            if (guiAgent != null) {
                guiAgent.sendUImessage();
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

            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("AgentInfoListCell.fxml"));
                mLLoader.setController(this);

                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            agentName.setText(item.getName());
            if (item.isHardwareReady)
                labelAgentState.setText("Ready");
            else
                labelAgentState.setText("Connecting");

            setGraphic(hBox);

        }

    }

}
