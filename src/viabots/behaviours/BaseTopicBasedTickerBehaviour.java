package viabots.behaviours;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.MessageTemplate;
import viabots.ViaBotAgent;
import viabots.messageData.TopicNames;


public abstract class BaseTopicBasedTickerBehaviour extends TickerBehaviour {
    ViaBotAgent owner;
    public AID[] sendingTopics;
    public AID[] receivingTopics;
    public MessageTemplate[] templates;// for receiving  messages  of particular topic
    long prevTickStartTime = 0;
    void createSendingTopic(TopicNames topicName) {
        sendingTopics[topicName.ordinal()] = owner.createTopicForBehaviour(topicName.name());
    }


    void createAndRegisterReceivingTopics(TopicNames topicName) {
        receivingTopics[topicName.ordinal()] = owner.createTopicForBehaviour(topicName.name());
        owner.registerBehaviourToTopic(receivingTopics[topicName.ordinal()]);
        templates[topicName.ordinal()] = MessageTemplate.MatchTopic(receivingTopics[topicName.ordinal()]);
        owner.subscribersForTopic[topicName.ordinal()]++;// can bee overfilled if this method is called repeatedly
    }


    public BaseTopicBasedTickerBehaviour(ViaBotAgent a) {

        super(a, ViaBotAgent.tickerPeriod);
        owner = a;
        sendingTopics = new AID[TopicNames.values().length];
        receivingTopics = new AID[TopicNames.values().length];
        templates = new MessageTemplate[TopicNames.values().length];
    }


    int dtOtputCounter = 0;

    @Override
    protected void onTick() {
        long time = System.currentTimeMillis();
        long dt = time - prevTickStartTime;
        prevTickStartTime = time;
        dtOtputCounter++;
        if (dtOtputCounter % 10 == 0) {
            dtOtputCounter = 0;
          //  System.out.println("----//// ticker actual period: " + dt);
        }
    }
}
