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
import viabots.messageData.S2RequestMsg;
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
    ControlValueCalculator cValueCalc = new ControlValueCalculator();
    BoxGenerationModel boxGenerationModel = null;
    boolean isUsingPrediction = true;
    boolean isProducingCvalues = true;
    public S3Behaviour(ViaBotAgent a) {
        super(a);
        owner = a;
        jobsListc = new TreeMap<>();
        subscribeToMessages();
    }

    @Override
    protected void onTick() {
        receiveNewBoxArrivedMsg();
        receiveInsertersReady();
        receiveBoxGenerationModel();
        receiveDisablePrediction();
        receiveDisableControl();
    }

    void subscribeToMessages() {
        createAndRegisterReceivingTopics(TopicNames.S2_TO_S3_TOPIC);
        createAndRegisterReceivingTopics(TopicNames.MODELER_NEW_BOX_TOPIC);
        createAndRegisterReceivingTopics(TopicNames.DISABLE_PREDICTION);
        createAndRegisterReceivingTopics(TopicNames.DISABLE_CONTROL);

        createAndRegisterReceivingTopics(TopicNames.BOX_GEN_MODEL_TOPIC);

        createSendingTopic(TopicNames.REQUESTS_TO_MODELER);
        createSendingTopic(TopicNames.S3_TO_S2_TOPIC);
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

    void receiveNewBoxArrivedMsg() {
        ACLMessage msg = owner.receive(templates[TopicNames.MODELER_NEW_BOX_TOPIC.ordinal()]);
        if (msg == null) return;
        else receiveNewBoxArrivedMsg();// read all msgs of this template

        if (msg.getOntology().contains(ConveyorOntologies.NewBoxWithID.name())) {//
            BoxMessage boxMessage = null;
            try {
                boxMessage = (BoxMessage) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

//check if this msg should be post back for others  to be able to receive it
            if (owner.s2MustPostNewBoxMsg(boxMessage.boxID)) {
                owner.postMessage(msg);
            }
            // update local copy of boxGenerationModel
            boxGenerationModel.getNextFromPattern();// incr3eses counter, dont care about return value

            //update control values and send it to s2
            cValueCalc.processNewBox(boxMessage);
            double[] predictedCvals = boxGenerationModel.countAvarageCones(10);
            if (isUsingPrediction) {
                if (predictedCvals != null) {// add prediction to the cVals

                    System.out.println("predicted cVals : " + predictedCvals[0] + "  " + predictedCvals[1] + "  " + predictedCvals[2]);
                    cValueCalc.addPrediction(predictedCvals);
                }
            }
            if (isProducingCvalues)
                sendControlValuesToS2(cValueCalc.cVals);
            else
                sendControlValuesToS2(ControlValueCalculator.zeroes); //send zero as cVal to
        }

    }

    void receiveBoxGenerationModel() {
        ACLMessage msg = owner.receive(templates[TopicNames.BOX_GEN_MODEL_TOPIC.ordinal()]);
        if (msg == null) return;
        {

            try {
                boxGenerationModel = (BoxGenerationModel) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            Log.soutWTime("S3/S4 received box generation model");

        }
    }

    void sendControlValuesToS2(double[] cVals) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(sendingTopics[TopicNames.S3_TO_S2_TOPIC.ordinal()]);
        try {
            msg.setContentObject(cVals);
        } catch (IOException e) {
            e.printStackTrace();
        }
        owner.send(msg);

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


            } else if (msg.getPerformative() == ACLMessage.FAILURE) {//this should be s2 asking for resources msg
                S2RequestMsg obj = null;

                try {
                    obj = (S2RequestMsg) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
// increase cVal for asker and send new cVals
                cValueCalc.increaseAskersVal(obj.coneType);
                sendControlValuesToS2(cValueCalc.cVals);
                System.out.println("S3 sent updated cVals after S2" + obj.coneType + " request");
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


    void receiveDisablePrediction() {
        ACLMessage msg = owner.receive(templates[TopicNames.DISABLE_PREDICTION.ordinal()]);
        if (msg != null) {

            try {
                isUsingPrediction = (Boolean) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            System.out.println("---------------------S3 is using prediction: " + isUsingPrediction);
        }
    }

    void receiveDisableControl() {
        ACLMessage msg = owner.receive(templates[TopicNames.DISABLE_CONTROL.ordinal()]);
        if (msg != null) {

            try {
                isProducingCvalues = (Boolean) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            System.out.println("==================== S3 is generating cotrol: " + isProducingCvalues);
        }
    }
}
