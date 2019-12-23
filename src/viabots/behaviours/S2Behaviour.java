package viabots.behaviours;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
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

    Map<String, S1ToS2Message> s1List = new TreeMap<>();
    TreeMap<Integer, BoxWInserters> insertersList = new TreeMap<>();

    public S2Behaviour(ViaBotAgent a, ConeType coneType) {
        super(a);
        owner = a;
        this.coneType = coneType;
        subscribeToMessages();
    }


    @Override
    protected void onTick() {

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


    void processMessages(MessageTemplate template) {// reads all available messages of corresponding template
        ACLMessage msg = owner.receive(template);
        while (msg != null) {
            if (template.equals(templates[TopicNames.S1_TO_S2_TOPIC.ordinal()])) {
                try {
                    S1ToS2Message incomingMsg = (S1ToS2Message) (msg.getContentObject());
                    s1List.put(msg.getSender().getName(), incomingMsg);

                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            } else if (template.equals(templates[TopicNames.MODELER_NEW_BOX_TOPIC.ordinal()])) {//further read messages from modeling behaviour, so that we have id of the box
                if (msg.getOntology().contains(ConveyorOntologies.NewBoxWithID.name())) {// make plan for this box
                    BoxMessage boxMessage = null;
                    try {
                        boxMessage = (BoxMessage) msg.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    String boxTypeString = msg.getContent().substring(ConveyorAgent.boxArrived.length() + 1);
                    BoxType type = boxMessage.boxType;
                    //get numerical positions of this cone type for new box
                    ArrayList<Integer> positionList = Box.getPositions(coneType, type);
                    //get name of agent for every required position of the new box
                    FastestInsertionsPlaner planer = new FastestInsertionsPlaner();
                    Map<Integer, String> plan = planer.makePlan(positionList, coneType);
                    //send task for every manipulator according the plan

                    Iterator<Map.Entry<Integer, String>> itr = plan.entrySet().iterator();

                    //send message to every manipulator in the plan
                    while (itr.hasNext()) {
                        Map.Entry<Integer, String> entry = itr.next();
                        sendInsertionRequestToS1(entry.getValue(), entry.getKey(), boxMessage.boxID);

                    }
                    insertersList.put(boxMessage.boxID, new BoxWInserters(boxMessage.boxID, boxMessage.boxType));
                }

            } else if (template.equals(taskAssignmentToS1OntTpl)) {//should receice confirmation of assignment
                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    BoxMessage reply = null;
                    try {
                        reply = (BoxMessage) msg.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    insertersList.get(reply.boxID).setInserter(msg.getSender(), reply.positionInBox);//mark insertion request accepted
                    if (insertersList.get(reply.boxID).hasAllInserters(coneType)) {// all inserters ready, send ready msg to S3
                        sendInsertersReady(reply.boxID, coneType);
                    }
                }
            }

            msg = owner.receive(template);
        }

    }


    void sendMessageToS1(String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.setContent(content);
        msg.addReceiver(sendingTopics[TopicNames.S2_TO_S1_TOPIC.ordinal()]);
        owner.send(msg);

    }

    void sendInsertersReady(int boxID, ConeType coneType) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.addReceiver(sendingTopics[TopicNames.S2_TO_S3_TOPIC.ordinal()]);
        msg.setOntology(ConveyorOntologies.TaskAssignmentToS1.name());
        msg.setPerformative(ACLMessage.CONFIRM);
        try {
            msg.setContentObject(new BoxMessage(boxID, null, coneType));
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

        msg.addReceiver(new AID(agentName, false));
        owner.send(msg);

    }
}
