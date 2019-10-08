package viabots;

import jade.lang.acl.ACLMessage;
import viabots.messageData.MessageToGUI;

import java.io.IOException;

public class ConveyorAgent extends ViaBotAgent {
    char commandStart = 'g';
    char commandStop = 's';
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

    }

    void stopBelt() {
        try {
            serialComm.out.write(commandStop);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void startBelt() {
        try {
            serialComm.out.write(commandStart);
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


}
