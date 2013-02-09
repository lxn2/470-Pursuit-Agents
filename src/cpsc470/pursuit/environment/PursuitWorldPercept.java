package cpsc470.pursuit.environment;

import java.util.List;
import java.util.Map;

import aima.core.agent.impl.DynamicPercept;
import aima.core.util.datastructure.XYLocation;

public class PursuitWorldPercept extends DynamicPercept {

	/**
	 * Keys for attributes in percepts for fully-observable pursuit environment.
	 * 
	 * @author maynardp
	 *
	 */
	public static enum AttributeKey {
		Maze,
		MyId,
		PursuedAgentId,
		PursuerAgentsIds,
		AgentsLocations
	}
	
	public PursuitWorldPercept(
			Maze maze, Integer myId, Integer pursuedAgentId, List<Integer> pursuerAgentsIds,
			Map<Integer, XYLocation> agentsLocations) {
		super(
			AttributeKey.values(),
			new Object[] { maze, myId, pursuedAgentId, pursuerAgentsIds, agentsLocations });
	}
}
