package viabots;

import jade.lang.acl.ACLMessage;
import viabots.behaviours.ConveyorAgentBehaviour;
import viabots.messageData.MessageContent;
import viabots.messageData.MessageToGUI;

import java.io.IOException;

public class ConveyorAgent extends ViaBotAgent {
    char commandStart = 'g';
    char commandStop = 's';
    char commandPlaceBox = 'p';
    boolean beltIsOn = false;
    TwoWaySerialComm serialComm;

    @Override
    protected void setup() {
        super.setup();
        type = ManipulatorType.CONVEYOR;
        serialComm = new TwoWaySerialComm();
        try {
            serialComm.connectToFirstPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        addBehaviour(new ConveyorAgentBehaviour(this));
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        System.out.println(getLocalName() + " was taken down----");
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
            if (content != null)
                System.out.println("request msg from gui received msg:" + content + "   " + getName());

            if (content.equalsIgnoreCase(MessageContent.TOGGLE_BELT.name())) {
                if (beltIsOn) stopBelt();
                else startBelt();
            } else if (content.equalsIgnoreCase(MessageContent.INSERT_PART.name())) {//common message type to all agents on the list, interpret as needed
                placeBox();
            }

        } //else
    }

}
