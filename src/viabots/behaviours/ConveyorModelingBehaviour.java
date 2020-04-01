package viabots.behaviours;

import GUI.GUIAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import viabots.*;
import viabots.messageData.BoxMessage;
import viabots.messageData.ConvModelingMsgToUI;
import viabots.messageData.ConveyorOntologies;
import viabots.messageData.TopicNames;

import java.io.IOException;
import java.util.*;

public class ConveyorModelingBehaviour extends BaseTopicBasedTickerBehaviour {
    public static final int numberOfSensors = 5; // there will be as much queues as there is sensors

    List<LinkedList<Box>> boxQueues = new ArrayList<>(numberOfSensors);
    ManipulatorAgent owner;
    public AID conveyorMsgTopic;
    public AID modelerToGuiTopic;
    ArrayList<BoxMessage> stopRequests = new ArrayList<>();
    MessageTemplate convMsgTpl;
    ArrayList<Box> currentBoxes = new ArrayList<>(); // this box should be valid from moment when message "boxStoppedAt" is sent till "moveOn is received"
    boolean shouldSendMoveOn = false;
    ArrayList<Box> lastCurrentBoxes = new ArrayList<>();

    BoxTimeCounter boxTimeCounter = new BoxTimeCounter(boxQueues, lastCurrentBoxes);// for statistics

    //normally there should be only one box in this list, but if two sensors has fired nearly same time there can be more than one box
    public ConveyorModelingBehaviour(ManipulatorAgent a) {
        super(a);
        owner = a;
        for (int i = 0; i < numberOfSensors; i++) {// initialize conveyor model - list of queues of Boxes
            boxQueues.add(new LinkedList<Box>());
        }
        //  boxQueues.get(0).add(new Box(BoxType.C));//for testing
        //  boxQueues.get(2).add(new Box(BoxType.B));//for testing
        //  boxQueues.get(2).add(new Box(BoxType.C));//for testing

        subscribeToMessages();

    }

    void subscribeToMessages() {
        createAndRegisterReceivingTopics(TopicNames.CONVEYOR_OUT_TOPIC);
        createAndRegisterReceivingTopics(TopicNames.REQUESTS_TO_MODELER);
        createSendingTopic(TopicNames.MODELER_GUI);
        createSendingTopic(TopicNames.MODELER_NEW_BOX_TOPIC);
        createSendingTopic(TopicNames.CONVEYOR_IN_TOPIC);
createSendingTopic(TopicNames.BOX_TIME_COUNTER_TOPIC);
//        conveyorMsgTopic = owner.createTopicForBehaviour(TopicNames.CONVEYOR_TOPIC.name());
//        convMsgTpl = MessageTemplate.MatchTopic(conveyorMsgTopic);
//        owner.registerBehaviourToTopic(conveyorMsgTopic);
//
//        modelerToGuiTopic = owner.createTopicForBehaviour(TopicNames.MODELER_GUI.name());//for sending
    }

