package viabots.behaviours;

import GUI.ConveyorGUI;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import viabots.BoxGenerationModel;
import viabots.BoxType;
import viabots.ConveyorAgent;
import viabots.ViaBotAgent;

public class ConveyorAgentBehaviour extends TickerBehaviour {
    ConveyorAgent master;
    BoxGenerationModel boxGenerationModel;

    public ConveyorAgentBehaviour(ConveyorAgent a) {
        super(a, ViaBotAgent.tickerPeriod);
        master = a;
        boxGenerationModel = new BoxGenerationModel();
    }

    @Override
    protected void onTick() {
        master.receiveUImessage();

        placeBoxOnBelt();// tries to put new box on the belt every tick


    }

    void placeBoxOnBelt() {

        if (master.previousHasLeft) {//warning - no synchronisation between update of this value and this readout

            BoxType type = boxGenerationModel.getNextFromPattern();
            master.placeBox();
            master.sendConveyorMessage(ConveyorAgent.boxArrived + " " + type.name());
        }
    }


}
