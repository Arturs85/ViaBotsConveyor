package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.converter.BooleanStringConverter;
import viabots.ManipulatorType;
import viabots.behaviours.ConeType;
import viabots.messageData.MessageContent;
import viabots.messageData.MessageToGUI;

import java.io.IOException;
import java.util.ArrayList;

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
    public CheckBox checkBoxA;
    public CheckBox checkBoxC;
    public CheckBox checkBoxB;
    @FXML

    public Spinner<Integer> spinnerConeCountA;
    public Spinner<Integer> spinnerConeCountB;
    public Spinner<Integer> spinnerConeCountC;
    public GridPane gridPane;


    FXMLLoader mLLoader;
    static GUIAgent guiAgent;
    boolean buttonsInitialised = false;
    boolean additionalButtonsInitialised = false;
    public Button beltButton;
    public Button boxButton;
    final int initialValueSpinner = 0;
    final int incrementValueSpinner = 10;
    static int cellId = 0;
    BooleanStringConverter bsc = new BooleanStringConverter() {
        @Override
        public String toString(Boolean value) {
            if (value)
                return ("Ready");
            else
                return ("Connecting");

        }
    };
    SpinnerValueFactory<Integer> valueFactoryA = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 250, initialValueSpinner,incrementValueSpinner);
    SpinnerValueFactory<Integer> valueFactoryB = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 250, initialValueSpinner,incrementValueSpinner);
    SpinnerValueFactory<Integer> valueFactoryC = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 250, initialValueSpinner,incrementValueSpinner);

    void spinnerSetup() {
//        spinnerConeCountA.setValueFactory(valueFactory);
//        spinnerConeCountB.setValueFactory(valueFactory);
//        spinnerConeCountC.setValueFactory(valueFactory);
        if (true) {//spinnerConeCountA == null) {
            spinnerConeCountA = new Spinner<Integer>(valueFactoryA);
            spinnerConeCountB = new Spinner<Integer>(valueFactoryB);
            spinnerConeCountC = new Spinner<Integer>(valueFactoryC);

            spinnerConeCountA.setId("spin");
            spinnerConeCountB.setId("spin");
            spinnerConeCountC.setId("spin");
        }

//        for (Node n : gridPane.getChildren()) {
//            if (n.getId() != null && n.getId().equals("spin") == spinnerConeCountA) {
//                n.setDisable(false);
//                return;
//            }
//        }
        gridPane.add(spinnerConeCountA, 1, 3);
        gridPane.add(spinnerConeCountB, 2, 3);
        gridPane.add(spinnerConeCountC, 3, 3);


    }

    void disableSpinners() {
        for (Node n : gridPane.getChildren()) {
            if (n.getId() != null && n == spinnerConeCountA) {
                n.setDisable(true);
            }
        }
    }

    void spinnerRemoval() {
        ArrayList<Node> forRemoval = new ArrayList<>();

        for (Node n : gridPane.getChildren()) {
            if (n.getId() != null && n.getId().equals("spin")) {
                // n.setDisable(true);//test
                forRemoval.add(n);

            }
        }
        gridPane.getChildren().removeAll(forRemoval);
    }

    //    void addSpinnerListeners(){
