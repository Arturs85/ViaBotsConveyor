package viabots;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import viabots.behaviours.GuiInteractionBehaviour;
import viabots.behaviours.PartType;
import viabots.behaviours.S1ManipulatorBehaviour;
import viabots.messageData.MessageToGUI;

;

public class ManipulatorAgent extends ViaBotAgent {
    public CommunicationWithHardware communication = new CommunicationWithHardware();


    @Override
    protected void setup() {
        super.setup();
        type = ManipulatorType.UNKNOWN;//for testing
        communication.start();// tries to connect to server in new thread
        // addBehaviour(new TestCommunicationBehaviour(this));
        addBehaviour(new S1ManipulatorBehaviour(this));

    }

    @Override
    protected void takeDown() {
        super.takeDown();
        if (communication != null)
            communication.isRunning = false;
        System.out.println(getName() + " takeDown executed");
    }

    public void insertPart(PartType partType) {
        switch (partType) {
            case A:
                communication.sendString(InterProcessCommands.insertPartA);
                break;
            case B:
                communication.sendString(InterProcessCommands.insertPartB);
                break;
            case C:
                communication.sendString(InterProcessCommands.insertPartC);
                break;
            case D:
                communication.sendString(InterProcessCommands.insertPartD);
                break;

        }
    }

    @Override
    public boolean isConnected() {
        return communication.isConnected();
    }

    public void receiveUImessage() {
        ACLMessage msg = receive(requestTamplate);
        if (msg != null) {

            System.out.println("request msg from gui received msg:-button move : " + getName());

        } //else
    }

}
