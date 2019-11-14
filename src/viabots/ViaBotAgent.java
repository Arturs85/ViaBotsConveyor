package viabots;


import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import viabots.behaviours.GuiInteractionBehaviour;
import viabots.messageData.TopicNames;

public class ViaBotAgent extends Agent {
    TopicManagementHelper topicHelper = null;
    public AID uiTopic;
    public MessageTemplate requestTamplate;
    public static final int tickerPeriod = 1000;//ms
    public ManipulatorType type;

    public boolean isConnected() {
        return false;
    }
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

        if (getArguments() != null)
            type = (ManipulatorType) getArguments()[1];

        addBehaviour(new GuiInteractionBehaviour(this));
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        GuiInteractionBehaviour.sendTakeDownMessageToGui(this);
    }

    public void receiveUImessage() {

    }
}
