package viabots;

import viabots.behaviours.ConveyorModelingBehaviour;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BoxTimeCounter {
    List<LinkedList<Box>> boxQueues;
   public ArrayList<Box> currentBoxes; // this box should be valid from moment when message "boxStoppedAt" is sent till "moveOn is received"
 public    ArrayList<Box> processedBoxes = new ArrayList<>(50);

    public BoxTimeCounter(List<LinkedList<Box>> boxQueues, ArrayList<Box> currentBoxes) {
        this.boxQueues = boxQueues;
        this.currentBoxes = currentBoxes;
    }

    long movementStartTime = 0;
    long movementEndTime = 0;
    boolean stoppedAtZero = false;

    public void stoppedAtSensor(boolean isSensorZero) {
        stoppedAtZero = isSensorZero;
        // calc movement time and add it to all boxes
        movementEndTime = System.currentTimeMillis();
        long movementTime = movementEndTime - movementStartTime;
        //add movement time to all boxes that are on the belt
        for (LinkedList<Box> boxes : boxQueues) {
            for (Box b : boxes) {
                b.timeMovingMs += movementTime;
            }
        }
    }

    public void startedMovingOn() {
        movementStartTime = System.currentTimeMillis();
        if (movementEndTime == 0) return;
        long stoppedTime = SimManipulator.timeScale * (movementStartTime - movementEndTime);
//add stopped time to both types of boxes- ones that was at the sensor and ones, that was between
        //System.out.println("btc current boxes size: "+currentBoxes.size());
        if (!stoppedAtZero) {
            for (Box b : currentBoxes) {
                b.timeAtStationMs += stoppedTime;
                System.out.println("        ^^^^^^^       mts " + stoppedTime);
            }
        } else {
            for (Box b : currentBoxes) {
                b.timeAtSensor0Ms += stoppedTime;
                //System.out.println("        ^^^^^^^       mts " + stoppedTime);
            }
        }
//tell witch boxes are not at sensors
        ArrayList<Box> all = toArraylist();
        all.removeAll(currentBoxes);
        if (!stoppedAtZero) {
            for (Box b :
                    all) {
                b.timeBetweenStationsMs += stoppedTime;
            }
        } else {
            for (Box b :
                    all) {
                b.timeWaitingOnManagementMs += stoppedTime;
            }
        }

    }

    ArrayList<Box> toArraylist() {
        ArrayList<Box> all = new ArrayList<>(5);
        for (LinkedList<Box> boxes : boxQueues) {
            for (Box b : boxes) {
                all.add(b);
            }
        }
        return all;
    }

    public void addToFinished(Box box) {
        processedBoxes.add(box);
        if (processedBoxes.size() % 5 == 0) print();// print results after each 10th box

    }

    public void print() {
        System.out.println("nr id boxType timeMovingMs timeBetweenStationsMs timeAtStationMs timeAtSensor0Ms timeWaitingOnManagementMs");

        for (int i = 0; i < processedBoxes.size(); i++) {
            Box b = processedBoxes.get(i);
            System.out.println(i + ". " + b.id + " " + b.boxType + " " + b.timeMovingMs + " " + b.timeBetweenStationsMs + " " + b.timeAtStationMs + " " + b.timeAtSensor0Ms + " " + b.timeWaitingOnManagementMs);
        }
    }
public String processedBoxesToString(){
        StringBuilder sb = new StringBuilder();
    sb.append("nr id boxType timeMovingMs timeBetweenStationsMs timeAtStationMs timeAtSensor0Ms timeWaitingOnManagementMs");
sb.append(System.lineSeparator());

    for (int i = 0; i < processedBoxes.size(); i++) {
        Box b = processedBoxes.get(i);
        sb.append(i + ". " + b.id + " " + b.boxType + " " + b.timeMovingMs + " " + b.timeBetweenStationsMs + " " + b.timeAtStationMs + " " + b.timeAtSensor0Ms + " " + b.timeWaitingOnManagementMs);
    sb.append(System.lineSeparator());
    }
    return sb.toString();
}
}
