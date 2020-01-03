package viabots.messageData;

import jade.core.AID;
import viabots.BoxType;
import viabots.behaviours.ConeType;

import java.io.Serializable;

public class BoxMessage implements Serializable {
    public int boxID;
    public BoxType boxType;
    public int positionInBox = 0;// can be used as sensor position of conveyor
    public ConeType coneType;
    public AID subscriber;

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
