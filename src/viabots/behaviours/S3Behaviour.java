package viabots.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import viabots.*;
import viabots.messageData.TopicNames;

public class S3Behaviour extends BaseTopicBasedTickerBehaviour {
    ViaBotAgent owner;
    public AID conveyorMsgTopic;
    MessageTemplate convMsgTpl;

    public S3Behaviour(ViaBotAgent a) {
        super(a);
        owner = a;
    }

    @Override
    protected void onTick() {

    }

    void subscribeToMessages() {
        createAndRegisterReceivingTopics(TopicNames.CONVEYOR_TOPIC);
//        conveyorMsgTopic = owner.createTopicForBehaviour(TopicNames.CONVEYOR_TOPIC.name());
//        convMsgTpl = MessageTemplate.MatchTopic(conveyorMsgTopic);
//        owner.registerBehaviourToTopic(conveyorMsgTopic);

    }

    void processMessages(MessageTemplate template) {// reads all available messages of coresponding template
        ACLMessage msg = owner.receive(template);
        while (msg != null) {

            msg = owner.receive(template);
        }

    }


}
