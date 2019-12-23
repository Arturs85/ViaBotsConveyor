package viabots;

import jade.core.AID;
import viabots.behaviours.ConeType;

import java.util.ArrayList;

public class BoxWInserters extends Box implements Comparable {
    AID[] inserters = new AID[baseBoxModel.length];

    public BoxWInserters(int id, BoxType boxType) {
        super(id, boxType);
    }

    public void setInserter(AID agent, int position) {
        inserters[position] = agent;
    }

    public boolean hasAllInserters(ConeType coneType) {
        for (int i = 0; i < baseBoxModel.length; i++) {
            if (baseBoxModel[i].equals(coneType) && inserters[i] != null) {

            } else return false;// there is no inserter for i position
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return id == ((BoxWInserters) obj).id;
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(id, ((BoxWInserters) o).id);

    }
}
