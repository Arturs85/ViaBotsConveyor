package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import viabots.BoxType;
import viabots.messageData.BoxParamsMsg;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class ConveyorGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    GUIAgent owner;
    MainWindow controller;
    static Stage classStage = new Stage();

    public ConveyorGUI(GUIAgent guiAgent) {
        owner = guiAgent;

    }


    @Override
    public void start(Stage primaryStage) {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("ViaBots Conveyor");
        primaryStage.setScene(new Scene(root, 1000, 675));
        controller = loader.getController();
        controller.setOwner(owner);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                System.out.println("Program closing...");
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.show();


    }
    void showParamsDialog(){

        // Create the custom dialog.
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Box type and pattern definition");
        dialog.setHeaderText("Example for box A with big cones and box B with small cones {0,1,0,0,1,0},{1,0,1,1,0,1}");

// Set the icon (must be included in the project).
        URL url = this.getClass().getResource("/images/boxIndexes.png");
       if(url!=null)
        dialog.setGraphic(new ImageView(url.toString()));
else
           System.out.println("No image for dialog found");
// Set the button types.
        ButtonType loginButtonType = new ButtonType("Set parameters", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
grid.setPrefWidth(600);
        TextField boxContentsDef = new TextField();
        boxContentsDef.setText("{0,1,0,0,1,0},{1,0,1,1,0,1}");
        //boxContentsDef.setPromptText("Boxes Content Definition");
        TextField boxPattern = new TextField();
        boxPattern.setText("A,A,A,A,B,B");
        TextField sensorPositions = new TextField();
        sensorPositions.setText("500,1500,2100,2800,3600");
sensorPositions.setPrefWidth(300);
        grid.add(new Label("Boxes Type definition (max 3):"), 0, 0);
        grid.add(boxContentsDef, 1, 0);
        grid.add(new Label("Incoming Boxes pattern:"), 0, 1);
        grid.add(boxPattern, 1, 1);
        grid.add(new Label("Sensor positions:"), 0, 2);
        grid.add(sensorPositions, 1, 2);
// Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
      //  loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
//        boxContentsDef.textProperty().addListener((observable, oldValue, newValue) -> {
//            loginButton.setDisable(newValue.trim().isEmpty());
//        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> boxContentsDef.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new String[]{boxContentsDef.getText(), boxPattern.getText(),sensorPositions.getText()};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();

        result.ifPresent(paramsString -> {
            System.out.println("Box types =" + paramsString[0] + ", pattern =" + paramsString[1]);
      BoxType pattern[]= BoxParamsMsg.parsePatternFromString(paramsString[1]);
      int boxType[][] = BoxParamsMsg.parseBoxContentsFromString(paramsString[0]);
      int[] sensPos = BoxParamsMsg.parseSensorPositionsFromString(paramsString[2]);
      if(pattern!= null) {System.out.println("pattern ok: "+pattern.toString());
            if(boxType!= null){ System.out.println("boxes definition ok: "+boxType.toString());


            BoxParamsMsg msg = new BoxParamsMsg(pattern,boxType);
            msg.sensorPositions = sensPos;
            owner.sendParametersMessage(msg);

      }
      }
        });
    }

}
