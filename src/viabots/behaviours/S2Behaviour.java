package viabots.behaviours;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import viabots.*;
import viabots.messageData.ManipulatorModel;
import viabots.messageData.MessageContent;
import viabots.messageData.S1ToS2Message;
import viabots.messageData.TopicNames;

import java.util.*;

public class S2Behaviour extends TickerBehaviour {
    ViaBotAgent owner;
    public AID conveyorMsgTopic;
    public AID s2toS1Topic;
    public AID s1toS2Topic;

    ConeType coneType;
    MessageTemplate convMsgTpl;
    MessageTemplate s1toS2Tpl;

    Map<String, S1ToS2Message> s1List = new TreeMap<>();


    public S2Behaviour(ViaBotAgent a, ConeType coneType) {
        super(a, ViaBotAgent.tickerPeriod);
        owner = a;
        this.coneType = coneType;
    }

    @Override
    protected void onTick() {

    }

    void subscribeToMessages() {
        conveyorMsgTopic = owner.createTopicForBehaviour(TopicNames.CONVEYOR_TOPIC.name());
        convMsgTpl = MessageTemplate.MatchTopic(conveyorMsgTopic);
        owner.registerBehaviourToTopic(conveyorMsgTopic);

        s2toS1Topic = owner.createTopicForBehaviour(TopicNames.S2_TO_S1_TOPIC.name());

        s1toS2Topic = owner.createTopicForBehaviour(TopicNames.S1_TO_S2_TOPIC.name());
        owner.registerBehaviourToTopic(s1toS2Topic);
        s1toS2Tpl = MessageTemplate.MatchTopic(s1toS2Topic);


    }


    void processMessages(MessageTemplate template) {// reads all available messages of corresponding template
        ACLMessage msg = owner.receive(template);
        while (msg != null) {
            if (template.equals(s1toS2Tpl)) {
                try {
                    S1ToS2Message incomingMsg = (S1ToS2Message) (msg.getContentObject());
                    s1List.put(msg.getSender().getName(), incomingMsg);

                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            } else if (template.equals(convMsgTpl)) {//further read messages from modeling behaviour, so that we have id of the box
                if (msg.getContent().contains(ConveyorAgent.boxArrived)) {// make plan for this box
                    String boxTypeString = msg.getContent().substring(ConveyorAgent.boxArrived.length() + 1);
                    BoxType type = BoxType.valueOf(boxTypeString);
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
                        sendInsertionRequestToS1(entry.getValue(), entry.getKey());

                    }

                }

            }

            msg = owner.receive(template);
        }

    }


    void sendMessageToS1(String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.setContent(content);
        msg.addReceiver(s2toS1Topic);
        owner.send(msg);

    }

    void sendInsertionRequestToS1(String agentName, int position) {
        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
        msg.setContent(MessageContent.INSERT_CONE.name());
        // msg.setContentObject(new Box());
        msg.addReceiver(new AID(agentName, false));
        owner.send(msg);

    }
}
