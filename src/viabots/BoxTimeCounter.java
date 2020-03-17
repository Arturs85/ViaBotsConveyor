package viabots;

import viabots.behaviours.ConveyorModelingBehaviour;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BoxTimeCounter {
    List<LinkedList<Box>> boxQueues;
    ArrayList<Box> currentBoxes; // this box should be valid from moment when message "boxStoppedAt" is sent till "moveOn is received"
    ArrayList<Box> processedBoxes = new ArrayList<>(50);

    public BoxTimeCounter(List<LinkedList<Box>> boxQueues, ArrayList<Box> currentBoxes) {
        this.boxQueues = boxQueues;
        this.currentBoxes = currentBoxes;
    }

    long movementStartTime = 0;
    long movementEndTime = 0;

    public void stoppedAtSensor() {
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
        long stoppedTime =  movementStartTime- movementEndTime ;
//add stopped time to both types of boxes- ones that was at the sensor and ones, that was between
        for (Box b : currentBoxes) {
            b.timeAtStationMs += stoppedTime;
        }
//tell witch boxes are not at sensors

        ArrayList<Box> all = toArraylist();
        all.removeAll(currentBoxes);
        for (Box b :
                all) {
            b.timeBetweenStationsMs += stoppedTime;
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
        for (int i = 0; i < processedBoxes.size(); i++) {
            Box b = processedBoxes.get(i);
            System.out.println(i + ". " + b.id + " " + b.boxType + " " + b.timeMovingMs + " " + b.timeBetweenStationsMs + " " + b.timeAtStationMs);
        }
    }
}
