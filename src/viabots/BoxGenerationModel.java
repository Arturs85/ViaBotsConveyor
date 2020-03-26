package viabots;

import viabots.behaviours.ConeType;

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

   public String getPatternAsString(){
       StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < pattern.length; i++) {
           sb.append(pattern[i].name());
           sb.append(' ');
       }
        return sb.toString();
   }

    BoxType getBoxTypeafAfter(int iterations) {

        int index = counter + iterations;
        index = index % pattern.length;
        return pattern[index];
    }


}
