package viabots.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import viabots.ManipulatorAgent;
import viabots.VSMRoles;

public class RoleCheckingBehaviour extends TickerBehaviour {
    public static final int ROLE_CHECKING_INTERVAL = 5000;
    ManipulatorAgent master;

    public RoleCheckingBehaviour(ManipulatorAgent a) {
        super(a, ROLE_CHECKING_INTERVAL);
        master = a;
    }

    @Override
    protected void onTick() {
        AID[] s3 = getSubsystemAgentNames(VSMRoles.S3);
        if (s3 != null && s3.length > 0) {

        } else
            applyAs(VSMRoles.S3);
    }

    /**
     * checks with DF if S3 role is free
     *
     * @return array of agent names that carries out this role (there should not be more than one)
     */
    AID[] getSubsystemAgentNames(VSMRoles role) {

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(role.name());
        template.addServices(sd);
        AID[] agentsList = null;
        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            agentsList = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                agentsList[i] = result[i].getName();
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        return agentsList;
    }


    void applyAs(VSMRoles role) {// TODO: 19.19.11 multiple agents can register to same unique role e.g. S3

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(myAgent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(role.name());
        sd.setName(role.name());
        dfd.addServices(sd);
        try {
            DFService.register(myAgent, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        master.currentRoles.add(role);
    }


}

