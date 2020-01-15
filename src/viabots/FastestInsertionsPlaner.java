package viabots;

import viabots.behaviours.ConeType;
import viabots.messageData.ManipulatorModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class FastestInsertionsPlaner {
    Map<String, ManipulatorModel> s1ModelsList;// = new TreeMap<>();

    public FastestInsertionsPlaner(Map<String, ManipulatorModel> s1ModelsList) {
        this.s1ModelsList = s1ModelsList;
    }

    /**
     * @param positionList positions where cones are needed to be inserted
     * @param coneType
     * @return Map of positions in the box(0-5) to the agent names Strings, or null if there are no enough cones
     */

    public Map<Integer, String> makePlan(ArrayList<Integer> positionList, ConeType coneType) {
        Map<Integer, String> workersToPositionsMap = new TreeMap<Integer, String>();
        int coneCount = positionList.size();
        while (coneCount > 0) {
            coneCount--;
            int fastestSoFar = Integer.MAX_VALUE;
            String fastestName = null;


            // using iterators
            Iterator<Map.Entry<String, ManipulatorModel>> itr = s1ModelsList.entrySet().iterator();

            //find fastest current inserter
            while (itr.hasNext()) {
                Map.Entry<String, ManipulatorModel> entry = itr.next();
                // System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                if (entry.getValue().peekInsertionTime(coneType) < fastestSoFar) {
                    fastestSoFar = entry.getValue().peekInsertionTime(coneType);
                    fastestName = entry.getKey();
                }
            }
// update state- model fstest insertion
            if (fastestName == null) return null; //can't make plan - not enough resources
            s1ModelsList.get(fastestName).insert(coneType);
            workersToPositionsMap.put(positionList.get(coneCount), fastestName);// order of inserters is not relevant this case
//carry on with next position
        }
        System.out.println("plan has been made from nr of models: " + s1ModelsList.size());
        return workersToPositionsMap;
    }


}
