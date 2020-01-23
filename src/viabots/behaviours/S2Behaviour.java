package viabots.behaviours;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import viabots.*;
import viabots.messageData.*;

import java.io.IOException;
import java.util.*;

public class S2Behaviour extends BaseTopicBasedTickerBehaviour {
    ViaBotAgent owner;
    public AID conveyorMsgTopic;
    public AID s2toS1Topic;
    public AID s1toS2Topic;

    ConeType coneType;
    MessageTemplate convMsgTpl;
    MessageTemplate s1toS2Tpl;
    MessageTemplate taskAssignmentToS1OntTpl = MessageTemplate.MatchOntology(ConveyorOntologies.S1TaskConfirmation.name());

    Map<String, ManipulatorModel> s1List = new TreeMap<>();
    TreeMap<Integer, BoxWInserters> insertersList = new TreeMap<>();
    S2States state = S2States.IDLE;
    static int infoWaitingTimeout = 6000;// ms
    int waitingCounter = 0;
    BoxMessage currentBoxMessage = null;// message of box for which inserters are currently requested or planned dont receive other messages of this type until plan for current is made

    public S2Behaviour(ViaBotAgent a, ConeType coneType) {
        super(a);
        owner = a;
        this.coneType = coneType;
        subscribeToMessages();
    }


    @Override
    protected void onTick() {
        //     System.out.println(owner.getLocalName() + " has " + owner.getCurQueueSize() + " msgs ," + getBehaviourName());
// look through all new messages
        processMessages2(templates[TopicNames.S1_TO_S2_TOPIC.ordinal()]);
        processMessages2(templates[TopicNames.MODELER_NEW_BOX_TOPIC.ordinal()]);

        switch (state) {
            case IDLE:
                break;
            case WAITING_S1_INFO:

                waitingCounter--;
                if (waitingCounter <= 0) {// waiting time is over, try to make plan using received manipulatorModels
                    boolean hasPlan = makePlan(currentBoxMessage);

                    if (hasPlan) {//plan has been made and requests according to the plan has been sent
                        enterState(S2States.WAITING_S1_CONFIRM_PREPEARED);
                    } else {// plan could not be made- start again with requests
                        enterState(S2States.WAITING_S1_INFO);
                    }

                }
                break;
            case WAITING_S1_CONFIRM_PREPEARED:
                if (insertersList.get(currentBoxMessage.boxID).hasAllInserters(coneType)) {// all inserters ready, send ready msg to S3
                    System.out.println(coneType + " has all inserters for box id: " + currentBoxMessage.boxID);
                    sendInsertersReady(currentBoxMessage.boxID, currentBoxMessage.boxType, coneType);
                    enterState(S2States.IDLE);
                }
                break;

            default:
                break;
        }

    }

    void enterState(S2States nextState) {
        switch (nextState) {
            case IDLE:

                state = S2States.IDLE;
                break;
            case WAITING_S1_INFO:

                s1List.clear();
                currentBoxMessage.coneType = coneType;// sets own cone type, so receivers can only select messages with appropriate cone type
                sendInfoRequestMessagesToS1(currentBoxMessage);
                waitingCounter = infoWaitingTimeout / ViaBotAgent.tickerPeriod;
                state = S2States.WAITING_S1_INFO;
                break;
            case WAITING_S1_CONFIRM_PREPEARED:

                state = S2States.WAITING_S1_CONFIRM_PREPEARED;
                break;

            default:
                break;
        }

    }

    void subscribeToMessages() {
        createAndRegisterReceivingTopics(TopicNames.MODELER_NEW_BOX_TOPIC);
        createAndRegisterReceivingTopics(TopicNames.S1_TO_S2_TOPIC);
        createSendingTopic(TopicNames.S2_TO_S1_TOPIC);
        createSendingTopic(TopicNames.S2_TO_S3_TOPIC);

//       conveyorMsgTopic = owner.createTopicForBehaviour(TopicNames.CONVEYOR_TOPIC.name());
//        convMsgTpl = MessageTemplate.MatchTopic(conveyorMsgTopic);
//        owner.registerBehaviourToTopic(conveyorMsgTopic);
//
//        s2toS1Topic = owner.createTopicForBehaviour(TopicNames.S2_TO_S1_TOPIC.name());
//
//        s1toS2Topic = owner.createTopicForBehaviour(TopicNames.S1_TO_S2_TOPIC.name());
//        owner.registerBehaviourToTopic(s1toS2Topic);
//        s1toS2Tpl = MessageTemplate.MatchTopic(s1toS2Topic);


    }

