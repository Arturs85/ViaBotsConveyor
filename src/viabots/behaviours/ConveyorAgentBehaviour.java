package viabots.behaviours;

import GUI.ConveyorGUI;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import viabots.BoxGenerationModel;
import viabots.BoxType;
import viabots.ConveyorAgent;
import viabots.ViaBotAgent;
import viabots.messageData.BoxMessage;
import viabots.messageData.MessageContent;
import viabots.messageData.TopicNames;

public class ConveyorAgentBehaviour extends BaseTopicBasedTickerBehaviour {
    ConveyorAgent master;
    BoxGenerationModel boxGenerationModel;

    public ConveyorAgentBehaviour(ConveyorAgent a) {
        super(a);
        master = a;
        boxGenerationModel = new BoxGenerationModel();
        createAndRegisterReceivingTopics(TopicNames.CONVEYOR_IN_TOPIC);
        createSendingTopic(TopicNames.CONVEYOR_OUT_TOPIC);
    }

    @Override
    protected void onTick() {
        receiveIncomingTopicMsgs();
        master.receiveUImessage();// this should be last call to message reception, for it is receiving msgs wo template

        placeBoxOnBelt();// tries to put new box on the belt every tick


    }

    public void placeBoxOnBelt() {

        if (master.previousHasLeft) {//warning - no synchronisation between update of this value and this readout

            BoxType type = boxGenerationModel.getNextFromPattern();
            master.placeBox();
            sendConveyorMessage(ConveyorAgent.boxArrived + " " + type.name());
        }
    }

    void receiveIncomingTopicMsgs() {
        ACLMessage msg = master.receive(templates[TopicNames.CONVEYOR_IN_TOPIC.ordinal()]);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.REQUEST) {// this should be stopBeltOnSensor x message
                BoxMessage boxMessage = null;
                try {
                    boxMessage = (BoxMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                master.requestStopBeltAt(boxMessage.positionInBox);


                System.out.println("box stopped at station received  " + master.getName());

            } else if (msg.getPerformative() == ACLMessage.INFORM) {// this should be moveOn message
                System.out.println(getBehaviourName() + " received inform to move on belt");
                master.startBelt();
            }

        }
    }

    public void sendConveyorMessage(String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.setContent(content);
        msg.addReceiver(sendingTopics[TopicNames.CONVEYOR_OUT_TOPIC.ordinal()]);
        master.send(msg);
        System.out.println("Conv sent a message with content : " + content);
    }


}
