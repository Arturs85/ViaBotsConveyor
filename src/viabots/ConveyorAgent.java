package viabots;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import javafx.application.Platform;
import viabots.behaviours.ConveyorAgentBehaviour;
import viabots.messageData.MessageContent;
import viabots.messageData.MessageToGUI;
import viabots.messageData.TopicNames;

import java.io.IOException;

public class ConveyorAgent extends BaseConveyorAgent {
//    char commandStart = 'g';
//    char commandStop = 's';
//    char commandPlaceBox = 'p';
//    char commansStopAt1 = '1';
//    char commansStopAt2 = '2';
//    char commansStopAt3 = '3';
//    char commansStopAt4 = '4';
//    char commansStopAt5 = '5';
//    char commansStopAt6 = '6';

    //public AID conveyorTopic;
 //   boolean beltIsOn = false;
    TwoWaySerialComm serialComm;
   // ConveyorAgentBehaviour cab;

    @Override
    protected void setup() {
        super.setup();
     //   type = ManipulatorType.CONVEYOR;
        serialComm = new TwoWaySerialComm(this);
        try {
            serialComm.connectToFirstPort();
        } catch (Exception e) {
          //  e.printStackTrace();
        }
//        conveyorTopic = topicHelper.createTopic(TopicNames.CONVEYOR_OUT_TOPIC.name());
//        cab = new ConveyorAgentBehaviour(this);
//        addBehaviour(cab);
    }

    @Override
    protected void takeDown() {
        super.takeDown();
//        System.out.println(getLocalName() + " was taken down----");
        serialComm.disconect();

  //      System.exit(1);

    }
@Override
    public void requestStopBeltAt(int position) {
        position++;//off by one correction
        char pos = String.valueOf(position).charAt(0);
        serialComm.writeToOutputQeue(pos);

        Log.soutWTime("conv sent char to avr: "+(String.valueOf(pos)));
    }

    @Override
    void stopBelt() {
        serialComm.writeToOutputQeue(commandStop);

        beltIsOn = false;

    }
@Override
    public void startBelt() {
          serialComm.writeToOutputQeue(commandStart);
            //serialComm.out.write(commandStart);
            beltIsOn = true;
    }
@Override
    public void  placeBox() {
        serialComm.writeToOutputQeue(commandPlaceBox);

        previousHasLeft = false;
        //cab.sendConveyorMessage(ConveyorAgent.boxArrived + " " + BoxType.A.name());//for testing

    }

    /**
     * determine box type according to the model and puts it on the conveyor (put it in the queue if previous box has not left jet)
     */
    void generateBox() {

    }

    @Override
    public boolean isConnected() {
        return serialComm.isConnected;
    }

    void onSerialInput(char data) {
        switch (data) {
            case '1':
                previousHasLeft = true;
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
                sendConveyorMessage(triggerAt + data);
                break;
            case 'A':
                previousHasLeft = true;

            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
                sendConveyorMessage(stoppedAt + data);
                break;
            default:
                sendConveyorMessage("uninterpreted: " + data);
               // System.out.println("uninterp: "+String.valueOf(data));
                break;
        }

    }
//    public void sendConveyorMessage(String content) {
//        ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
//        msg.setContent(content);
//        msg.addReceiver(conveyorTopic);
//        send(msg);
//      Log.soutWTime(content);
//    }


//    void sendMessageToGui() {
//        MessageToGUI data = new MessageToGUI(serialComm.isConnected, type);//impl in GuiInteractions behaviour
//        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//        try {
//            msg.setContentObject(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        msg.addReceiver(uiTopic);
//
//        send(msg);
//        // System.out.println(getBehaviourName()+" sent msg to gui-----------");
//    }



}
