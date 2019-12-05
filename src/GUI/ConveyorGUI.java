package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

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
}
