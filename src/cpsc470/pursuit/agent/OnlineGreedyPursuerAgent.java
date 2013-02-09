package cpsc470.pursuit.agent;

/**
 * Specialize AbstractOnlineGreedyPursuitWorldAgent for pursuer agents.
 * 
 * @author maynardp
 *
 */
public class OnlineGreedyPursuerAgent extends AbstractOnlineGreedyPursuitWorldAgent implements PursuerAgent {

	public OnlineGreedyPursuerAgent() {
		super(new OnlineGreedyPursuerAgentProgram());
	}

	public OnlineGreedyPursuerAgent(OnlineGreedyPursuerAgentProgram agentProgram) {
		super(agentProgram);
	}
}
