package cpsc470.pursuit.agent;

import cpsc470.pursuit.environment.Maze;
import cpsc470.pursuit.environment.PursuitWorldPercept;

import aima.core.util.datastructure.XYLocation;

/**
 * Specialize AbstractOnlineGreedyPursuitWorldAgentProgram for pursued agents by overriding getGoalLocation().
 * 
 * @author maynardp
 *
 */
public class OnlineGreedyPursuedAgentProgram extends AbstractOnlineGreedyPursuitWorldAgentProgram {

	@Override
	protected XYLocation getGoalLocation(PursuitWorldPercept percept) {
		Maze maze = (Maze) percept.getAttribute(PursuitWorldPercept.AttributeKey.Maze);
		return maze.getSafetyLocation();
	}

}
