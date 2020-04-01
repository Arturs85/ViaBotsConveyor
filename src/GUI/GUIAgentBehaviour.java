package GUI;

import jade.core.behaviours.TickerBehaviour;
import viabots.ViaBotAgent;

public class GUIAgentBehaviour extends TickerBehaviour {
    GUIAgent owner;
    static long timeLast = 0;
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

        if (owner.hasOpertionStarted) {
            long time = System.currentTimeMillis();
            owner.onTimeMiliSec += (time - timeLast);
            timeLast = time;
        }
        owner.displayStatistics();

    }
}
