package viabots.behaviours;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import viabots.*;
import viabots.messageData.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class S1ManipulatorBehaviour extends BaseTopicBasedTickerBehaviour {
    ManipulatorAgent master;
    EnumSet<ConeType> enabledParts;
    ManipulatorModel manipulatorModel;// manipulator specific properties- ready for sending as msg object
    S1States state = S1States.IDLE;
    TreeMap<Integer, ArrayList<Integer>> toDoList = new TreeMap<>();//key- boxId, value- list of Positions to insert cone
    Integer currentBoxId = null;
    Integer currentPosition = null;// cone position in box
    MessageTemplate boxAtStationTpl;
    MessageTemplate taskAssignmentTpl;
    MessageTemplate coneAssignmentTpl;
    int stationPosition = 0; // box sensor number
    ConcurrentLinkedDeque<String> hardwareMsgQueue;
    ConeType previousType;
    final static int typeChangeTimeMs = 5000;
    int typeChangeCounter = 0;
//boolean isHoldingCone=false;

    public S1ManipulatorBehaviour(ManipulatorAgent manipulatorAgent, Integer sensorPosition) {
        super(manipulatorAgent);

        master = manipulatorAgent;
        hardwareMsgQueue = master.hardwareMsgQueue;
        enabledParts = EnumSet.noneOf(ConeType.class);
        createAndRegisterReceivingTopics(TopicNames.S2_TO_S1_TOPIC);
        manipulatorModel = new ManipulatorModel(owner.getLocalName(), ConeType.A, manipulatorAgent.type);//default cone type is set here!!!
        previousType = manipulatorModel.currentCone;
        boxAtStationTpl = MessageTemplate.MatchOntology(ConveyorOntologies.BoxAtSatation.name());
        taskAssignmentTpl = MessageTemplate.MatchOntology(ConveyorOntologies.TaskAssignmentToS1.name());
        coneAssignmentTpl = MessageTemplate.MatchOntology(ConveyorOntologies.ChangeConeType.name());

        createAndRegisterReceivingTopics(TopicNames.UI_TO_MANIPULATOR);
        createSendingTopic(TopicNames.S1_TO_S2_TOPIC);
        createSendingTopic(TopicNames.REQUESTS_TO_MODELER);
        stationPosition = sensorPosition;
        System.out.println("created agent at sensor position " + sensorPosition);
        //  GuiInteractionBehaviour.sendConeTypeChanged(owner, manipulatorModel.currentCone);// For initial cone type to bee visible on gui

    }

    void setCurentConeType(ConeType coneType) {//for changing cone type that robot currently is set to insert
        manipulatorModel.currentCone = coneType;
    }

    void insertPart(ConeType coneType) {
        master.insertPart(coneType);
    }

    int printCounter = 0;//for testing

    @Override
    protected void onTick() {
        receiveInfoRequestMessage();
        receiveInsertionRequestMessage();
        receiveBoxArrivedMessage();
        receiveTypeChangeMsg();
        receiveUImessage();

        switch (state) {
            case IDLE:
// try to pick up cone for next box, that will arrive i.e. box with least id on the todolist
                Integer nextPosition = getNextConeToPrepeare();
                if (nextPosition != null) {//should prepare cone because there is known next box with positions
                    currentPosition = nextPosition;
                    // dont set curBoxId yet, because that would mean that box has arrived
                    ConeType nextConeType = Box.getConeTypeForBoxPosition(currentPosition);
                    if (nextConeType != previousType) {// start tool change
                        Log.soutWTime("Starting cone type change to: " + nextConeType);
                        typeChangeCounter = typeChangeTimeMs / ViaBotAgent.tickerPeriod;
                        state = S1States.CHANGING_TYPE;
                        break;
                    }
                    startConePickup(Box.getConeTypeForBoxPosition(currentPosition));

                    Log.soutWTime("Starting cone pickup, pos: " + currentPosition);
                }
                break;
            case PICKING_FROM_HOLDER:
                if (receiveHardwareMsgsOperationCompleate()) {
                    //master.communication.listenForReplyWTimeout(CommunicationWithHardware.SO_READ_TIMEOUT_MS);// this call blocks , no behaviours is executed during this time
                    Log.soutWTime(getAgent().getLocalName() + " received from hardware -pickup complete - state = holding");
                    state = S1States.HOLDING_CONE;
                } else {
                }

                break;
            case HOLDING_CONE:
                if (currentBoxId != null) {//means that box is at sensor
                    master.insertPartInPosition(currentPosition);
                    state = S1States.INSERTING;
                    Log.soutWTime("Box " + currentBoxId + " is waiting at this station");
                } else {

                }
                break;
            case INSERTING:
                //wait for reply from hardware

                if (receiveHardwareMsgsOperationCompleate()) {
                    //   master.communication.listenForReplyWTimeout(CommunicationWithHardware.SO_READ_TIMEOUT_MS);
                    //replay received , this means, that operation is done
                    // send message to conv modeler to move on, if there are no more jobs for this box at this station
                    //todo decrese cone count available in model

                    Log.soutWTime(getBehaviourName() + " received from hardware -insertion complete ");
                    previousType = Box.getConeTypeForBoxPosition(currentPosition);// to know if manip needs change tool for next cone
                    toDoList.get(currentBoxId).remove(currentPosition);// removes position of newly inserted cone from the todolist
                    currentPosition = null;
                    if (toDoList.get(currentBoxId).isEmpty()) {
                        sendInsertionCompleteAtStation(new BoxMessage(currentBoxId, null));
                        toDoList.remove(currentBoxId); // remove processed box, so that manipulator can pick next box on the list and prepeare cone for it
                        currentBoxId = null;
                        state = S1States.IDLE;
                    } else {// there still is some position on the list
                        Log.soutWTime(getBehaviourName() + " proceeding with next cone insertion ");

                        currentPosition = toDoList.get(currentBoxId).get(toDoList.get(currentBoxId).size() - 1);// get next position
                        // master.insertPartInPosition(currentPosition);
                        startConePickup(Box.getConeTypeForBoxPosition(currentPosition));// should check if tool change is needed also here (on same box)
                    }
                } else {
                    //e.printStackTrace();
                    // most likely timeout occured -continue to wait for reply
                    //Log.soutWTime(getBehaviourName() + " did not received confirm from hardware - " + e.getClass().getName());

                }
                break;
            case CHANGING_TYPE:
                typeChangeCounter--;
                if (typeChangeCounter <= 0) {// tool change has ended
                    previousType = Box.getConeTypeForBoxPosition(currentPosition);//mark, that type is changed
                    state = S1States.IDLE;
                    Log.soutWTime("S1 Finished type change: " + previousType);
                }

                break;

        }


//        try {
//            master.communication.listenForReplyWTimeout();
//            Log.soutWTime(getBehaviourName() + " insertion ok");
//
//        } catch (IOException e) {
//            //e.printStackTrace();
//            // Log.soutWTime(getBehaviourName() + "did not receive  insertion ok msg within timeout");
//        }
    }

    /**
     * call this after processing all topic messages, because this call will empty msg queue
     */
    public void receiveTypeChangeMsg() {// further needs update to receive box id
        ACLMessage msg = master.receive(coneAssignmentTpl);
        while (msg != null) {
            S1ToS2Message msgObj = null;
            try {
                msgObj = (S1ToS2Message) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            Log.soutWTime(master.getLocalName() + " changing cone type from " + manipulatorModel.currentCone + " to " + msgObj.currentConeType);
            setCurentConeType(msgObj.currentConeType);
            GuiInteractionBehaviour.sendConeTypeChanged(owner, manipulatorModel.currentCone);
            msg = master.receive(coneAssignmentTpl);

        }
    }

    public void receiveBoxArrivedMessage() {//from conv modeling
        ACLMessage msg = master.receive(boxAtStationTpl);
        if (msg != null) {
            BoxMessage boxMessage = null;
            try {
                boxMessage = (BoxMessage) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            // insert cones according to the toDoList
            currentBoxId = boxMessage.boxID;
            if (currentPosition == null)//means that manipulator has not prepared cone
                currentPosition = toDoList.get(currentBoxId).get(toDoList.get(currentBoxId).size() - 1);// get first position

            if (state.equals(S1States.HOLDING_CONE)) {
                master.insertPartInPosition(currentPosition);//
                state = S1States.INSERTING;
            } else if (state.equals(S1States.IDLE)) {//pick up cone, this shold not be executed, because first cone should always be prepeared
                Log.soutWTime("this should not be executed, because first cone should always be prepared");
                startConePickup(Box.getConeTypeForBoxPosition(currentPosition));
            }


            Log.soutWTime("box id: " + boxMessage.boxID + " stopped at station received  " + master.getName());

        }
    }

    //process all msgs of this topic
    public void receiveInfoRequestMessage() {//from s2
        ACLMessage msg = master.receive(templates[TopicNames.S2_TO_S1_TOPIC.ordinal()]);
        if (msg != null) {
            if (msg.getOntology().equals(ConveyorOntologies.NewBoxWithID.name())) {
                BoxMessage boxMessage = null;
                try {
                    boxMessage = (BoxMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                Log.soutWTime(getBehaviourName() + " received info request with cone type: " + boxMessage.coneType + " behaviours cur cone: " + manipulatorModel.currentCone);
                if (!manipulatorModel.currentCone.equals(boxMessage.coneType)) {//do nothing if receive request from other s2
                    return;
                }
                // form the ansver to this info request of this box id
                ManipulatorModel model = makeModel(boxMessage.boxID);
                sendinfoToS2(model);

                Log.soutWTime("request info msg from s2 received  " + master.getName());
            }
        } //else
    }


    public void receiveInsertionRequestMessage() {//from s2, sent by name
        ACLMessage msg = master.receive(taskAssignmentTpl);
        if (msg != null) {
            if (msg.getOntology().equals(ConveyorOntologies.TaskAssignmentToS1.name())) {//remove double check
                BoxMessage boxMessage = null;
                try {
                    boxMessage = (BoxMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

                sendStopBoxAtStation(new BoxMessage(boxMessage.boxID, boxMessage.boxType, boxMessage.coneType));
                // add job to the list
                addJobToTheList(boxMessage.boxID, boxMessage.positionInBox);

                manipulatorModel.insert(Box.getConeTypeForBoxPosition(boxMessage.positionInBox));  // mark that cone is reserved - remove it from array(it will be picked from holder later )
                GuiInteractionBehaviour.sendConeCountChanged(master, manipulatorModel.conesAvailable);

                Log.soutWTime("request insertion msg from s2 received  " + master.getName() + " for position " + boxMessage.positionInBox);

                sendAcceptAssignmentToS2(boxMessage);
            }
        } //else
    }

    void addJobToTheList(int boxId, int conePosition) {
        ArrayList<Integer> positions = toDoList.get(boxId);
        if (positions == null) {
            positions = new ArrayList<>(4);
            toDoList.put(boxId, positions);
        }
        positions.add(conePosition);
// pick up cone for next insertion, if has not holding one already
//        if (!state.equals(S1States.PICKING_FROM_HOLDER) && !state.equals(S1States.HOLDING_CONE)) {
//            Log.soutWTime("Starting cone pickup because of new job added");
//            startConePickup(Box.getConeTypeForBoxPosition(conePosition));
//        }
    }

    void startConePickup(ConeType coneType) {
        master.pickUpCone(coneType);
        state = S1States.PICKING_FROM_HOLDER;
    }

    Integer getNextConeToPrepeare() {// see if there is jobs on the list, and prepare cone for that job
        if (toDoList.isEmpty()) return null;

        Integer nextBox = toDoList.firstKey();
        if (nextBox == null) return null;

        ArrayList<Integer> positions = toDoList.get(nextBox);// get next box position list
        if (positions.isEmpty()) {
            Log.soutWTime("job with no positions??");
            return null;
        }
        return positions.get(0);
    }

    /**
     * checks msgs from manipulator hardware, if it contains "inserted". normally there should not bee more than one msg,
     * because this check should be called more often than insertion time. Other msgs are ignored
     *
     * @return true if confirmation is received
     */
    boolean receiveHardwareMsgsOperationCompleate() {//
        if (hardwareMsgQueue.isEmpty()) return false;

        String msg = hardwareMsgQueue.pollLast();
        if (msg == null) return false;

        if (msg.contains("INSERTED")) {
            Log.soutWTime("hardwareMsgQueue size: " + hardwareMsgQueue.size());
            return true;
        }
        return false;
    }

    ManipulatorModel makeModel(int boxId) {
        return manipulatorModel;// needs to be extended to update speeds according  if the requested box id is already on  the agents work list
    }

    void sendinfoToS2(ManipulatorModel model) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);//inform
        model.reset();//reset isFirstInsertionDone before sending, so that planner gets right value - false. isFirstInsertionDone is meant to use for planner only
        try {
            msg.setContentObject(model);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(sendingTopics[TopicNames.S1_TO_S2_TOPIC.ordinal()]);
        owner.sendLogMsgToGui("manip sends to topic: " + sendingTopics[TopicNames.S1_TO_S2_TOPIC.ordinal()].toString());
        owner.send(msg);
        Log.soutWTime(getAgent().getLocalName() + model.currentCone + " info Msg sent to s2 topic");
    }

    void sendAcceptAssignmentToS2(BoxMessage boxMessage) {
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        boxMessage.coneType = manipulatorModel.currentCone;
        try {
            msg.setContentObject(boxMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(sendingTopics[TopicNames.S1_TO_S2_TOPIC.ordinal()]);//should send reply only to assigner
        owner.send(msg);
    }

    void sendStopBoxAtStation(BoxMessage boxMessage) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        boxMessage.positionInBox = stationPosition;// this case positioninBox is actually used to inform conv modeler of position number of sensor
        try {
            msg.setContentObject(boxMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.REQUESTS_TO_MODELER.ordinal()]);
        owner.send(msg);
        Log.soutWTime(getAgent().getLocalName() + " sent msg stopBoxAtStation to modeler, boxid " + boxMessage.boxID);
    }

    void sendInsertionCompleteAtStation(BoxMessage boxMessage) {
        ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
        boxMessage.positionInBox = stationPosition;// this case positioninBox is actually used to inform conv modeler of position number of sensor
        try {
            msg.setContentObject(boxMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.REQUESTS_TO_MODELER.ordinal()]);
        owner.send(msg);
        Log.soutWTime(getAgent().getLocalName() + " sent msg insertion complete to modeler, boxid " + boxMessage.boxID);

    }

    public void receiveUImessage() {//for testing insertion
        ACLMessage msg = master.receive(owner.uiCommandTpl);// takes all msgs out of list, call as lasst recieve in iteration
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.REQUEST) {
                String cont = msg.getContent();
                if (cont != null) {
                    if (msg.getContent().equals(MessageContent.INSERT_PART_A.name()))
                        master.insertPart(ConeType.A);
                    else if (msg.getContent().equals(MessageContent.INSERT_PART_B.name()))
                        master.insertPart(ConeType.B);

                    Log.soutWTime("request msg from gui received msg:" + cont + ": " + master.getName());
                }
            } else if (msg.getPerformative() == ACLMessage.INFORM) {// this is part enable msg
                MessageToGUI messageObj = null;
                try {
                    messageObj = (MessageToGUI) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                if (messageObj != null) {
                    if (messageObj.enabledParts != null) {
                        enabledParts = messageObj.enabledParts;
                        enabledParts.forEach(coneType -> setCurentConeType(coneType));// sets last as current type- needs upgrade
                        Log.soutWTime(enabledParts.toString());
                    }
                    if (messageObj.coneCount != null) {//update available cone count, received from ui
                        for (int i = 0; i < messageObj.coneCount.length; i++) {
                            if (messageObj.coneCount[i] >= 0) {//negative values indicate no change in count
                                master.coneCountAvailable[i] = messageObj.coneCount[i];//todo use manipulator model insted
                                manipulatorModel.conesAvailable[i] = messageObj.coneCount[i];
                                Log.soutWTime(master.getLocalName() + " cone count update msg received: " + master.coneCountAvailable[i]);

                            }
                        }
                    }

                }

            } else {//put back message
                owner.postMessage(msg);// chances are that this function puts back msg on the qeue
            }


        } //else
    }

}
