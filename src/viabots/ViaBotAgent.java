package viabots;


import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import viabots.messageData.TopicNames;

public class ViaBotAgent extends Agent {
    TopicManagementHelper topicHelper = null;
    public AID uiTopic;
    MessageTemplate requestTamplate;
    @Override
    protected void setup() {
        super.setup();

        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            uiTopic = topicHelper.createTopic(TopicNames.GUI_TOPIC.name());
            requestTamplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        } catch (
                ServiceException e) {
            e.printStackTrace();
        }

    }
}
