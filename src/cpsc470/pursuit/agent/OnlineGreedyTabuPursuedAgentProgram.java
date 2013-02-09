package cpsc470.pursuit.agent;

import aima.core.agent.Action;
import aima.core.util.datastructure.XYLocation;
import cpsc470.pursuit.environment.Maze;
import cpsc470.pursuit.environment.PursuitWorldAction;

public class OnlineGreedyTabuPursuedAgentProgram extends OnlineGreedyPursuedAgentProgram {

	private static final int TABU_LIST_SIZE = 1000; // TODO (p2) make this an argument for at least one constructor
	
	public OnlineGreedyTabuPursuedAgentProgram() {
		
		// PROBLEM 2: FILL THIS IN
		
	}
	
	public void clearTabuList() {
		
		// PROBLEM 2: FILL THIS IN
		
	}
	
	@Override
	protected Action getGreedyMove(XYLocation currentLocation, XYLocation goalLocation, Maze maze) {
		PursuitWorldAction action = PursuitWorldAction.Stay;
		
		// PROBLEM 2: FILL THIS IN
		// 
		
		return action;
	}
}
