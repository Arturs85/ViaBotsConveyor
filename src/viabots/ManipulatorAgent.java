package viabots;

import viabots.behaviours.PartType;
import viabots.behaviours.S1ManipulatorBehaviour;
import viabots.behaviours.TestCommunicationBehaviour;

;

public class ManipulatorAgent extends ViaBotAgent {
    public ManipulatorType type = ManipulatorType.UNKNOWN;//for testing
    public CommunicationWithHardware communication = new CommunicationWithHardware();


    @Override
    protected void setup() {
        super.setup();
        communication.start();// tries to connect to server in new thread
        // addBehaviour(new TestCommunicationBehaviour(this));
        addBehaviour(new S1ManipulatorBehaviour(this));
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


}