    /**
     * @return true if plan has been made
     */
    boolean makePlan(BoxMessage boxMessage) {
        BoxType type = boxMessage.boxType;
        //get numerical positions of this cone type for new box
        ArrayList<Integer> positionList = Box.getPositions(coneType, type);
        //get name of agent for every required position of the new box
        FastestInsertionsPlaner planer = new FastestInsertionsPlaner(s1List);
        Map<Integer, String> plan = planer.makePlan(positionList, coneType);

        if (plan == null) return false;
        //send task for every manipulator according the plan

        Iterator<Map.Entry<Integer, String>> itr = plan.entrySet().iterator();

        //send message to every manipulator in the plan
        while (itr.hasNext()) {
            Map.Entry<Integer, String> entry = itr.next();
            sendInsertionRequestToS1(entry.getValue(), entry.getKey(), boxMessage.boxID);

        }
        insertersList.put(boxMessage.boxID, new BoxWInserters(boxMessage.boxID, boxMessage.boxType));
        return true;
    }

//    void processMessages(MessageTemplate template) {// reads all available messages of corresponding template
//        ACLMessage msg = owner.receive(template);
//        while (msg != null) {
//            System.out.println(owner.getLocalName()+" received msg with template: "+ template.toString());
//            if (template.equals(templates[TopicNames.S1_TO_S2_TOPIC.ordinal()])) {
//                if (msg.getPerformative() == ACLMessage.INFORM) {// this should be reply to info request
//                    try {
//                        ManipulatorModel incomingMsg = (ManipulatorModel) (msg.getContentObject());
//                        if(!incomingMsg.currentCone.equals(coneType)){//this msg is for other s2 type
//                            owner.postMessage(msg);
//                            return;
//                        }
//                        s1List.put(msg.getSender().getName(), incomingMsg);
//                        System.out.println("s2" +coneType+" received model from "+msg.getSender().getLocalName());
//                    } catch (UnreadableException e) {
//                        e.printStackTrace();
//                    }
//                } else// this should be ready confirmation from s1
//                    //should receice confirmation of assignment
//                    if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
//                        BoxMessage reply = null;
//                        try {
//                            reply = (BoxMessage) msg.getContentObject();
//                        } catch (UnreadableException e) {
//                            e.printStackTrace();
//                        }
//                        if(!reply.coneType.equals(coneType)){//this msg is for other s2 type
//                            owner.postMessage(msg);
//                            System.out.println("s2" +coneType+" received agree s1 "+reply.coneType+", posting back");
//
//                            continue;// continue untill end of msg queue
//                        }
//                        System.out.println("s2" +coneType+" received agree from "+msg.getSender().getLocalName());
//
//                        insertersList.get(reply.boxID).setInserter(msg.getSender(), reply.positionInBox);//mark insertion request accepted
//
//
//                    }
//
//            } else if (template.equals(templates[TopicNames.MODELER_NEW_BOX_TOPIC.ordinal()])) {
//                if (msg.getOntology().contains(ConveyorOntologies.NewBoxWithID.name())) {// make plan for this box
//                    BoxMessage boxMessage = null;
//                    try {
//                        boxMessage = (BoxMessage) msg.getContentObject();
//                    } catch (UnreadableException e) {
//                        e.printStackTrace();
//                    }
//                    if(currentBoxMessage!=null && currentBoxMessage.boxID==boxMessage.boxID){// this behaviour already receved this msg, put it back
//                        owner.postMessage(msg);
//                        return;
//                    }
//                    currentBoxMessage=boxMessage;
//                    enterState(S2States.WAITING_S1_INFO);// sends info requests and waits time
//                    String boxTypeString = msg.getContent().substring(ConveyorAgent.boxArrived.length() + 1);
////check if this msg should be post back for other s2 to be able to receive it
//                    if(owner.s2MustPostNewBoxMsg(currentBoxMessage.boxID)){
//                        owner.postMessage(msg);
//                    }
//                }
//
//            }
//
//            msg = owner.receive(template);
//        }
//
//    }


