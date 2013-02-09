package cpsc470.pursuit.agent;

import java.util.Map;

import cpsc470.pursuit.environment.PursuitWorldPercept;

import aima.core.util.datastructure.XYLocation;

/**
 * Specialize AbstractOnlineGreedyPursuitWorldAgentProgram for pursuer agents by overriding getGoalLocation().
 * 
 * @author maynardp
 *
 */
public class OnlineGreedyPursuerAgentProgram extends AbstractOnlineGreedyPursuitWorldAgentProgram {

	@Override
	protected XYLocation getGoalLocation(PursuitWorldPercept percept) {
		XYLocation location = null;
		
		// TODO PROBLEM 1: FILL THIS IN
		Map<Integer, XYLocation> agentLocations =
				(Map<Integer, XYLocation>) percept.getAttribute(PursuitWorldPercept.AttributeKey.AgentsLocations);
		Object pursuedAgentIdValue = (Integer) percept.getAttribute(PursuitWorldPercept.AttributeKey.PursuedAgentId);
		return agentLocations.get(pursuedAgentIdValue);
	}

}
