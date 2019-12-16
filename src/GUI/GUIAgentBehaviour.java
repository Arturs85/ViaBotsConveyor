package GUI;

import jade.core.behaviours.TickerBehaviour;

public class GUIAgentBehaviour extends TickerBehaviour {
    GUIAgent owner;

    GUIAgentBehaviour(GUIAgent guiAgent) {
        super(guiAgent, 1000);
        owner = guiAgent;
    }

    @Override
    protected void onTick() {
        owner.receiveUImessage();
        owner.receiveConvMsg();
        owner.receiveModelerMsg();
    }
}
