package viabots.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import viabots.ManipulatorAgent;

import java.io.IOException;

public class TestCommunicationBehaviour extends OneShotBehaviour {
ManipulatorAgent master = (ManipulatorAgent) getAgent();

    @Override
    public void action() {
        if(master.communication!= null){
            master.communication.sendBytes(new byte[]{66,67,69});
            String reply=null;
            try {
              reply=  master.communication.listenForReplyWTimeout();
            } catch (IOException e) {

                e.printStackTrace();
            return;
            }
            if(reply!= null)
                System.out.println("Received reply within timeout: "+reply);
        }
    }
}
