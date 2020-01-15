package viabots.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import viabots.*;
import viabots.messageData.BoxMessage;
import viabots.messageData.ConveyorOntologies;
import viabots.messageData.TopicNames;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.TreeMap;

public class S3Behaviour extends BaseTopicBasedTickerBehaviour {
    ViaBotAgent owner;
    public AID conveyorMsgTopic;
    MessageTemplate convMsgTpl;
    //TreeMap<Integer,ArrayList<BoxMessage>> jobsList;
    TreeMap<Integer, EnumSet<ConeType>> jobsListc;

    public S3Behaviour(ViaBotAgent a) {
        super(a);
        owner = a;
        jobsListc = new TreeMap<>();
        subscribeToMessages();
    }

    @Override
    protected void onTick() {
        receiveInsertersReady();
    }

    void subscribeToMessages() {
        createAndRegisterReceivingTopics(TopicNames.S2_TO_S3_TOPIC);
        createSendingTopic(TopicNames.REQUESTS_TO_MODELER);
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


    void receiveInsertersReady() {
        ACLMessage msg = owner.receive(templates[TopicNames.S2_TO_S3_TOPIC.ordinal()]);
        while (msg != null) {
            System.out.println(getBehaviourName() + " msg received");
            if (msg.getPerformative() == ACLMessage.CONFIRM) {//this should be inserters ready msg
                BoxMessage boxMessage = null;
                try {
                    boxMessage = (BoxMessage) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                System.out.println("S3 received inserters ready from " + msg.getSender().getLocalName());
                EnumSet<ConeType> unfilledList = jobsListc.get(boxMessage.boxID);
                if (unfilledList == null) {//create new job
                    unfilledList = Box.getConeTypes(boxMessage.boxType);
                    jobsListc.put(boxMessage.boxID, unfilledList);// boxmsg needs this field to bee filled
                }
// mark received cone type as ready- remove it from set
                unfilledList.remove(boxMessage.coneType);

                if (unfilledList.isEmpty()) {// all inserters are confirmed - remove this entry from joblist and send msg to convMod
                    sendInsertersReady(boxMessage.boxID);
                    jobsListc.remove(boxMessage.boxID);
                }


            }

            msg = owner.receive(templates[TopicNames.S2_TO_S3_TOPIC.ordinal()]);
        }
    }


    void sendInsertersReady(int boxID) {// receiving this msg means that conv can move past sensor 0
        ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
        msg.addReceiver(sendingTopics[TopicNames.REQUESTS_TO_MODELER.ordinal()]);
        try {
            msg.setContentObject(new BoxMessage(boxID, null, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        owner.send(msg);
        System.out.println("s3 all inserters ready sent, boxId: " + boxID);
    }

}
