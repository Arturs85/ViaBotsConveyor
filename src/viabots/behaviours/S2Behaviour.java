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
    static int infoWaitingTimeout = 3000;// ms
    static int grantWaitingTimeout = 4000;// ms

    int waitingCounter = 0;
    BoxMessage currentBoxMessage = null;// message of box for which inserters are currently requested or planned dont receive other messages of this type until plan for current is made
    double latestCval = 0;
    double[] latestCVals;
    double latestS1Count = 0;

    public S2Behaviour(ViaBotAgent a, ConeType coneType) {
        super(a);
        owner = a;
        this.coneType = coneType;
        subscribeToMessages();
    }
//todo large reply waiting times

    @Override
    protected void onTick() {
        //     System.out.println(owner.getLocalName() + " has " + owner.getCurQueueSize() + " msgs ," + getBehaviourName());
// look through all new messages
        processMessages2(templates[TopicNames.S1_TO_S2_TOPIC.ordinal()]);
        processMessages2(templates[TopicNames.MODELER_NEW_BOX_TOPIC.ordinal()]);
        boolean hasNewControlValues = receiveControlValue();// right moment?

        switch (state) {
            case IDLE:
                receiveS2Request();// receive these msgs only when not planing own inserters
                if (hasNewControlValues)
                    sendWorkerRequest();// ask for workers, because control values may be changed and cone availability may been changed
                break;
            case WAITING_S1_INFO:

                waitingCounter--;
                if (waitingCounter <= 0) {// waiting time is over, try to make plan using received manipulatorModels
                    latestS1Count = s1List.size();
                    boolean hasPlan = makePlan(currentBoxMessage);

                    if (hasPlan) {//plan has been made and requests according to the plan has been sent
                        enterState(S2States.WAITING_S1_CONFIRM_PREPEARED);
                    } else {// plan could not be made- start again with requests
                        sendWorkerRequest();// request workers if plan cant be made
                        enterState(S2States.WAITING_S2_REPLY);
                    }

                }
                break;
            case REFRESH_S1_LIST:
                waitingCounter--;
                if (waitingCounter <= 0) {// waiting time is over, try to make plan using received manipulatorModels
                    latestS1Count = s1List.size();
                    enterState(S2States.IDLE);
                }

                break;
            case WAITING_S1_CONFIRM_PREPEARED:
                if (insertersList.get(currentBoxMessage.boxID).hasAllInserters(coneType)) {// all inserters ready, send ready msg to S3
                    System.out.println(coneType + " has all inserters for box id: " + currentBoxMessage.boxID);
                    sendInsertersReady(currentBoxMessage.boxID, currentBoxMessage.boxType, coneType);
                    enterState(S2States.IDLE);
                }
                break;
            case WAITING_S2_REPLY://wait for reply to request of worker, do not send new requests until reply is received
                waitingCounter--;
                receiveReplyToRequest();
                if (waitingCounter <= 0) {// waiting time is over
// this means that no positive replys were received, no use to try to query own workers again, send request to s3
                    // enterState(S2States.WAITING_S1_INFO);
//send again request to s2, because previous one may be skipped due to the unsuitable state of receiver

                    // sendWorkerRequest();// request workers again
                    // enterState(S2States.WAITING_S2_REPLY);
                    sendResourceRequestToS3();
                    enterState(S2States.WAITING_S1_INFO);// query own manipulators again, maybe cones has been added
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
            case REFRESH_S1_LIST:
                s1List.clear();
                currentBoxMessage.coneType = coneType;// sets own cone type, so receivers can only select messages with appropriate cone type
                sendInfoRequestMessagesToS1(currentBoxMessage);
                waitingCounter = infoWaitingTimeout / ViaBotAgent.tickerPeriod;
                state = S2States.REFRESH_S1_LIST;


                break;
            case WAITING_S1_CONFIRM_PREPEARED:

                state = S2States.WAITING_S1_CONFIRM_PREPEARED;
                break;
            case WAITING_S2_REPLY:
                waitingCounter = grantWaitingTimeout / ViaBotAgent.tickerPeriod;
                state = S2States.WAITING_S2_REPLY;
                break;
            default:
                break;
        }

    }

    void subscribeToMessages() {
        createAndRegisterReceivingTopics(TopicNames.MODELER_NEW_BOX_TOPIC);
        createAndRegisterReceivingTopics(TopicNames.S1_TO_S2_TOPIC);
        createAndRegisterReceivingTopics(TopicNames.S3_TO_S2_TOPIC);
        createAndRegisterReceivingTopics(TopicNames.S2_TO_S2_TOPIC);

        createSendingTopic(TopicNames.S2_TO_S1_TOPIC);
        createSendingTopic(TopicNames.S2_TO_S3_TOPIC);
        createSendingTopic(TopicNames.S2_TO_S2_TOPIC);
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


    void processMessages2(MessageTemplate template) {// reads all available messages of corresponding template
        ACLMessage msg = owner.receive(template);
        if (msg != null) {
            processMessages2(template);
        }//start processing from last msg, to be able to post back unrelated messages
        if (msg != null) {
            // System.out.println(owner.getLocalName() + " received msg with template: " + template.toString());
            if (template.equals(templates[TopicNames.S1_TO_S2_TOPIC.ordinal()])) {
                if (msg.getPerformative() == ACLMessage.INFORM) {// this should be reply to info request
                    try {
                        ManipulatorModel incomingMsg = (ManipulatorModel) (msg.getContentObject());
                        if (!incomingMsg.currentCone.equals(coneType)) {//this msg is for other s2 type
                            owner.postMessage(msg);
                            //return;
                        } else {
                            s1List.put(msg.getSender().getName(), incomingMsg);
                            System.out.println("s2" + coneType + " received model from " + msg.getSender().getLocalName() + incomingMsg.toString());
                        }
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

                        } else {
                            System.out.println("s2" + coneType + " received agree from " + msg.getSender().getLocalName());

                            if (insertersList.get(reply.boxID) != null) {
                                insertersList.get(reply.boxID).setInserter(msg.getSender(), reply.positionInBox);//mark insertion request accepted//!!!NULL POINTER ERROR
                            } else {
                                System.out.println(getBehaviourName() + " 254---NULL POINTER WARNING____");
                            }
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

    boolean receiveControlValue() {// will all s2 of one agent receive this msg?
        ACLMessage msg = owner.receive(templates[TopicNames.S3_TO_S2_TOPIC.ordinal()]);
        if (msg == null) return false;
        double[] cVals = null;
        try {
            cVals = (double[]) msg.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        latestCval = cVals[coneType.ordinal()];
        latestCVals = cVals;
        if (owner.s2MustPostMsg(msg, TopicNames.S3_TO_S2_TOPIC))
            owner.postMessage(msg);
        System.out.println(getBehaviourName() + coneType + " received control values from S3, own value: " + latestCval);
        return true;
    }

    void receiveS2Request() {// all s2 on every agent should receive this
        ACLMessage msg = owner.receive(templates[TopicNames.S2_TO_S2_TOPIC.ordinal()]);
        if (msg == null) return;
        System.out.println("S2" + coneType + " Received s2 request for worker from S2, msg hash: " + msg.hashCode());

        if (msg.getOntology().equals(ConveyorOntologies.S1Request.name()) && (msg.getPerformative() == ACLMessage.REQUEST)) {
            S2RequestMsg requestMsg = null;
            try {
                requestMsg = (S2RequestMsg) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            if (owner.s2MustPostMsg(msg, TopicNames.S2_TO_S2_TOPIC))
                owner.postMessage(msg);
            if (requestMsg.coneType.equals(coneType)) return;// it is message from itself
            if (state != S2States.IDLE) return;// process message only when not planing own inserters
            System.out.println("S2" + coneType + " Received s2 request for worker from S2" + requestMsg.coneType);
            String manip = findLeastValuedManipulator(requestMsg.coneType);

            // compare own cVal with requesting s2
            if (requestMsg.cVal > (getLatestCval() + 1) && manip != null) {
                // find manipulator to give to requester

                enterState(S2States.REFRESH_S1_LIST);//update workers list after giveaway

                sendChangeConeType(manip, requestMsg.coneType);
                //send reply to requester
                sendReplyToS2Request(true, requestMsg.coneType);
                System.out.println("S2" + coneType + " sending change to " + requestMsg.coneType);
            } else {
                sendReplyToS2Request(false, requestMsg.coneType);

            }
            System.out.println("initiator: " + requestMsg.coneType + " " + requestMsg.cVal + " receiver: " + coneType + " " + getLatestCval());

        } else// different ontology, or reply to request-> those msgs will be processed separately
            owner.postMessage(msg);

    }

    void receiveReplyToRequest() {
        ACLMessage msg = owner.receive(templates[TopicNames.S2_TO_S2_TOPIC.ordinal()]);
        if (msg == null) return;
        if (msg.getOntology().equals(ConveyorOntologies.S1Request.name())) {
            S2RequestMsg replyMsg = null;
            try {
                replyMsg = (S2RequestMsg) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            if (msg.getPerformative() == ACLMessage.AGREE && replyMsg.coneType.equals(coneType)) {
                System.out.println("S2" + coneType + " Received POSITIVE reply to request for worker from S2");
                //try to send requests to workers
                enterState(S2States.WAITING_S1_INFO);
            } else if (msg.getPerformative() == ACLMessage.REFUSE && replyMsg.coneType.equals(coneType)) {
                System.out.println("S2" + coneType + " Received NEGATIVE reply to request for worker from S2");

            } else owner.postMessage(msg);//other receiver

        }
    }

    String findLeastValuedManipulator(ConeType requesterType) {//returns manipulator agent name
        int speedOfLeastSoFar = 0;
        String nameOfLeastSoFar = null;
        for (Map.Entry<String, ManipulatorModel> entry : s1List.entrySet()) {//consider only manipulators with cones of requester type
            if (entry.getValue().timesForNextInsertion[coneType.ordinal()] > speedOfLeastSoFar && entry.getValue().conesAvailable[requesterType.ordinal()] > 0) {
                speedOfLeastSoFar = entry.getValue().timesForNextInsertion[coneType.ordinal()];
                nameOfLeastSoFar = entry.getKey();
            }
        }
        return nameOfLeastSoFar;
    }

    void sendResourceRequestToS3() {
        ACLMessage msg = new ACLMessage(ACLMessage.FAILURE);
        try {
            msg.setContentObject(new S2RequestMsg(coneType, getLatestCval()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.S2_TO_S3_TOPIC.ordinal()]);
        owner.send(msg);
        //enterState(S2States.WAITING_S2_REPLY);
        System.out.println(getBehaviourName() + coneType + " sending failure to S3");
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

    //for informing own s1 to change its cone type
    void sendChangeConeType(String manipAgent, ConeType coneType) {// to s1
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.addReceiver(new AID(manipAgent, true));
        msg.setOntology(ConveyorOntologies.ChangeConeType.name());
        try {
            msg.setContentObject(new S1ToS2Message(null, coneType));
        } catch (IOException e) {
            e.printStackTrace();
        }
        owner.send(msg);
    }

    void sendReplyToS2Request(Boolean granted, ConeType receiverConeType) {// to s1
        ACLMessage msg;
        if (granted)
            msg = new ACLMessage(ACLMessage.AGREE);
        else
            msg = new ACLMessage(ACLMessage.REFUSE);

        msg.addReceiver(sendingTopics[TopicNames.S2_TO_S2_TOPIC.ordinal()]);
        msg.setOntology(ConveyorOntologies.S1Request.name());
        try {
            msg.setContentObject(new S2RequestMsg(receiverConeType, 0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        owner.send(msg);
    }

    void sendWorkerRequest() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setOntology(ConveyorOntologies.S1Request.name());
        try {
            msg.setContentObject(new S2RequestMsg(coneType, getLatestCval()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.S2_TO_S2_TOPIC.ordinal()]);
        owner.send(msg);
        //enterState(S2States.WAITING_S2_REPLY);
        System.out.println(getBehaviourName() + coneType + " sending worker request to other S2's");

    }

    double getLatestCval() {
        return latestCval - latestS1Count;
    }
}
