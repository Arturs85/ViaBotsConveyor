package viabots;

import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    ContainerController cc;
    static int agentsCounter = 0;
    static String localHost = "vnpc-Precision-T1700";
    final static int CONNECTING_INTERVAL = 4000;
    static String version = "feb17";
    /**
     * arg[0] hostname of main container- needed only if this is peripherial container
     * arg[1] manipulator type name as in enum {@link ManipulatorType}
     * arg[2] agent name
     */
    public static void main(String[] args) {
//test
        System.out.println("version: "+version);
        BoxType type = BoxType.B;
        String content = ConveyorAgent.stoppedAt + type.name();
        char position = content.charAt(ConveyorAgent.stoppedAt.length());
        System.out.println("char: " + position);
        //end test
        String libPathProperty = System.getProperty("java.library.path");
        System.out.println(libPathProperty);
        try {
            localHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("hostname: " + localHost);
        Main main = new Main();

        if (args.length > 0) {
            System.out.println("main launched with arguments: ");

            for (String s : args) {
                System.out.println(s);
            }
            // presence of arguments means that this is pheripherial container
            ManipulatorType desiredType = null;
            try {
                desiredType = ManipulatorType.valueOf(args[1]);
            } catch (IllegalArgumentException iae) {
                System.out.println("unable to find requested type: " + args[1] + "...terminating");
                return;
            }
            main.cc = null;
            while (main.cc == null) {
                main.cc = main.startPeripherialContainer(args[0]);
                try {
                    Thread.sleep(CONNECTING_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String agName = null;
            if (args.length >= 3) agName = args[2];

            Integer sensorPosition = null;
            if (args.length >= 4) sensorPosition = Integer.parseInt(args[3]);

            main.createAgent(desiredType, agName, sensorPosition);

        } else {// no arguments mean that this is main container
            main.cc = main.startJade();

            main.createGUIAgent();
            main.createAgent(null);// creates vsm role agent

            //  main.createAgent(ManipulatorType.BAXTER);//for testing


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // main.createAgent(ManipulatorType.CONVEYOR);
            //main.createAgent(ManipulatorType.IRB160);//for testing

        }


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

    ContainerController startPeripherialContainer(String mainContainerHostName) {//wont be needed if agents would be started from commandline
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        // p.setParameter(Profile.GUI, "true");
        p.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");
        p.setParameter(Profile.MAIN_HOST, mainContainerHostName);

        p.setParameter(Profile.EXPORT_PORT, "1099");
        // Create a new non-main container, connecting to the default
// main container (i.e. on this host, port 1099)
        ContainerController cc = rt.createAgentContainer(p);


        return cc;
    }

    void createAgent(ManipulatorType type) {
        createAgent(type, null);
    }

    void createAgent(ManipulatorType type, String agentName) {
        createAgent(type, agentName, null);
    }
    //type = null for vsmRoles agent
    void createAgent(ManipulatorType type, String agentName, Integer sensorPosition) {
        if (cc != null) {
            // Create a new agent, a DummyAgent
// and pass it a reference to an Object
            Object reference = new Object();
            Object args[] = new Object[]{reference, type, sensorPosition};
            AgentController dummy;
            if (agentName == null) agentName = "ViaBotManipulator";
            try {
                if (type == null) {
                    dummy = cc.createNewAgent("ManagementRoleKeeper",
                            "viabots.ViaBotAgent", args);
                } else if (type.equals(ManipulatorType.CONVEYOR)) {
                    dummy = cc.createNewAgent("Conveyor",
                            "viabots.ConveyorAgent", args);
                } else
                    dummy = cc.createNewAgent(agentName + (++agentsCounter),
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


    void createConveyorAgent() {
        if (cc != null) {
            Object reference = new Object();
            Object args[] = null;//new Object[]{reference, tasks, agents, finishedTasks, initialBehaviour, this,speed,energyCons};

            try {
                AgentController dummy = cc.createNewAgent("ConveyorAgent",
                        "viabots.ConveyorAgent", args);
                dummy.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
