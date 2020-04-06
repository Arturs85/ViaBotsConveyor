package viabots;

import viabots.behaviours.ConeType;

public class SimManipulatorAgent extends ManipulatorAgent {
SimManipulator simManipulator;
    final static int ABB_INSERT_TIME = 4000;
    final static int SMALL_INSERT_TIME = 5000;
    final static int BAXTER_INSERT_TIME = 6000;
    final static int ABB_PICKUP_TIME = 7000;
    final static int SMALL_PICKUP_TIME = 8000;
    final static int BAXTER_PICKUP_TIME = 9000;

    ManipulatorType manipType;

    @Override
    protected void setup() {
        super.setup();
        if(getArguments()[1]!=null)
            manipType = (ManipulatorType) getArguments()[1];
        simManipulator = new SimManipulator(hardwareMsgQueue,manipType);

    super.communication.isRunning = false;
    }

    @Override
    public void insertPartInPosition(int position) {
        simManipulator.insertConeInPosition(position);
    }

    @Override
    public void pickUpCone(ConeType coneType) {
        simManipulator.pickupCone();
    }



}
