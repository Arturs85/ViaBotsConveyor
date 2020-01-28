package GUI;

import FIPA.AgentID;
import FIPA.AgentIDHelper;
import FIPA.AgentIDsHelper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import viabots.Box;
import viabots.ManipulatorType;
import viabots.ViaBotAgent;
import viabots.messageData.ConvModelingMsgToUI;
import viabots.messageData.ConveyorOntologies;
import viabots.messageData.MessageToGUI;
import viabots.messageData.TopicNames;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class GUIAgent extends Agent {
    volatile ObservableList<AgentInfo> agents = FXCollections.observableArrayList();
    ConveyorGUI conveyorGUI;
    TopicManagementHelper topicHelper = null;
    AID uiTopic;
    AID uiCommandTopic; //from ui
    public AID conveyorTopic;
    public AID modelerToGuiTopic;
    public AID logTopic;

    MessageTemplate uiMsgTpl;
    MessageTemplate convMsgTpl;
    MessageTemplate modelerToGuiTpl;
    MessageTemplate logTopicTpl;

    @Override
    protected void setup() {
        super.setup();
        new JFXPanel(); // this will prepare JavaFX toolkit and environment
        Platform.runLater(() -> {
            conveyorGUI = new ConveyorGUI(this);

            conveyorGUI.start(ConveyorGUI.classStage);
            conveyorGUI.controller.workingAgentsListView.setItems(agents);

        });

        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            uiTopic = topicHelper.createTopic(TopicNames.GUI_TOPIC.name());
            uiCommandTopic = topicHelper.createTopic(TopicNames.GUI_TOPIC.name());
            uiMsgTpl = MessageTemplate.MatchTopic(uiTopic);
            topicHelper.register(uiTopic);
            conveyorTopic = topicHelper.createTopic(TopicNames.CONVEYOR_OUT_TOPIC.name());
            convMsgTpl = MessageTemplate.MatchTopic(conveyorTopic);
            topicHelper.register(conveyorTopic);

            modelerToGuiTopic = topicHelper.createTopic(TopicNames.MODELER_GUI.name());// register to receive msgs from convModelingBehaviour
            modelerToGuiTpl = MessageTemplate.MatchTopic(modelerToGuiTopic);
            topicHelper.register(modelerToGuiTopic);

            logTopic = topicHelper.createTopic(TopicNames.LOG_TOPIC.name());
            logTopicTpl = MessageTemplate.MatchTopic(logTopic);
            topicHelper.register(logTopic);

        } catch (
                ServiceException e) {
            e.printStackTrace();
        }
        addBehaviour(new GUIAgentBehaviour(this));

        //agents.add(new AgentInfo(ManipulatorType.UNKNOWN));
        AgentInfoListCell.guiAgent = this;
        AgentInfo.guiAgent = this;
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        Platform.runLater(() -> ConveyorGUI.classStage.close());

    }

    AgentInfo findAgentInfoByName(String name) {
        for (AgentInfo ai : agents) {
            if (ai.getName().compareTo(name) == 0)
                return ai;
        }
        return null;
    }

    void updateGUI(MessageToGUI msg, String agentName) {
        AgentInfo ai = findAgentInfoByName(agentName);
        if (ai != null) {
            // ai.isHardwareReady = msg.isHardwareReady;
            ai.currentRoles = msg.currentRoles;
            Platform.runLater(() -> {
                ai.setIsHardwareReadyProperty(msg.isHardwareReady);
                if (ai.currentRoles != null)
                    ai.setCurrentRolesString(ai.currentRoles.toString());
                if (msg.coneCount != null) {
                    ai.coneAvailableCountA.setValue(msg.coneCount[0]);
                    ai.coneAvailableCountB.setValue(msg.coneCount[1]);
                    ai.coneAvailableCountC.setValue(msg.coneCount[2]);
                }
                if (msg.enabledParts != null) {
                    ai.setEnabledCones(msg.enabledParts);
                }
            });
            if (msg.isTakenDown) {
                Platform.runLater(() -> {
                    agents.remove(ai);
                    conveyorGUI.controller.logTextArea.appendText("Msg from conveyor: " + ai.agentName + " takedown notification received\n");
                });
                System.out.println("takedown request received");
            }
        } else {
// else create new AgentInfo entry -td
            Platform.runLater(() -> agents.add(new AgentInfo(agentName, msg.manipulatorType)));// todo- add other info on record creation time
        }
    }

    void receiveConvMsg() {
        ACLMessage msg = receive(convMsgTpl);
        if (msg != null) {
            Platform.runLater(() -> conveyorGUI.controller.logTextArea.appendText("Msg from conveyor: " + msg.getContent() + "\n"));
            System.out.println("Msg from conveyor: " + msg.getContent());
        }
    }

    void receiveLogMsg() {// read all log messages
        ACLMessage msg = receive(logTopicTpl);
        while (msg != null) {
            String cont = msg.getContent();
            Platform.runLater(() -> conveyorGUI.controller.logTextArea.appendText("-----LOG---: " + cont + "\n"));
            // System.out.println("Msg from conveyor: " + msg.getContent());
            msg = receive(logTopicTpl);
        }
    }

    void receiveModelerMsg() {
        ACLMessage msg = receive(modelerToGuiTpl);
        if (msg != null) {
            List<LinkedList<Box>> boxQueues = null;
            try {
                boxQueues = ((ConvModelingMsgToUI) msg.getContentObject()).boxQueues;
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            //display conveyor model
            String data = getModelAsString(boxQueues);
            Platform.runLater(() -> conveyorGUI.controller.logTextArea.appendText("Msg from modeler: " + data + "\n"));
            //System.out.println("Msg from conveyor: " + msg.getContent());
        }
    }

    String getModelAsString(List<LinkedList<Box>> boxQueues) {
        StringBuilder str = new StringBuilder("-->");

        for (int i = 0; i < boxQueues.size(); i++) {
            LinkedList<Box> queue = boxQueues.get(i);

            ListIterator li = queue.listIterator(queue.size());

// Iterate in reverse.

            while (li.hasPrevious()) {
                Box b = (Box) li.previous();
                str.append(" [" + b.boxType.name() + b.id + "] ");
            }


            str.append(" || ");
        }
        return str.toString();
    }

    void receiveUImessage() {
        ACLMessage msg = receive(uiMsgTpl);

        while (msg != null) {
            if (msg != null) {

                //  System.out.println(" received ui broadcast");
                MessageToGUI data;
                try {
                    data = (MessageToGUI) msg.getContentObject();
                    if (data != null) {
                        updateGUI(data, msg.getSender().getName());
                        //   System.out.println("GUI received msg:- hardware:" + data.isHardwareReady);
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            } else {
                //  System.out.println(getName() + " received null msg- no msg");
            }

            //    conveyorGUI.controller.workingAgentsListView.refresh();
            msg = receive(uiMsgTpl);
        }
    }

    void sendUImessage(String agentName, String content) {

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setOntology(ConveyorOntologies.GuiCommands.name());
        if (content != null) {
            msg.setContent(content);
        }
        msg.addReceiver(new AID(agentName, true));
        send(msg);
        System.out.println(getName() + " command msg sent");
    }

    void sendUImessage(String agentName, MessageToGUI messageToGUI) {

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology(ConveyorOntologies.GuiCommands.name());

        if (messageToGUI != null) {
            try {
                msg.setContentObject(messageToGUI);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        msg.addReceiver(new AID(agentName, true));
        send(msg);
        System.out.println(getName() + "checkbox info msg sent");
    }

}