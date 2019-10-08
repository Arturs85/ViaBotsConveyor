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



    void insertPart(PartType partType) {
        master.insertPart(partType);
    }


    @Override
    protected void onTick() {
        master.receiveUImessage();
        insertPart(PartType.A);
        try {
            master.communication.listenForReplyWTimeout();
            System.out.println(getBehaviourName() + "insertion ok");

        } catch (IOException e) {
            //e.printStackTrace();
            // System.out.println(getBehaviourName() + "did not receive  insertion ok msg within timeout");
        }
    }
}
