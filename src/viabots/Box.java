package viabots;

import java.io.Serializable;

public class Box implements Serializable {
    static int idCounter = 0;
    public int id;
    public BoxType boxType;

    public Box(BoxType boxType) {
        this.id = idCounter++;
        this.boxType = boxType;
    }


}
