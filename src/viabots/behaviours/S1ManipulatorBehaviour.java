package viabots.behaviours;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import viabots.ManipulatorAgent;
import viabots.messageData.MessageContent;
import viabots.messageData.MessageToGUI;

import java.util.EnumSet;

public class S1ManipulatorBehaviour extends TickerBehaviour {
    ManipulatorAgent master;
    EnumSet<ConeType> enabledParts;

    public S1ManipulatorBehaviour(ManipulatorAgent manipulatorAgent) {
        super(manipulatorAgent, 1000);

        master = manipulatorAgent;
        enabledParts = EnumSet.noneOf(ConeType.class);

    }


    void insertPart(ConeType coneType) {
        master.insertPart(coneType);
    }


    @Override
    protected void onTick() {
        receiveUImessage();
        receiveEnabledPartsMsg();
//        try {
//            master.communication.listenForReplyWTimeout();
//            System.out.println(getBehaviourName() + " insertion ok");
//
//        } catch (IOException e) {
//            //e.printStackTrace();
//            // System.out.println(getBehaviourName() + "did not receive  insertion ok msg within timeout");
//        }
    }

    public void receiveUImessage() {//for testing insertion
        ACLMessage msg = master.receive(master.requestTamplate);
        if (msg != null) {
            String cont = msg.getContent();
            if (cont != null) {
                if (msg.getContent().equals(MessageContent.INSERT_PART_A.name()))
                    master.insertPart(ConeType.A);
                else if (msg.getContent().equals(MessageContent.INSERT_PART_B.name()))
                    master.insertPart(ConeType.B);

                System.out.println("request msg from gui received msg:" + cont + ": " + master.getName());
            }
        } //else
    }

    void receiveEnabledPartsMsg() {
        ACLMessage msg = master.receive(master.informTamplate);
        if (msg != null) {
            MessageToGUI messageObj = null;
            try {
                messageObj = (MessageToGUI) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            if (messageObj != null) {
                if (messageObj.enabledParts != null)
                    enabledParts = messageObj.enabledParts;
                System.out.println(enabledParts.toString());
            }
        }
    }
}
