package viabots.behaviours;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import viabots.ManipulatorAgent;
import viabots.messageData.MessageToGUI;

import java.io.IOException;

public class S1ManipulatorBehaviour extends TickerBehaviour {
    ManipulatorAgent master;

    public S1ManipulatorBehaviour(ManipulatorAgent manipulatorAgent) {
        super(manipulatorAgent, 1000);

        master = manipulatorAgent;

    }

    void sendMessageToGui() {
        MessageToGUI data = new MessageToGUI(master.communication.isConnected(), master.type);
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(master.uiTopic);

        master.send(msg);

    }

    void insertPart(PartType partType) {
        master.insertPart(partType);
    }


    @Override
    protected void onTick() {
        sendMessageToGui();
        insertPart(PartType.A);
        try {
            master.communication.listenForReplyWTimeout();
            System.out.println(getBehaviourName() + "insertion ok");

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println(getBehaviourName() + "did not receive  insertion ok msg within timeout");
        }
    }
}