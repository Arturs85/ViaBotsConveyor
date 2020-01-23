package viabots.behaviours;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import viabots.ManipulatorAgent;
import viabots.ViaBotAgent;
import viabots.messageData.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.TreeMap;

public class S1ManipulatorBehaviour extends BaseTopicBasedTickerBehaviour {
    ManipulatorAgent master;
    EnumSet<ConeType> enabledParts;
    ManipulatorModel manipulatorModel;// manipulator specific properties- ready for sending as msg object
    S1States state = S1States.IDLE;
    TreeMap<Integer, ArrayList<Integer>> toDoList = new TreeMap<>();//key- boxId, value- list of Positions to insert cone
    Integer currentBoxId = null;
    Integer currentPosition = null;
    MessageTemplate boxAtStationTpl;
    MessageTemplate taskAssignmentTpl;
    int stationPosition = 0; // box sensor number

    public S1ManipulatorBehaviour(ManipulatorAgent manipulatorAgent) {
        super(manipulatorAgent);

        master = manipulatorAgent;
        enabledParts = EnumSet.noneOf(ConeType.class);
        createAndRegisterReceivingTopics(TopicNames.S2_TO_S1_TOPIC);
        manipulatorModel = new ManipulatorModel(owner.getLocalName(), ConeType.A);//default cone type is set here!!!
        boxAtStationTpl = MessageTemplate.MatchOntology(ConveyorOntologies.BoxAtSatation.name());
        taskAssignmentTpl = MessageTemplate.MatchOntology(ConveyorOntologies.TaskAssignmentToS1.name());
        createAndRegisterReceivingTopics(TopicNames.UI_TO_MANIPULATOR);
        createSendingTopic(TopicNames.S1_TO_S2_TOPIC);
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
        receiveUImessage();

        switch (state) {
            case IDLE:

                break;

            case INSERTING:
                //wait for reply from hardware
                try {
                    master.communication.listenForReplyWTimeout();
                    //replay received , this means, that operation is done
                    // send message to conv modeler to move on, if there are no more jobs for this box at this station
                    toDoList.get(currentBoxId).remove(currentPosition);// removes position of newly inserted cone from the todolist
                    currentPosition = null;
                    if (toDoList.get(currentBoxId).isEmpty()) {
                        sendInsertionCompleteAtStation(new BoxMessage(currentBoxId, null));
                        currentBoxId = null;
                        state = S1States.IDLE;
                    } else {// there still is some position on the list

                        currentPosition = toDoList.get(currentBoxId).get(toDoList.get(currentBoxId).size() - 1);// get next position
                        master.insertPartInPosition(currentPosition);

                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                    // most likely timeout occured -continue to wait for reply
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
    public void receiveDirectlyAdressedMsgs() {// further needs update to receive box id
        ACLMessage msg = master.receive();
        while (msg != null) {
            if (msg.getContent().contains(MessageContent.INSERT_CONE.name())) {

                String posString = msg.getContent().substring(MessageContent.INSERT_CONE.name().length());
                int pos = Integer.parseInt(posString);
                // TODO: 19.19.12    //prepeare to insert part when box arrives

            }

            msg = master.receive();
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
            currentPosition = toDoList.get(currentBoxId).get(toDoList.get(currentBoxId).size() - 1);// get first position
            master.insertPartInPosition(currentPosition);
            state = S1States.INSERTING;
            System.out.println("box stopped at station received  " + master.getName());

        }
    }

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

    public void receiveInsertionRequestMessage() {//from s2
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

    }

    ManipulatorModel makeModel(int boxId) {
        return manipulatorModel;// needs to be extended to update speeds according  if the requested box id is already on  the agents work list
    }

    void sendinfoToS2(ManipulatorModel model) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);//inform
        try {
            msg.setContentObject(model);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(sendingTopics[TopicNames.S1_TO_S2_TOPIC.ordinal()]);
        owner.sendLogMsgToGui("manip sends to topic: " + sendingTopics[TopicNames.S1_TO_S2_TOPIC.ordinal()].toString());
        owner.send(msg);
        System.out.println(getBehaviourName() + model.currentCone + " info Msg sent to s2 topic");
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
    }

    public void receiveUImessage() {//for testing insertion
        ACLMessage msg = master.receive();// takes all msgs out of list, call as lasst recieve in iteration
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
                                master.coneCountAvailable[i] = messageObj.coneCount[i];
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
