package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.util.Callback;

public class MainWindow {
    @FXML
    public ListView<AgentInfo> workingAgentsListView;
    public TextArea logTextArea;
    public Label labelOnTime;
    public Label labelProcessedBoxes;
    public Label labelSecPerBox;

    GUIAgent owner;

    public void setOwner(GUIAgent owner) {
        this.owner = owner;

        workingAgentsListView.setCellFactory(new Callback<ListView<AgentInfo>, ListCell<AgentInfo>>() {
            @Override
            public ListCell<AgentInfo> call(ListView<AgentInfo> param) {

                return new AgentInfoListCell();
            }
        });
    }
}
