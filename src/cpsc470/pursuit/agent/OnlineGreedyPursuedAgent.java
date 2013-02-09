package cpsc470.pursuit.agent;

/**
 * Specialize AbstractOnlineGreedyPursuitWorldAgent for pursued agents.
 * 
 * @author maynardp
 *
 */
public class OnlineGreedyPursuedAgent extends AbstractOnlineGreedyPursuitWorldAgent implements PursuedAgent {

	public OnlineGreedyPursuedAgent() {
		super(new OnlineGreedyPursuedAgentProgram());
	}

	public OnlineGreedyPursuedAgent(OnlineGreedyPursuedAgentProgram agentProgram) {
		super(agentProgram);
	}

}
