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
import viabots.ManipulatorType;
import viabots.messageData.MessageToGUI;
import viabots.messageData.TopicNames;

import java.io.IOException;

public class GUIAgent extends Agent {
    volatile ObservableList<AgentInfo> agents = FXCollections.observableArrayList();
    ConveyorGUI conveyorGUI;
    TopicManagementHelper topicHelper = null;
    AID uiTopic;
    AID uiCommandTopic; //from ui
    MessageTemplate uiMsgTpl;

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
        } catch (
                ServiceException e) {
            e.printStackTrace();
        }
        addBehaviour(new GUIAgentBehaviour(this));

        //agents.add(new AgentInfo(ManipulatorType.UNKNOWN));
        AgentInfoListCell.guiAgent = this;
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
            ai.isHardwareReady = msg.isHardwareReady;
            ai.currentRoles = msg.currentRoles;
            if (msg.isTakenDown) {
                Platform.runLater(() -> agents.remove(ai));
                System.out.println("takedown request received");
            }
        } else {
// else create new AgentInfo entry -td
            Platform.runLater(() -> agents.add(new AgentInfo(agentName, msg.manipulatorType)));// todo- add other info on record creation time
        }
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

            conveyorGUI.controller.workingAgentsListView.refresh();
            msg = receive(uiMsgTpl);
        }
    }

    void sendUImessage(String agentName, String content) {

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        if (content != null) {
            msg.setContent(content);
        }
        msg.addReceiver(new AID(agentName, true));
        send(msg);
        System.out.println(getName() + " command msg sent");
    }

    void sendUImessage(String agentName, MessageToGUI messageToGUI) {

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
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