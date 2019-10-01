package viabots;


import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import viabots.messageData.TopicNames;

public class ViaBotAgent extends Agent {
    TopicManagementHelper topicHelper = null;
    public AID uiTopic;

    @Override
    protected void setup() {
        super.setup();

        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            uiTopic = topicHelper.createTopic(TopicNames.GUI_TOPIC.name());

        } catch (
                ServiceException e) {
            e.printStackTrace();
        }

    }
}
