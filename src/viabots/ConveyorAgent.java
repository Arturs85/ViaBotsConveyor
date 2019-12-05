package viabots;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import viabots.behaviours.ConveyorAgentBehaviour;
import viabots.messageData.MessageContent;
import viabots.messageData.MessageToGUI;
import viabots.messageData.TopicNames;

import java.io.IOException;

public class ConveyorAgent extends ViaBotAgent {
    char commandStart = 'g';
    char commandStop = 's';
    char commandPlaceBox = 'p';
    char commansStopAt1 = '1';
    char commansStopAt2 = '2';
    char commansStopAt3 = '3';
    char commansStopAt4 = '4';
    char commansStopAt5 = '5';
    char commansStopAt6 = '6';

    public AID conveyorTopic;
    boolean beltIsOn = false;
    TwoWaySerialComm serialComm;
    static String triggerAt = "triggerAt";

    @Override
    protected void setup() {
        super.setup();
        type = ManipulatorType.CONVEYOR;
        serialComm = new TwoWaySerialComm(this);
        try {
            serialComm.connectToFirstPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        conveyorTopic = topicHelper.createTopic(TopicNames.CONVEYOR_TOPIC.name());

        addBehaviour(new ConveyorAgentBehaviour(this));
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        System.out.println(getLocalName() + " was taken down----");
        serialComm.commPort.close();
    }

    void requestStopBeltAt(int position) {
        try {
            serialComm.out.write((char) position);
            //  beltIsOn = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stopBelt() {
        try {
            serialComm.out.write(commandStop);
            beltIsOn = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void startBelt() {
        try {
            serialComm.out.write(commandStart);
            beltIsOn = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void placeBox() {
        try {
            serialComm.out.write(commandPlaceBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return serialComm.isConnected;
    }

    void onSerialInput(char data) {
        switch (data) {
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
                sendConveyorTriggerAtMessage(data);
                break;
            default:
                break;
        }

    }

    void sendConveyorTriggerAtMessage(char sensorPosition) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(triggerAt + sensorPosition);
        msg.addReceiver(conveyorTopic);
        send(msg);
        System.out.println("sent triggerAt " + sensorPosition);
    }

    void sendMessageToGui() {
        MessageToGUI data = new MessageToGUI(serialComm.isConnected, type);//impl in GuiInteractions behaviour
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(uiTopic);

        send(msg);
        // System.out.println(getBehaviourName()+" sent msg to gui-----------");
    }

    @Override
    public void receiveUImessage() {
        ACLMessage msg = receive(requestTamplate);
        if (msg != null) {
            String content = msg.getContent();
            if (content != null) {
                System.out.println("request msg from gui received msg:" + content + "   " + getName());

                if (content.equalsIgnoreCase(MessageContent.TOGGLE_BELT.name())) {
                    if (beltIsOn) stopBelt();
                    else startBelt();
                } else if (content.equalsIgnoreCase(MessageContent.PLACE_BOX.name())) {//
                    placeBox();
                }
            }
        } //else
    }

}
