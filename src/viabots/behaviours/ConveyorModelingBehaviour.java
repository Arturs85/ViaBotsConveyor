package viabots.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import viabots.*;
import viabots.messageData.ConvModelingMsgToUI;
import viabots.messageData.TopicNames;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConveyorModelingBehaviour extends TickerBehaviour {
    static int numberOfSensors = 4; // there will be as much queues as there is sensors

    List<LinkedList<Box>> boxQueues = new ArrayList<>(numberOfSensors);
    ManipulatorAgent owner;
    public AID conveyorMsgTopic;
    public AID modelerToGuiTopic;

    MessageTemplate convMsgTpl;


    public ConveyorModelingBehaviour(ManipulatorAgent a, long period) {
        super(a, period);
        owner = a;
        for (int i = 0; i < numberOfSensors; i++) {// initialize conveyor model - list of queues of Boxes
            boxQueues.add(new LinkedList<Box>());
        }
        //  boxQueues.get(0).add(new Box(BoxType.C));//for testing
        //  boxQueues.get(2).add(new Box(BoxType.B));//for testing
        //  boxQueues.get(2).add(new Box(BoxType.C));//for testing

        subscribeToMessages();

    }

    void subscribeToMessages() {
        conveyorMsgTopic = owner.createTopicForBehaviour(TopicNames.CONVEYOR_TOPIC.name());
        convMsgTpl = MessageTemplate.MatchTopic(conveyorMsgTopic);
        owner.registerBehaviourToTopic(conveyorMsgTopic);

        modelerToGuiTopic = owner.createTopicForBehaviour(TopicNames.MODELER_GUI.name());//for sending
    }

    void unsubscribe() {//in case behaviour would be removed in runtime


    }

    void processMessages(MessageTemplate template) {// reads all available messages of coresponding template
        ACLMessage msg = owner.receive(template);
        while (msg != null) {
            //  owner.sendLogMsgToGui(getBehaviourName() + " received conv msg: " + msg.getContent());

            if (msg.getContent().contains(ConveyorAgent.boxArrived)) {// add new bo to first queue
                String boxTypeString = msg.getContent().substring(ConveyorAgent.boxArrived.length() + 1);
                BoxType type = BoxType.valueOf(boxTypeString);
                boxQueues.get(0).add(new Box(type));

            } else if (msg.getContent().contains(ConveyorAgent.stoppedAt)) {
                char position = msg.getContent().charAt(ConveyorAgent.stoppedAt.length());
                //    owner.sendLogMsgToGui(getBehaviourName() + " received sopped at, read char: " + position);
                switch (position) {
                    case 'A':

                        toTheNext(0);
                        break;
                    case 'B':
                        toTheNext(1);
                        break;
                    case 'C':
                        toTheNext(2);
                        break;
                    case 'D':
                        toTheNext(3);
                        break;
                    case 'E':
                        toTheNext(4);
                        break;
                    case 'F':
                        toTheNext(5);

                        break;
                    default:
                        break;

                }


            }
            msg = owner.receive(template);
        }

    }

    /**
     * moves first box of queue before this sensor to next queue, or removes it if there are no more queues
     */
    void toTheNext(int sensorNumber) {
        // owner.sendLogMsgToGui(getBehaviourName() + " toTheNextCalled with sensor nr:" + sensorNumber);

        Box box = boxQueues.get(sensorNumber).removeFirst();// todo add queues size check

        if ((sensorNumber + 1) >= boxQueues.size()) {//no more queues, erease box(do nothing)

        } else {
            boxQueues.get(sensorNumber + 1).add(box);

        }

    }

    void sendConvModelMsgToGUI() {//sends whole model

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        try {
            msg.setContentObject(new ConvModelingMsgToUI(boxQueues));
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(modelerToGuiTopic);
        owner.send(msg);
        System.out.println("modeling queue msg object sent");

    }

    @Override
    protected void onTick() {
        processMessages(convMsgTpl);
        sendConvModelMsgToGUI();
    }
}
