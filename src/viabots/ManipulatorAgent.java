package viabots;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import viabots.behaviours.*;

;import java.util.EnumSet;

public class ManipulatorAgent extends ViaBotAgent {
    public CommunicationWithHardware communication = new CommunicationWithHardware();
    int sensorPosition;
    public int[] coneCountAvailable = new int[ConeType.values().length];

    @Override
    protected void setup() {
        super.setup();
        // type = ManipulatorType.UNKNOWN;//for testing
        communication.start();// tries to connect to server in new thread
        // addBehaviour(new TestCommunicationBehaviour(this));
        if (getArguments().length < 3)
            System.out.println("!!!---missing sensor position argument ---!!!");
        else if (getArguments()[2] != null)
            sensorPosition = (Integer) getArguments()[2];

        addBehaviour(new S1ManipulatorBehaviour(this, sensorPosition));
        addBehaviour(new RoleCheckingBehaviour(this));
        currentRoles = EnumSet.noneOf(VSMRoles.class);
//addBehaviour(new ConveyorModelingBehaviour(this,2000));//for testing----------------------------------!!!
    }

    @Override
    protected void takeDown() {
        super.takeDown();
// Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        if (communication != null)
            communication.isRunning = false;
        System.out.println(getName() + " takeDown executed");
    }

    public void insertPart(ConeType coneType) {
        switch (coneType) {
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
        int index = coneType.ordinal();
        coneCountAvailable[index] = coneCountDecrese(coneCountAvailable[index]);
      //  GuiInteractionBehaviour.sendConeCountChanged(this, coneCountAvailable);
    }

    public void insertPartInPosition(int position) {//positions in box(As shown in Box) is mapped to string commands to manipulator process
        switch (position) {
            case 0:
                communication.sendString(InterProcessCommands.insertPartA);
                break;
            case 1:
                communication.sendString(InterProcessCommands.insertPartB);
                break;
            case 2:
                communication.sendString(InterProcessCommands.insertPartC);
                break;
            case 3:
                communication.sendString(InterProcessCommands.insertPartD);
                break;
            case 4:
                communication.sendString(InterProcessCommands.insertPartE);
                break;
            case 6:
                communication.sendString(InterProcessCommands.insertPartF);
                break;

        }
        //decrese available cone count of inserted type
        int index = Box.getConeTypeForBoxPosition(position).ordinal();
        coneCountAvailable[index] = coneCountDecrese(coneCountAvailable[index]);
     //   GuiInteractionBehaviour.sendConeCountChanged(this, coneCountAvailable);
    }

    static int coneCountDecrese(int count) {
        count--;
        if (count < 0) count = 0;
        return count;
    }
    @Override
    public boolean isConnected() {
        return communication.isConnected();
    }

    @Override
    public void receiveUImessage() {
//        ACLMessage msg = receive(requestTamplate);
//        if (msg != null) {
//
//            System.out.println("request msg from gui received msg:-button move : " + getName());
//
//        } //else
    }

}
