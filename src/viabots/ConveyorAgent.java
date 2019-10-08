package viabots;

import jade.lang.acl.ACLMessage;
import viabots.messageData.MessageToGUI;

import java.io.IOException;

public class ConveyorAgent extends ViaBotAgent {
    char commandStart = 'g';
    char commandStop = 's';
    ManipulatorType type;
    TwoWaySerialComm serialComm;

    @Override
    protected void setup() {
        super.setup();
        type = ManipulatorType.CONVEYOR;
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

    void sendMessageToGui() {
        MessageToGUI data = new MessageToGUI(serialComm.isConnected, type);
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
