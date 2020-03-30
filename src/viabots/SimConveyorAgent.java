package viabots;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SimConveyorAgent extends BaseConveyorAgent {
    SimConveyor simConveyor;

    @Override
    protected void setup() {
        super.setup();
        simConveyor = new SimConveyor(this);
        new Thread(simConveyor).start();

    }

    @Override
    public void requestStopBeltAt(int position) {
        simConveyor.requestStopBeltAt(position);
    }

    @Override
    public void startBelt() {
        simConveyor.moveOn();
        beltIsOn = true;// irrelevant ?
    }

    @Override
    public void placeBox() {

        simConveyor.placeBox();
        previousHasLeft = false;

    }

    @Override
    protected void takeDown() {
        super.takeDown();
        simConveyor.isRunning = false;
    }

    @Override
    public void setSensorIntervals(int[] intervals) {
        simConveyor.setSensorPositions(intervals);
    }
}
