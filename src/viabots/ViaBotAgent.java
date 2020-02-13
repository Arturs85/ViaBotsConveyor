package viabots;


import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import viabots.behaviours.ConeType;
import viabots.behaviours.GuiInteractionBehaviour;
import viabots.behaviours.S2Behaviour;
import viabots.behaviours.S3Behaviour;
import viabots.messageData.ConveyorOntologies;
import viabots.messageData.TopicNames;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.TreeMap;

public class ViaBotAgent extends Agent {
    TopicManagementHelper topicHelper = null;
    public AID uiTopic;
    public AID logTopic;

    // public MessageTemplate requestTamplate;
    // public MessageTemplate informTamplate;

    public static final int tickerPeriod = 500;//ms
    public ManipulatorType type;
    public EnumSet<VSMRoles> currentRoles;
    int sNewBoxArrivedMsgSubscriberRolesCount = 0;//shows how many different s behaviours is added to this agent
    TreeMap<Integer, Integer> unannouncedBoxes = new TreeMap<>(); //key boxid, value - number of s roles that has processed this newbox msg
    TreeMap<ACLMessage, Integer> unreadMsgs = new TreeMap(Comparator.comparingInt(Object::hashCode));// shows how many times msg has been read
    public int[] subscribersForTopic = new int[TopicNames.values().length];
    public MessageTemplate uiCommandTpl;

    public boolean s2MustPostNewBoxMsg(int boxId) {// called after receiving newBox msg, shows whether to post back to queue this msg
        if (unannouncedBoxes.get(boxId) == null) {
            unannouncedBoxes.put(boxId, 1);//mark first reception
        } else {//update reads count
            unannouncedBoxes.put(boxId, unannouncedBoxes.get(boxId) + 1);
        }
        //check whether all s2 has read this newbox msg
        if (unannouncedBoxes.get(boxId) >= sNewBoxArrivedMsgSubscriberRolesCount) {
            unannouncedBoxes.remove(boxId);
            return false;
        } else
            return true;
    }

    public boolean s2MustPostMsg(ACLMessage msg, TopicNames topicName) {// called after receiving newBox msg, shows whether to post back to queue this msg
        if (unreadMsgs.get(msg) == null) {
            unreadMsgs.put(msg, 1);//mark first reception
        } else {//update reads count
            unreadMsgs.put(msg, unreadMsgs.get(msg) + 1);
        }
        //check whether all s2 has read this newbox msg
        if (unreadMsgs.get(msg) >= subscribersForTopic[topicName.ordinal()]) {
            unreadMsgs.remove(msg);
            return false;
        } else
            return true;
    }

    public boolean isConnected() {
        return false;
    }//remove this?


    @Override
    protected void setup() {
        super.setup();

        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            uiTopic = topicHelper.createTopic(TopicNames.GUI_TOPIC.name());
            //     requestTamplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            //    informTamplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            logTopic = topicHelper.createTopic(TopicNames.LOG_TOPIC.name());

        } catch (
                ServiceException e) {
            e.printStackTrace();
        }

        if (getArguments() != null)
            type = (ManipulatorType) getArguments()[1];

        if (type == null) {// this is vsm role agent
            currentRoles = EnumSet.noneOf(VSMRoles.class);

            addVsmRole(VSMRoles.S2_A);
            addVsmRole(VSMRoles.S2_B);
            addVsmRole(VSMRoles.S3);


            type = ManipulatorType.UNKNOWN;
        }

        addBehaviour(new GuiInteractionBehaviour(this));
        uiCommandTpl = MessageTemplate.MatchOntology(ConveyorOntologies.GuiCommands.name());
    }

    void addVsmRole(VSMRoles vsmRole) {// duplicate of RoleCheckingBehaviour function, temporary
        currentRoles.add(vsmRole);

        switch (vsmRole) {
            case S3:
                addBehaviour(new S3Behaviour(this));
                sNewBoxArrivedMsgSubscriberRolesCount++;
                break;
            case S2_A:
                addBehaviour(new S2Behaviour(this, ConeType.A));
                sNewBoxArrivedMsgSubscriberRolesCount++;
                break;
            case S2_B:
                addBehaviour(new S2Behaviour(this, ConeType.B));
                sNewBoxArrivedMsgSubscriberRolesCount++;

                break;
        }
    }
    @Override
    protected void takeDown() {
        super.takeDown();
        GuiInteractionBehaviour.sendTakeDownMessageToGui(this);

    }

    public AID createTopicForBehaviour(String name) {
        if (topicHelper == null) return null;

        return topicHelper.createTopic(name);
    }


    public void registerBehaviourToTopic(AID topic) {
        if (topicHelper == null) return;

        try {
            topicHelper.register(topic);
        } catch (ServiceException e) {
            e.printStackTrace(); // what to do here?
        }
    }

    public void sendLogMsgToGui(String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(content);
        msg.addReceiver(logTopic);
        send(msg);
        //  System.out.println(content);
    }
    public void receiveUImessage() {

    }
}
