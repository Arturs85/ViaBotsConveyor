package viabots;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import viabots.behaviours.ConeType;
import viabots.behaviours.RoleCheckingBehaviour;
import viabots.behaviours.S1ManipulatorBehaviour;

;import java.util.EnumSet;

public class ManipulatorAgent extends ViaBotAgent {
    public CommunicationWithHardware communication = new CommunicationWithHardware();
    public EnumSet<VSMRoles> currentRoles;


    @Override
    protected void setup() {
        super.setup();
        // type = ManipulatorType.UNKNOWN;//for testing
        communication.start();// tries to connect to server in new thread
        // addBehaviour(new TestCommunicationBehaviour(this));
        addBehaviour(new S1ManipulatorBehaviour(this));
        addBehaviour(new RoleCheckingBehaviour(this));
        currentRoles = EnumSet.noneOf(VSMRoles.class);

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
    }

    @Override
    public boolean isConnected() {
        return communication.isConnected();
    }

    @Override
    public void receiveUImessage() {
        ACLMessage msg = receive(requestTamplate);
        if (msg != null) {

            System.out.println("request msg from gui received msg:-button move : " + getName());

        } //else
    }

}