    void processMessages2(MessageTemplate template) {// reads all available messages of corresponding template
        ACLMessage msg = owner.receive(template);
        if (msg != null) {
            processMessages2(template);
        }//start processing from last msg, to be able to post back unrelated messages
        if (msg != null) {
            System.out.println(owner.getLocalName() + " received msg with template: " + template.toString());
            if (template.equals(templates[TopicNames.S1_TO_S2_TOPIC.ordinal()])) {
                if (msg.getPerformative() == ACLMessage.INFORM) {// this should be reply to info request
                    try {
                        ManipulatorModel incomingMsg = (ManipulatorModel) (msg.getContentObject());
                        if (!incomingMsg.currentCone.equals(coneType)) {//this msg is for other s2 type
                            owner.postMessage(msg);
                            return;
                        }
                        s1List.put(msg.getSender().getName(), incomingMsg);
                        System.out.println("s2" + coneType + " received model from " + msg.getSender().getLocalName());
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                } else// this should be ready confirmation from s1
                    //should receice confirmation of assignment
                    if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        BoxMessage reply = null;
                        try {
                            reply = (BoxMessage) msg.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        if (!reply.coneType.equals(coneType)) {//this msg is for other s2 type
                            owner.postMessage(msg);
                            System.out.println("s2" + coneType + " received agree s1 " + reply.coneType + ", posting back");

                        }
                        System.out.println("s2" + coneType + " received agree from " + msg.getSender().getLocalName());

                        if (insertersList.get(reply.boxID) != null) {
                            insertersList.get(reply.boxID).setInserter(msg.getSender(), reply.positionInBox);//mark insertion request accepted//!!!NULL POINTER ERROR
                        } else {
                            System.out.println(getBehaviourName() + " 254---NULL POINTER WARNING____");
                        }

                    }

            } else if (template.equals(templates[TopicNames.MODELER_NEW_BOX_TOPIC.ordinal()])) {
                if (msg.getOntology().contains(ConveyorOntologies.NewBoxWithID.name())) {// make plan for this box
                    BoxMessage boxMessage = null;
                    try {
                        boxMessage = (BoxMessage) msg.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    if (currentBoxMessage != null && currentBoxMessage.boxID == boxMessage.boxID) {// this behaviour already receved this msg, put it back
                        owner.postMessage(msg);
                        return;
                    }
                    currentBoxMessage = boxMessage;
                    enterState(S2States.WAITING_S1_INFO);// sends info requests and waits time
                    String boxTypeString = msg.getContent().substring(ConveyorAgent.boxArrived.length() + 1);
//check if this msg should be post back for other s2 to be able to receive it
                    if (owner.s2MustPostNewBoxMsg(currentBoxMessage.boxID)) {
                        owner.postMessage(msg);
                    }
                }

            }

            // msg = owner.receive(template);
        } else return;

    }



    /**
     * forward boxMessage to coresponding s1 so they can reply with speeds for that particular box
     */
    void sendInfoRequestMessagesToS1(BoxMessage boxMessage) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.setOntology(ConveyorOntologies.NewBoxWithID.name());
        try {
            msg.setContentObject(boxMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.S2_TO_S1_TOPIC.ordinal()]);// should this be sent to all s1 or just ones with same cone type as sender?
        owner.send(msg);
        System.out.println(owner.getLocalName() + " sent info request to s1 topic");
    }

    void sendInsertersReady(int boxID, BoxType boxType, ConeType coneType) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.addReceiver(sendingTopics[TopicNames.S2_TO_S3_TOPIC.ordinal()]);
        msg.setOntology(ConveyorOntologies.TaskAssignmentToS1.name());
        msg.setPerformative(ACLMessage.CONFIRM);
        try {
            msg.setContentObject(new BoxMessage(boxID, boxType, coneType));
        } catch (IOException e) {
            e.printStackTrace();
        }
        owner.send(msg);
    }

    void sendInsertionRequestToS1(String agentName, int position, int boxID) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.setOntology(ConveyorOntologies.TaskAssignmentToS1.name());
        try {
            msg.setContentObject(new BoxMessage(boxID, null, position));
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(new AID(agentName, true));
        owner.send(msg);
        System.out.println(getBehaviourName() + coneType + " sending insertion request to s1: " + agentName + " position " + position);
    }
}
