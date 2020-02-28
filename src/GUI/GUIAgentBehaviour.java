package GUI;

import jade.core.behaviours.TickerBehaviour;
import viabots.ViaBotAgent;

public class GUIAgentBehaviour extends TickerBehaviour {
    GUIAgent owner;

    GUIAgentBehaviour(GUIAgent guiAgent) {
        super(guiAgent, ViaBotAgent.tickerPeriod);
        owner = guiAgent;
    }

    @Override
    protected void onTick() {
        owner.receiveConvMsg();
        owner.receiveModelerMsg();
        owner.receiveLogMsg();
        owner.receiveUImessage();

        if (owner.hasOpertionStarted)
            owner.onTimeMiliSec += ViaBotAgent.tickerPeriod;

        owner.displayStatistics();

    }
}
