package viabots;

import viabots.behaviours.ConeType;
import viabots.behaviours.ConveyorModelingBehaviour;

import javax.swing.*;

import java.io.Serializable;
import java.util.Arrays;

import static viabots.BoxType.*;

public class BoxGenerationModel implements Serializable {



    public   BoxType[] pattern = new BoxType[]{B, A, B, C,B,A,B,B,C,C,C,C};//{B, A, A, C,A,B,B,C,C,C};
    int counter = -1;
    final static int coneTypeValuesLength = ConeType.values().length;

    public void setPattern(BoxType[] pattern) {
        this.pattern = pattern;
        counter =-1;
    }
    public BoxType getNextFromPattern() {
        counter++;
        return pattern[counter % pattern.length];


    }

    public double[] countAvarageCones(int lengthOfPrediction) {
        if (lengthOfPrediction < 1) return null;
        if (counter < 0) return null;

        double[] predictedConeCounts = new double[coneTypeValuesLength];
        for (int i = 0; i < lengthOfPrediction; i++) {
            BoxType boxType = getBoxTypeafAfter(i);
            int[] cones = Box.getEachConeTypeCountForBox(boxType);

            for (int j = 0; j < coneTypeValuesLength; j++) {//count the number of each cone type in this box and add it to the array of summs
                predictedConeCounts[j] += cones[j];
            }

        }
        for (int i = 0; i < predictedConeCounts.length; i++) {
            predictedConeCounts[i] /= lengthOfPrediction;
        }

        return predictedConeCounts;
    }

    static final double avgPickupTime = 10;
    static final double avgInsertTime = 5;
    static final double avgToolChangeTime = 10;

//    public double[][] findOptCVals(int length){
//        int leastTimeSoFar=Integer.MAX_VALUE;
//        int[] aCountSoFar= new int[length];
//        for (int i = 0; i < length; i++) {
//            BoxType bt = getBoxTypeafAfter(i);
//
//            for (int aMan = 0; aMan <= ConveyorModelingBehaviour.numberOfSensors-1; aMan++) {//manip count = 4
//                int bMan =  ConveyorModelingBehaviour.numberOfSensors-1-aMan;
//          //  double timeForBox =
//            }
//        }
//
//    }

   public String getPatternAsString(){
       StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < pattern.length; i++) {
           sb.append(pattern[i].name());
           sb.append(' ');
       }
        return sb.toString();
   }

    public BoxType getBoxTypeafAfter(int iterations) {

        int index = counter + iterations;
        if (index < 0) {// to handle negative indexes
            index = pattern.length + index - 1;
        }
        index = index % pattern.length;
        return pattern[index];
    }


}
