package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

public class MainWindow {
    @FXML
    public ListView<AgentInfo> workingAgentsListView;
    public TextArea logTextArea;
    public Label labelOnTime;
    public Label labelProcessedBoxes;
    public Label labelSecPerBox;
    public CheckBox checkBoxUsePrediction;
    public CheckBox checkBoxUseCvalues;
    public Button buttonShowParameters;
    int previousCarrerPos = 0;
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

    public void appendTextWOAutoscroll(String text) {

        previousCarrerPos = logTextArea.caretPositionProperty().get();
        logTextArea.appendText(text);
        if (logTextArea.isFocused())
            logTextArea.positionCaret(previousCarrerPos);
    }
}
