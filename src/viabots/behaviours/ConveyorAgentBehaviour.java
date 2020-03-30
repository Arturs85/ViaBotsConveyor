package viabots.behaviours;

import GUI.ConveyorGUI;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import viabots.*;
import viabots.messageData.BoxMessage;
import viabots.messageData.BoxParamsMsg;
import viabots.messageData.MessageContent;
import viabots.messageData.TopicNames;

import java.io.IOException;

public class ConveyorAgentBehaviour extends BaseTopicBasedTickerBehaviour {
    BaseConveyorAgent master;
    BoxGenerationModel boxGenerationModel;
    static final int boxGenModelSendingInterval = 5000; //ms
    int boxGenSendingIntCounter = 0;
    public ConveyorAgentBehaviour(BaseConveyorAgent a) {
        super(a);
        master = a;
        boxGenerationModel = new BoxGenerationModel();
        createAndRegisterReceivingTopics(TopicNames.CONVEYOR_IN_TOPIC);
        createSendingTopic(TopicNames.CONVEYOR_OUT_TOPIC);
        createSendingTopic(TopicNames.BOX_GEN_MODEL_TOPIC);
createAndRegisterReceivingTopics(TopicNames.PARAMETERS_TOPIC);
    }

    @Override
    protected void onTick() {
        super.onTick();
receiveParamsMsg();
        receiveIncomingTopicMsgs();
        master.receiveUImessage();// this should be last call to message reception, for it is receiving msgs wo template

        placeBoxOnBelt();// tries to put new box on the belt every tick

        if (boxGenSendingIntCounter >= boxGenModelSendingInterval) {// clear counter and send model
            boxGenSendingIntCounter = 0;
            sendBoxGeneratorModelMessage();
        }
        boxGenSendingIntCounter += ViaBotAgent.tickerPeriod;



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
                master.requestStopBeltAt(boxMessage.positionInBox);// this request should only be sent jus before box will arrive


                Log.soutWTime("request to stop at station received  " + boxMessage.positionInBox);

            } else if (msg.getPerformative() == ACLMessage.INFORM) {// this should be moveOn message
                 Log.soutWTime(getBehaviourName() + " received inform to move on belt from "+ msg.getSender().getLocalName());
                master.startBelt();
            }

        }
    }
void receiveParamsMsg(){
    ACLMessage msg = master.receive(templates[TopicNames.PARAMETERS_TOPIC.ordinal()]);
if(msg!= null){
    BoxParamsMsg msgObj = null;
    try {
        msgObj= (BoxParamsMsg) msg.getContentObject();
    } catch (UnreadableException e) {
        e.printStackTrace();
    }
boxGenerationModel.setPattern(msgObj.pattern);
    System.out.println("Conveyor agent updated box pattern: "+boxGenerationModel.getPatternAsString());
    owner.sendLogMsgToGui("Conveyor agent updated box pattern: "+boxGenerationModel.getPatternAsString());
    Box.setBoxContents(msgObj.boxContents);
    if(msgObj.sensorPositions!=null){
        master.setSensorIntervals(msgObj.sensorPositions);
    }
}
}
    public void sendConveyorMessage(String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.setContent(content);
        msg.addReceiver(sendingTopics[TopicNames.CONVEYOR_OUT_TOPIC.ordinal()]);
        master.send(msg);
         Log.soutWTime("Conv sent a message with content : " + content);
    }

    public void sendBoxGeneratorModelMessage() {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        try {
            msg.setContentObject(boxGenerationModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.BOX_GEN_MODEL_TOPIC.ordinal()]);
        master.send(msg);
        // Log.soutWTime("Conv sent a message with content : " + content);
    }

}
