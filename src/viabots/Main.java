package viabots;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
    ContainerController cc;
    static int agentsCounter = 0;
    final static String mainContainerHostName = "vnpc-Precision-T1700";
    public static void main(String[] args) {
        // write your code here
        Main main = new Main();
        main.cc = main.startJade();
        main.createAgent();
        // main.createAgent();

        main.createGUIAgent();
    }


    ContainerController startJade() {
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        rt.invokeOnTermination(new Runnable() {
            @Override
            public void run() {
                System.out.println(getClass().getName() + " runtimeTermination");
            }
        });
        // Create a default profile
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        p.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");

        //p.setParameter(Profile.SERVICES,"TopicManagement");
        // Create a new non-main container, connecting to the default
// main container (i.e. on this host, port 1099)
        ContainerController cc = rt.createMainContainer(p);
        return cc;
    }

    ContainerController startPeripherialContainer() {//wont be needed if agents would be started from commandline
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        // p.setParameter(Profile.GUI, "true");
        p.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");
        p.setParameter(Profile.EXPORT_HOST, mainContainerHostName);
        p.setParameter(Profile.EXPORT_PORT, "1099");
        //p.setParameter(Profile.SERVICES,"TopicManagement");
        // Create a new non-main container, connecting to the default
// main container (i.e. on this host, port 1099)
        ContainerController cc = rt.createAgentContainer(p);
        return cc;
    }

    void createAgent() {
        if (cc != null) {
            // Create a new agent, a DummyAgent
// and pass it a reference to an Object
            Object reference = new Object();
            Object args[] = null;//new Object[]{reference, tasks, agents, finishedTasks, initialBehaviour, this,speed,energyCons};

            try {
                AgentController dummy = cc.createNewAgent("ViaBotConv " + (++agentsCounter),
                        "viabots.ManipulatorAgent", args);
// Fire up the agent
                dummy.start();

                //agents.add(new AgentInfo(dummy.getName(), simTime, dummy, speed));//[]a,b,c,s2,s3,s4
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // controller.guiAgent.sendMessageUI(isRunning,false);
    }

    void createGUIAgent() {
        if (cc != null) {
            Object reference = new Object();
            Object args[] = null;//new Object[]{reference, tasks, agents, finishedTasks, initialBehaviour, this,speed,energyCons};

            try {
                AgentController dummy = cc.createNewAgent("ConveyorGUIAgent",
                        "GUI.GUIAgent", args);
                dummy.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