//        spinnerConeCountA.se
//    }
//
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

    void initCheckBoxes() {

        checkBoxA.setOnAction(event -> {
            if (checkBoxA.isSelected())
                getItem().enabledParts.add(ConeType.A);
            else
                getItem().enabledParts.remove(ConeType.A);

            if (guiAgent != null) {
                guiAgent.sendUImessage(getItem().agentName, new MessageToGUI(getItem().enabledParts));
            }
        });
        checkBoxB.setOnAction(event -> {
            if (checkBoxB.isSelected())
                getItem().enabledParts.add(ConeType.B);
            else
                getItem().enabledParts.remove(ConeType.B);

            if (guiAgent != null) {
                guiAgent.sendUImessage(getItem().agentName, new MessageToGUI(getItem().enabledParts));
            }
        });
        checkBoxC.setOnAction(event -> {
            if (checkBoxC.isSelected())
                getItem().enabledParts.add(ConeType.C);
            else
                getItem().enabledParts.remove(ConeType.C);

            if (guiAgent != null) {
                guiAgent.sendUImessage(getItem().agentName, new MessageToGUI(getItem().enabledParts));
            }
        });
    }


    @Override
    protected void updateItem(AgentInfo item, boolean empty) {
        //   super.updateItem(item, empty);
        if (getItem() != null) { // get old item
            //remove all bidirectional bindings and listeners

            //getItem().speedAProperty().removeListener(textListener);
            checkBoxA.selectedProperty().unbindBidirectional(getItem().isAEnabledProperty);
            checkBoxB.selectedProperty().unbindBidirectional(getItem().isBEnabledProperty);
            checkBoxC.selectedProperty().unbindBidirectional(getItem().isCEnabledProperty);
            agentRoles.textProperty().unbindBidirectional(getItem().currentRolesStringProperty());
            labelAgentState.textProperty().unbindBidirectional(getItem().isHardwareReady);

            if (spinnerConeCountA != null) {
                spinnerConeCountA.getValueFactory().valueProperty().unbindBidirectional(getItem().objectPropConeAvailA);
            }
            if (spinnerConeCountB != null) {
                spinnerConeCountB.getValueFactory().valueProperty().unbindBidirectional(getItem().objectPropConeAvailB);
            }
            if (spinnerConeCountC != null) {
                spinnerConeCountC.getValueFactory().valueProperty().unbindBidirectional(getItem().objectPropConeAvailC);
            }

        }
//        if (spinnerConeCountA != null) {
//            spinnerConeCountA.addEventFilter(MouseEvent.ANY, event -> {
//                event.consume();
//                System.out.println("spinner event");
//            });
//        }
        if (gridPane != null) spinnerRemoval();

        spinnerConeCountA = null;
        spinnerConeCountB = null;
        spinnerConeCountC = null;

        super.updateItem(item, empty);
        if (empty || item == null) {
            //  setText(null);
            setGraphic(null);
//if(item== null) System.out.println("Item null listview");
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
            checkBoxA.selectedProperty().bindBidirectional(item.isAEnabledProperty);
            checkBoxB.selectedProperty().bindBidirectional(item.isBEnabledProperty);
            checkBoxC.selectedProperty().bindBidirectional(item.isCEnabledProperty);
            agentRoles.textProperty().bindBidirectional(item.currentRolesStringProperty());
            labelAgentState.textProperty().bindBidirectional(item.isHardwareReady, bsc);

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
            if (item.getType().equals(ManipulatorType.BAXTER) || item.getType().equals(ManipulatorType.IRB120) || item.getType().equals(ManipulatorType.SMALL_ONE)
            || item.getType().equals(ManipulatorType.SIM_BAXTER) || item.getType().equals(ManipulatorType.SIM_IRB120) || item.getType().equals(ManipulatorType.SIM_SMALL_ONE)) {
                spinnerSetup();// right place?
                if (spinnerConeCountA != null)
                    spinnerConeCountA.getValueFactory().valueProperty().bindBidirectional(item.objectPropConeAvailA);
                if (spinnerConeCountB != null)
                    spinnerConeCountB.getValueFactory().valueProperty().bindBidirectional(item.objectPropConeAvailB);
                if (spinnerConeCountC != null)
                    spinnerConeCountC.getValueFactory().valueProperty().bindBidirectional(item.objectPropConeAvailC);



            } else {
                spinnerRemoval();
            }


            if (!buttonsInitialised) {
                initButton();
                initCheckBoxes();
                buttonsInitialised = true;

            }


            agentName.setText(item.getName());
            agentType.setText(item.getType().name());
//            if (item.currentRoles != null) {
//                agentRoles.setText(item.currentRoles.toString());
//            } else {
//                agentRoles.setText("[]");
//
//            }

//
//            if (item.isHardwareReady)
//                labelAgentState.setText("Ready");
//            else
//                labelAgentState.setText("Connecting");

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
