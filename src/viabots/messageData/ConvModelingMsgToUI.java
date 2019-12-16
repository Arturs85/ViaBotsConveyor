package viabots.messageData;

import viabots.Box;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ConvModelingMsgToUI implements Serializable {
    public List<LinkedList<Box>> boxQueues;

    public ConvModelingMsgToUI(List<LinkedList<Box>> boxQueues) {
        this.boxQueues = boxQueues;
    }


}
