package viabots;

import viabots.behaviours.TestCommunicationBehaviour;

enum ManipulatorType {BAXTER,IRB160,PEPPER,SMALL_ONE};

public class ManipulatorAgent extends ViaBotAgent {
    ManipulatorType type;
public CommunicationWithHardware communication=new CommunicationWithHardware();


    @Override
    protected void setup() {
        super.setup();
communication.start();// tries to connect to server in new thread
        addBehaviour(new TestCommunicationBehaviour());

    }


}
