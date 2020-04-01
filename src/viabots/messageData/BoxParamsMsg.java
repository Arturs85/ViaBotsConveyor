package viabots.messageData;

import viabots.BoxType;
import viabots.ConveyorAgent;
import viabots.TwoWaySerialComm;
import viabots.behaviours.ConveyorAgentBehaviour;
import viabots.behaviours.ConveyorModelingBehaviour;

import java.io.BufferedReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import static viabots.BoxType.*;
import static viabots.BoxType.C;

public class BoxParamsMsg implements Serializable {
  public  BoxType pattern[] = new BoxType[]{B, A, B, C,B,A,B,B,C,C,C,C};//{B, A, A, C,A,B,B,C,C,C};
  public    int[][] boxContents = new int[][]{new int[]{1, 1, 1, 1, 0, 1}, new int[]{1, 0, 1, 1, 0, 1}, new int[]{0, 1, 1, 0, 1, 0}};// first index- boxType ordinal, contents of arrays- required positions for boxType
public int[] sensorPositions = null;
    public int toolChangeTime;
    public BoxParamsMsg(BoxType[] pattern, int[][] boxContents) {
        this.pattern = pattern;
        this.boxContents = boxContents;
    }

    public   static BoxType[] parsePatternFromString(String s){
    ArrayList<BoxType> res = new ArrayList<>(10);

    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
      BoxType bt = BoxType.fromChar(c);
        if(bt!=null) res.add(bt);
        //else return null;
    }
BoxType r[]=new BoxType[res.size()];
        res.toArray(r);
return r;
}
public static int[][] parseBoxContentsFromString(String s){
        ArrayList<ArrayList<Integer> > res= new ArrayList<ArrayList<Integer>>();
   ArrayList<Integer> curBox=null;
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
       switch (c){
           case '{': curBox = new ArrayList<>(6);
           break;
           case '}':
               if(curBox.size()==6)
               res.add(curBox);
               else
                   return null;
           break;
           case '1':curBox.add(1);
           break;
           case '0':curBox.add(0);
               break;
           default:
               break;

       }
    }
    int[][] r = new int[res.size()][];
    for (int i = 0; i < res.size(); i++) {
        ArrayList<Integer> b=res.get(i);
       r[i]=new int[b.size()];
        for (int j = 0; j < b.size(); j++) {
            r[i][j]=b.get(j);
        }
    }
    return  r;
    }
public  static int[] parseSensorPositionsFromString(String s){
    StringTokenizer st = new StringTokenizer(s);
   ArrayList<Integer> pos = new ArrayList<>(5);

   try {
       while(true) {
           pos.add(Integer.parseInt(st.nextToken(",")));
       }
   }catch (NoSuchElementException e){
      //string is parsed
   }
   if(pos.size()!= ConveyorModelingBehaviour.numberOfSensors) return null;
   int[] res = new int[pos.size()];
    for (int i = 0; i < pos.size(); i++) {
        res[i]=pos.get(i);
    }
    return res;
}


}
