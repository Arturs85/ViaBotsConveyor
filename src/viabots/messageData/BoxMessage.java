package viabots.messageData;

import viabots.BoxType;
import viabots.behaviours.ConeType;

import java.io.Serializable;

public class BoxMessage implements Serializable {
    public int boxID;
    public BoxType boxType;
    public int positionInBox = 0;
    public ConeType coneType;

    public BoxMessage(int boxID, BoxType boxType, ConeType coneType) {
        this.boxID = boxID;
        this.boxType = boxType;
        this.coneType = coneType;
    }

    public BoxMessage(int boxID, BoxType boxType) {
        this.boxID = boxID;
        this.boxType = boxType;
    }

    public BoxMessage(int boxID, BoxType boxType, int positionInBox) {
        this.boxID = boxID;
        this.boxType = boxType;
        this.positionInBox = positionInBox;
    }
}
