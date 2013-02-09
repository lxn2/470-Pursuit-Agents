package cpsc470.pursuit.agent;

/**
 * Extends the OnlineGreedyPursuedAgent so as to provide the pursued agent with a <em>tabu list</em>,
 * i.e., a list that maintains the most recently visited N locations so as to avoid local optima.
 * 
 * @author maynardp
 *
 */
public class OnlineGreedyTabuPursuedAgent extends OnlineGreedyPursuedAgent {

	public OnlineGreedyTabuPursuedAgent() {
		super(new OnlineGreedyTabuPursuedAgentProgram());
	}
	
}