    void unsubscribe() {//in case behaviour would be removed in runtime


    }
boolean isBeltRunning = false;// to determine if issue moveOn command
    void processMessages(MessageTemplate template) {// reads all available messages of coresponding template
        ACLMessage msg = owner.receive(template);
        while (msg != null) {
            //  owner.sendLogMsgToGui(getBehaviourName() + " received conv msg: " + msg.getContent());

            if (msg.getContent().contains(ConveyorAgent.boxArrived)) {// add new bo to first queue
                String boxTypeString = msg.getContent().substring(ConveyorAgent.boxArrived.length() + 1);
                BoxType type = BoxType.valueOf(boxTypeString);// make this with ontologie and message object
                Box b = new Box(type);
                Log.soutWTime("Modeler received box arrived, type : " + type);
                boxQueues.get(0).add(b);
                currentBoxes.add(b);// mark that this box will prevent belt from turning on, if it is stopped (belt will stop at first sensor)

// send new box information on own topic
                sendNewBoxMessage(b);
            } else if (msg.getContent().contains(ConveyorAgent.stoppedAt)) {
                isBeltRunning = false;
                char position = msg.getContent().charAt(ConveyorAgent.stoppedAt.length());
                owner.sendLogMsgToGui(getBehaviourName() + " received sopped at, read char: " + position);
                Log.soutWTime("sending log " + getBehaviourName() + " received sopped at, read char: " + position);

                putBoxToNextQueue(position);
                lastCurrentBoxes = new ArrayList<>(currentBoxes);


              boxTimeCounter.currentBoxes= lastCurrentBoxes;
               Log.soutWTime("cmb --->>> lastCurrentBoxes size: "+lastCurrentBoxes.size()); // for testing

                boxTimeCounter.stoppedAtSensor(position == 'A');
            }else if (msg.getContent().contains(ConveyorAgent.triggerAt)) {
                char position = msg.getContent().charAt(ConveyorAgent.triggerAt.length());
                owner.sendLogMsgToGui(getBehaviourName() + " received trigger at, read char: " + position);
                Log.soutWTime("sending log " + getBehaviourName() + " received trigger at, read char: " + position);
toTheNext(Integer.valueOf(String.valueOf(position))-1);
            }
            msg = owner.receive(template);
        }

    }
void putBoxToNextQueue(char position){
    switch (position) {
        case 'A':

            toTheNext(0);
            break;
        case 'B':
            toTheNext(1);
            break;
        case 'C':
            toTheNext(2);
            break;
        case 'D':
            toTheNext(3);
            break;
        case 'E':
            toTheNext(4);
            break;
        case 'F':
            toTheNext(5);

            break;
        default:
            break;

    }

}
    /**
     * moves first box of queue before this sensor to next queue, or removes it if there are no more queues
     */
    void toTheNext(int sensorNumber) {
        // owner.sendLogMsgToGui(getBehaviourName() + " toTheNextCalled with sensor nr:" + sensorNumber);
        if (boxQueues.get(sensorNumber).isEmpty()) {
            Log.soutWTime("Modeler -err- queue empty, qnr : " + sensorNumber);
            return;// if there is no box, than there was unauthorized triggering of this sensor
        }
        Box box = boxQueues.get(sensorNumber).removeFirst();// todo add queues size check

        if ((sensorNumber + 1) >= boxQueues.size()) {//no more queues, erease box(do nothing)
            boxTimeCounter.addToFinished(box);
            if (box.id % 5 == 0) {
            //sendTimesToGUI();// statistics- results
        owner.sendLogMsgToGui(boxTimeCounter.processedBoxesToString());
        }
        } else {
            boxQueues.get(sensorNumber + 1).add(box);
//check if this box if first in next queue, if so and if next sensor is subscribed to it, then send to conv "stop at sensor x"

            if (boxQueues.get(sensorNumber + 1).getFirst().equals(box)) {
                //find if next sensor is subscribed at this box
                if (shouldStop(box.id, sensorNumber + 1)) {
                    //send conveyor to stop at next trigger of this sensor
                    sendStopAtNextTrigger(sensorNumber + 1);
                }
            }
        }
        //check if next box is subscribed by this sens
        if (shouldStopForNextBox(sensorNumber)) {
            sendStopAtNextTrigger(sensorNumber);
        }
// check if there is someone subscribed at this sensor position for current box
        AID subscriber = getSubscriber(box.id, sensorNumber);
        if (subscriber != null) {
            // at this point box is awaiting at the sensor
            sendBoxStoppedAt(box.id, subscriber);
            currentBoxes.add(box);
        }
//        if (sensorNumber == 0) {//new box arrived at sensor 0, add it to current boxes until inserters fo it are planed
//            currentBoxes.add(box);// s3 can attempt to clear this box before it is in this list
//        }
        if (currentBoxes.isEmpty()) {
           // Log.soutWTime(" no subscribers for sensor " + sensorNumber + " for box " + box.id + " sending move on to conv");

            if(!isBeltRunning)
                shouldSendMoveOn = true;//sendMoveOnMessage();// this should only be sent after all stopped at messages are processed
        }
        sendConvModelMsgToGUI();

    }

    boolean shouldStop(int boxId, int sensorNr) {
        for (BoxMessage b : stopRequests) {
            if (b.boxID == boxId && b.positionInBox == sensorNr) {//position in box used as position of sensor along the conveyor
                return true;
            }
        }
        return false;
    }

    boolean shouldStopForNextBox(int sensorNr) {
        if (boxQueues.get(sensorNr).isEmpty()) return false;
        Box nextBox = boxQueues.get(sensorNr).getFirst();
        return shouldStop(nextBox.id, sensorNr);

    }

    AID getSubscriber(int boxId, int sensorNumber) {
        for (BoxMessage request : stopRequests) {
            if (request.positionInBox == sensorNumber && request.boxID == boxId) {
                return request.subscriber;
            }
        }
        return null;
    }

