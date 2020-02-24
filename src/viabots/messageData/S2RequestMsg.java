package viabots.messageData;

import viabots.behaviours.ConeType;

import java.io.Serializable;

public class S2RequestMsg implements Serializable {
    public ConeType coneType;
    public double cVal;

    public S2RequestMsg(ConeType coneType, double cVal) {
        this.coneType = coneType;
        this.cVal = cVal;
    }


}
