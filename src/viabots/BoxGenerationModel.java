package viabots;

import viabots.behaviours.ConeType;

import javax.swing.*;

import static viabots.BoxType.*;

public class BoxGenerationModel {

    BoxType pattern[] = new BoxType[]{A, A, B, C};
    int counter = -1;

    public BoxType getNextFromPattern() {
        counter++;
        return pattern[counter % pattern.length];

    }


}