    public void receiveStopOrMoveOnRequestMessage() {// from s1 or s3
        ACLMessage msg = owner.receive(templates[TopicNames.REQUESTS_TO_MODELER.ordinal()]);
        if (msg != null) {
            receiveStopOrMoveOnRequestMessage();
            BoxMessage boxMessage = null;
            try {
                boxMessage = (BoxMessage) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }


            if (msg.getPerformative() == ACLMessage.REQUEST) {

                // add requested stop position of this box and store subscribers AID
                boxMessage.subscriber = msg.getSender();// in case subscriber has not filled this field
                stopRequests.add(boxMessage);//positionId means sensor position

                Log.soutWTime("-------request from " + boxMessage.subscriber.getLocalName() + "- for box " + boxMessage.boxID);

            } else if (msg.getPerformative() == ACLMessage.CONFIRM) {// belt can continue to move
//remove this box from current list


                Iterator<Box> i = currentBoxes.iterator();
                while (i.hasNext()) {
                    Box b = i.next(); // must be called before you can call i.remove()
                    if (b.id == boxMessage.boxID) {
                        i.remove();
                    }
                }
// if currentBox list is empty conv can move on
                //if (currentBoxes.isEmpty() && !isBeltRunning) {
                if (currentBoxes.isEmpty()) {
                    shouldSendMoveOn = true;//sendMoveOnMessage();
                  //  Log.soutWTime(getBehaviourName() + " move on msg to conveyor sent");
                } else {
                    Log.soutWTime("there are boxes still in current list: " + currentBoxes.size() + "  - " + currentBoxes.toString());
                }
                Log.soutWTime(getBehaviourName() + " confirm move on received " + msg.getSender().getLocalName());

            }

        }

    }

    void sendConvModelMsgToGUI() {//sends whole model

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        try {
            msg.setContentObject(new ConvModelingMsgToUI(boxQueues));
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(sendingTopics[TopicNames.MODELER_GUI.ordinal()]);
        owner.send(msg);
         Log.soutWTime("boxes on the belt :  "+ GUIAgent.getModelAsString(boxQueues));

    }

    void sendMoveOnMessage() {// to conveyor
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(sendingTopics[TopicNames.CONVEYOR_IN_TOPIC.ordinal()]);
// no content is needed for this message
        owner.send(msg);
    isBeltRunning = true;
        Log.soutWTime(getBehaviourName() + " -----======------sending move on to conveyor");
        boxTimeCounter.startedMovingOn();
    }

    void sendBoxStoppedAt(int boxId, AID subscriber) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology(ConveyorOntologies.BoxAtSatation.name());
        msg.addReceiver(subscriber);
        BoxMessage contObj = new BoxMessage(boxId, null);
        try {
            msg.setContentObject(contObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.soutWTime("sent msg box stopped at your station to " + subscriber.getLocalName() + " about boxId " + boxId);
        owner.send(msg);
    }

    void sendStopAtNextTrigger(int sensorNr) {//to conveyor
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(sendingTopics[TopicNames.CONVEYOR_IN_TOPIC.ordinal()]);
        try {
            msg.setContentObject(new BoxMessage(0, null, sensorNr));
        } catch (IOException e) {
            e.printStackTrace();
        }
       Log.soutWTime("sending to conv: stop at sensor "+sensorNr);
        owner.send(msg);
    }
void sendTimesToGUI(){
    ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
    msg.addReceiver(sendingTopics[TopicNames.BOX_TIME_COUNTER_TOPIC.ordinal()]);
    try {
        msg.setContentObject(boxTimeCounter.processedBoxes);
    } catch (IOException e) {
        e.printStackTrace();
    }
    owner.send(msg);
}

    public void sendNewBoxMessage(Box box) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.setOntology(ConveyorOntologies.NewBoxWithID.name());
        BoxMessage contObj = new BoxMessage(box.id, box.boxType);
        try {
            msg.setContentObject(contObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.MODELER_NEW_BOX_TOPIC.ordinal()]);
        owner.send(msg);
   Log.soutWTime("==========  sent new box message: "+box.id+box.boxType+" ==========");
    }
int printIntervalCounter=0;//for testing
    @Override
    protected void onTick() {
        shouldSendMoveOn = false;
        processMessages(templates[TopicNames.CONVEYOR_OUT_TOPIC.ordinal()]);
        receiveStopOrMoveOnRequestMessage();
        if (shouldSendMoveOn && currentBoxes.isEmpty()) {// double check on curr boxes, can bee refractored in prior calls in this tick
            sendMoveOnMessage();
        }else{
        printIntervalCounter++;
            if (printIntervalCounter > 50) {
                printIntervalCounter=0;
                Log.soutWTime("cant move on because curBoxes size: "+ currentBoxes.size() + "  - " + currentBoxes.toString());
            }
        }
    }

    public static BoxType boxHasLeftConv(List<LinkedList<Box>> prevBoxQueues, List<LinkedList<Box>> actualBoxQueues) {
        if (prevBoxQueues.get(prevBoxQueues.size() - 1).isEmpty()) return null;

        if (actualBoxQueues.get(actualBoxQueues.size() - 1).isEmpty())
            return prevBoxQueues.get(prevBoxQueues.size() - 1).getLast().boxType;
        if (prevBoxQueues.get(prevBoxQueues.size() - 1).getLast().id != actualBoxQueues.get(actualBoxQueues.size() - 1).getLast().id) {
            return prevBoxQueues.get(prevBoxQueues.size() - 1).getLast().boxType;

        }
        return null;
    }
}
