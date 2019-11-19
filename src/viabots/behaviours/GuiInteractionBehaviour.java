package viabots.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import viabots.ManipulatorAgent;
import viabots.ManipulatorType;
import viabots.VSMRoles;
import viabots.ViaBotAgent;
import viabots.messageData.MessageToGUI;

import java.io.IOException;
import java.util.EnumSet;

public class GuiInteractionBehaviour extends TickerBehaviour {
    ViaBotAgent master;

    public GuiInteractionBehaviour(ViaBotAgent a) {
        super(a, ViaBotAgent.tickerPeriod);
        master = a;
        initShutdownHook();
        System.out.println(a.getClass());
    }

    void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    System.out.println("Shutting down ...");
                    //some cleaning up code...
                    sendTakeDownMessageToGui(master);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });
    }

    void sendMessageToGui(EnumSet<VSMRoles> currentRoles) {
        MessageToGUI data;
        if (currentRoles != null)
            data = new MessageToGUI(master.isConnected(), master.type, currentRoles);
        else
            data = new MessageToGUI(master.isConnected(), master.type);

        // System.out.println("is connected: "+master.communication.isConnected());
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(master.uiTopic);

        master.send(msg);
        // System.out.println(getBehaviourName()+" sent msg to gui-----------");
    }

    public static void sendTakeDownMessageToGui(ViaBotAgent master) {
        MessageToGUI data = new MessageToGUI(master.isConnected(), master.type, true);
        // System.out.println("is connected: "+master.communication.isConnected());
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(master.uiTopic);

        master.send(msg);
        System.out.println(" -------sent takedown msg to gui-----------");
    }
    @Override
    protected void onTick() {
        if (master.getClass().equals(ManipulatorAgent.class))
            sendMessageToGui(((ManipulatorAgent) master).currentRoles);
        else
            sendMessageToGui(null);

    }
}
