package viabots.behaviours;

import GUI.ConveyorGUI;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import viabots.ConveyorAgent;
import viabots.ViaBotAgent;

public class ConveyorAgentBehaviour extends TickerBehaviour {
    ConveyorAgent master;

    public ConveyorAgentBehaviour(ConveyorAgent a) {
        super(a, ViaBotAgent.tickerPeriod);
        master = a;
    }

    @Override
    protected void onTick() {
        master.receiveUImessage();

    }
}
