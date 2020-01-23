package viabots.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import viabots.CommunicationWithHardware;
import viabots.ManipulatorAgent;

import java.io.IOException;

public class TestCommunicationBehaviour extends TickerBehaviour {
    ManipulatorAgent master;

    public TestCommunicationBehaviour(ManipulatorAgent manipulatorAgent) {
        super(manipulatorAgent, 1000);
        master = manipulatorAgent;
    }
    @Override
    protected void onTick() {


        if (master.communication != null) {
            if (!master.communication.isConnected()) {
                return;

            }
            master.communication.sendBytes(new byte[]{66,67,69});
            String reply=null;
            try {
                reply = master.communication.listenForReplyWTimeout(CommunicationWithHardware.SO_READ_TIMEOUT_MS);
            } catch (IOException e) {
                System.out.println("No reply---");
                //   e.printStackTrace();
            return;
            }
            if(reply!= null)
                System.out.println("Received reply within timeout: "+reply);
        }
    }


}
