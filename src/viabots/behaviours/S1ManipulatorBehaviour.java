package viabots.behaviours;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import viabots.Box;
import viabots.CommunicationWithHardware;
import viabots.ManipulatorAgent;
import viabots.messageData.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.TreeMap;

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
//boolean isHoldingCone=false;

    public S1ManipulatorBehaviour(ManipulatorAgent manipulatorAgent, Integer sensorPosition) {
        super(manipulatorAgent);

        master = manipulatorAgent;
        enabledParts = EnumSet.noneOf(ConeType.class);
        createAndRegisterReceivingTopics(TopicNames.S2_TO_S1_TOPIC);
        manipulatorModel = new ManipulatorModel(owner.getLocalName(), ConeType.A);//default cone type is set here!!!
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
                    startConePickup(Box.getConeTypeForBoxPosition(currentPosition));

                }
                break;
            case PICKING_FROM_HOLDER:
                try {
                    master.communication.listenForReplyWTimeout(CommunicationWithHardware.SO_READ_TIMEOUT_MS);// this call blocks , no behaviours is executed during this time
                    System.out.println(getAgent().getLocalName() + " received from hardware -pickup complete ");
                    state = S1States.HOLDING_CONE;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case HOLDING_CONE:
                if (currentBoxId != null) {//means that box is at sensor
                    master.insertPartInPosition(currentPosition);
                    state = S1States.INSERTING;

                }
                break;
            case INSERTING:
                //wait for reply from hardware
                try {
                    master.communication.listenForReplyWTimeout(CommunicationWithHardware.SO_READ_TIMEOUT_MS);
                    //replay received , this means, that operation is done
                    // send message to conv modeler to move on, if there are no more jobs for this box at this station
                    //todo decrese cone count available in model

                    System.out.println(getBehaviourName() + " received from hardware -insertion complete ");
                    toDoList.get(currentBoxId).remove(currentPosition);// removes position of newly inserted cone from the todolist
                    currentPosition = null;
                    if (toDoList.get(currentBoxId).isEmpty()) {
                        sendInsertionCompleteAtStation(new BoxMessage(currentBoxId, null));
                        toDoList.remove(currentBoxId); // remove processed box, so that manipulator can pick next box on the list and prepeare cone for it
                        currentBoxId = null;
                       state = S1States.IDLE;
                    } else {// there still is some position on the list
                        System.out.println(getBehaviourName() + " proceeding with next cone insertion ");

                        currentPosition = toDoList.get(currentBoxId).get(toDoList.get(currentBoxId).size() - 1);// get next position
                        // master.insertPartInPosition(currentPosition);
                        startConePickup(Box.getConeTypeForBoxPosition(currentPosition));
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                    // most likely timeout occured -continue to wait for reply
                    System.out.println(getBehaviourName() + " did not received confirm from hardware - " + e.getClass().getName());

                }
                break;

        }


//        try {
//            master.communication.listenForReplyWTimeout();
//            System.out.println(getBehaviourName() + " insertion ok");
//
//        } catch (IOException e) {
//            //e.printStackTrace();
//            // System.out.println(getBehaviourName() + "did not receive  insertion ok msg within timeout");
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
            System.out.println(master.getLocalName() + " changing cone type from " + manipulatorModel.currentCone + " to " + msgObj.currentConeType);
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
            if(currentPosition==null)//means that manipulator has not prepared cone
            currentPosition = toDoList.get(currentBoxId).get(toDoList.get(currentBoxId).size() - 1);// get first position
            if (state.equals(S1States.HOLDING_CONE)) {
                master.insertPartInPosition(currentPosition);//
                state = S1States.INSERTING;
            } else {//pick up cone, this shold not be executed, because first cone should always be prepeared
                startConePickup(Box.getConeTypeForBoxPosition(currentPosition));
            }


            System.out.println("box id: " + boxMessage.boxID + " stopped at station received  " + master.getName());

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
                System.out.println(getBehaviourName() + " received info request with cone type: " + boxMessage.coneType + " behaviours cur cone: " + manipulatorModel.currentCone);
                if (!manipulatorModel.currentCone.equals(boxMessage.coneType)) {//do nothing if receive request from other s2
                    return;
                }
                // form the ansver to this info request of this box id
                ManipulatorModel model = makeModel(boxMessage.boxID);
                sendinfoToS2(model);

                System.out.println("request info msg from s2 received  " + master.getName());
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

                System.out.println("request insertion msg from s2 received  " + master.getName() + " for position " + boxMessage.positionInBox);

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
        if (!state.equals(S1States.PICKING_FROM_HOLDER) && !state.equals(S1States.HOLDING_CONE)) {
            startConePickup(Box.getConeTypeForBoxPosition(conePosition));
        }
    }

    void startConePickup(ConeType coneType) {
        master.pickUpCone(coneType);
        state = S1States.PICKING_FROM_HOLDER;
    }

    Integer getNextConeToPrepeare() {// see if there is jobs on the list, and prepare cone for that job
if(toDoList.isEmpty()) return null;

        Integer nextBox = toDoList.firstKey();
        if (nextBox == null) return null;

        ArrayList<Integer> positions = toDoList.get(nextBox);// get next box position list
        if (positions.isEmpty()) {
            System.out.println("job with no positions??");
            return null;
        }
        return positions.get(0);
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
        System.out.println(getAgent().getLocalName() + model.currentCone + " info Msg sent to s2 topic");
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
        System.out.println(getAgent().getLocalName() + " sent msg stopBoxAtStation to modeler, boxid " + boxMessage.boxID);
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
        System.out.println(getAgent().getLocalName() + " sent msg insertion complete to modeler, boxid " + boxMessage.boxID);

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

                    System.out.println("request msg from gui received msg:" + cont + ": " + master.getName());
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
                        System.out.println(enabledParts.toString());
                    }
                    if (messageObj.coneCount != null) {//update available cone count, received from ui
                        for (int i = 0; i < messageObj.coneCount.length; i++) {
                            if (messageObj.coneCount[i] >= 0) {//negative values indicate no change in count
                                master.coneCountAvailable[i] = messageObj.coneCount[i];//todo use manipulator model insted
                                manipulatorModel.conesAvailable[i] = messageObj.coneCount[i];
                                System.out.println(master.getLocalName() + " cone count update msg received: " + master.coneCountAvailable[i]);

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
