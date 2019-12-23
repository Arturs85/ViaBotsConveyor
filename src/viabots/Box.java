package viabots;

import viabots.behaviours.ConeType;

import java.io.Serializable;
import java.util.ArrayList;

public class Box implements Serializable {
    static int idCounter = 0;
    static final int[][] boxContents = new int[][]{new int[]{1, 1, 1, 0, 0, 0}, new int[]{0, 1, 1, 1, 0, 0}, new int[]{0, 0, 0, 1, 1, 1}};// first index- boxType ordinal, contents of arrays- required positions for boxType
    static final ConeType[] baseBoxModel = new ConeType[]{ConeType.A, ConeType.B, ConeType.B, ConeType.B, ConeType.B, ConeType.A};//cone types for each position
/*
     ____
    |1 2 |
    |3 4 |
    |5 6 |
     ----
       |
       V
    front
 1,6 = big cones- type A
 */


    public int id;
    public BoxType boxType;

    /**
     * for uniqe boxes
     *
     * @param boxType
     */
    public Box(BoxType boxType) {
        this.id = idCounter++;
        this.boxType = boxType;
    }

    public Box(int id, BoxType boxType) {
        this.id = id;
        this.boxType = boxType;
    }

    /**
     * @param coneType
     * @param boxType
     * @return Arrylist of integer positions for given ConeType in box of given Boxtype
     */
    public static ArrayList<Integer> getPositions(ConeType coneType, BoxType boxType) {
        ArrayList list = new ArrayList<Integer>(3);
        for (int i = 0; i < baseBoxModel.length; i++) {
            if (baseBoxModel[i].equals(coneType) && boxContents[boxType.ordinal()][i] == 1) {
                list.add(i);
            }
        }
        return list;
    }

    public ArrayList<Integer> getPositions(ConeType coneType) {
        return getPositions(coneType, boxType);

    }


}
