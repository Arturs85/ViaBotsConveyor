package viabots;

import com.sun.javafx.embed.swing.JFXPanelInterop;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.*;


public class SimConveyor implements Runnable {
    static int sensorCount;
int beltSpeed = 130; // mm/sec
int stepPeriodMs =100;
int beltMmPerStep = (130*stepPeriodMs)/1000;
 volatile    LinkedList<Integer> boxPositions = new LinkedList<Integer>();

    public void setSensorPositions(int[] sensorPositions) {
        this.sensorPositions = sensorPositions;
    }

    //int sensorPositions[] = new int[]{500,1100,1800,2600,3500};   //mm
int sensorPositions[] = new int[]{500,1500,2100,2800,3600};   //mm
    int stopRequests[] = new int[]{9999,0,0,0,0};
    BaseConveyorAgent master;// for sending messages  only
    ArrayList<Integer> currentSensors = new ArrayList<>(5);
boolean stateIsStopped = true;
boolean isRunning = true;
int moveSteps =0;
Canvas canvas;

    public SimConveyor(BaseConveyorAgent master) {
        this.master = master;
        canvas= createWindow();
        System.out.println("  mm per step: "+ beltMmPerStep);
    }


void simStep(){

    if(stateIsStopped){

    }else{
        movementStep();
    }
}


void movementStep(){// call only if stateismoving
    moveSteps++;
    currentSensors.clear();

    // move boxes
   ListIterator<Integer> it = boxPositions.listIterator();
synchronized (this){
    while (it.hasNext()) {
        Integer boxPos = it.next();
        boxPos+=beltMmPerStep;
        it.set(boxPos);
    // check if box is at sensor
        for (int i = 0; i < sensorPositions.length; i++) {
            if(boxPos>sensorPositions[i]-beltMmPerStep && boxPos<=sensorPositions[i]){// box has hit the sensor
                currentSensors.add(i);// mark trigerred sensors for drawing

                synchronized (this){
                if(stopRequests[i]>0) {//there was a stop request at this sensor, stop the belt
                    stateIsStopped = true;


                    master.onSerialInput(getCharForPosition(i + 1));
                    stopRequests[i] --;
            }else{//no stop requests, send trigger at
                    master.onSerialInput(Integer.toString(i + 1).charAt(0));

                }
              }
            }
        }
    }
}
    //remove last box from conveyor if it has passed all sensors
    if(!boxPositions.isEmpty()){
    if(boxPositions.peekFirst()> sensorPositions[sensorPositions.length-1]) boxPositions.removeFirst();
}
    }

void moveOn(){
    synchronized (this){
        stateIsStopped = false;
    }
}

void placeBox(){

        boxPositions.addLast(0);
    System.out.println("SimConv added new box , total "+boxPositions.size());
    }

char getCharForPosition(int pos){
    switch (pos){
        case 1: return 'A';
        case 2: return 'B';
        case 3: return 'C';
        case 4: return 'D';
        case 5: return 'E';
        case 6: return 'F';

    }
    return 'R';//not supossed to reach this return, means error
}
void requestStopBeltAt(int positions){
   synchronized (this){
    stopRequests[positions]++;
}
}
    static final int width = 500;
    static final int height = 200;

    Canvas createWindow(){
        Canvas canvas = new Canvas(width,height);
new JFXPanel();
        Platform.runLater(new Runnable() {
    @Override
    public void run() {
        Stage stage = new Stage();
        stage.setX(width);
        stage.setX(height);
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        stage.setTitle("Conveyor Sim");
        stage.setScene(new Scene(root));
        stage.show();

    }
});


        return canvas;

    }
   int pad =10;
    int convWidth = 400;
    int sensRadi=4;
    int boxW=15;
    void draw(){
     GraphicsContext gc = canvas.getGraphicsContext2D();
       gc.clearRect(0,0,width,height);
        gc.strokeRect(pad,height-pad-pad, convWidth, pad);
        for (int i = 0; i < sensorPositions.length; i++) {
            int x = sensorPositions[i]/10;
            gc.strokeOval(x+pad-sensRadi,height-pad-pad-sensRadi-sensRadi-1,2*sensRadi,2*sensRadi);

        }
       synchronized (this){// concurrent modification fix ?
        for (Integer i:boxPositions) {

            gc.strokeRoundRect((i/10)+pad-boxW,height-3*pad,boxW,pad-1,2,2);
        }
        }
        for (Integer i :currentSensors) {
            int x = sensorPositions[i]/10;
            gc.fillOval(x+pad-sensRadi,height-pad-pad-sensRadi-sensRadi-1,2*sensRadi,2*sensRadi);

        }
       // gc.strokeText(" time : "+System.currentTimeMillis(),10,10);
        gc.strokeText(" moving : "+ !stateIsStopped,10,30);
        if(!boxPositions.isEmpty())
        gc.strokeText(" last box pos : "+ boxPositions.peekLast(),10,50);
        gc.strokeText(" movement steps : "+ moveSteps,10,70);
    }

    @Override
    public void run() {
        while(isRunning){
simStep();
       if(canvas!=null) {
           Platform.runLater(new Runnable() {
               @Override
               public void run() {
                   draw();
               }
           });
       }
        try {
            Thread.sleep(stepPeriodMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    }
}
