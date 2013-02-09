package cpsc470.pursuit.agent;

import cpsc470.pursuit.environment.Maze;
import cpsc470.pursuit.environment.PursuitWorldPercept;
import aima.core.search.framework.GoalTest;
import aima.core.util.datastructure.XYLocation;

public class AStarPursuedAgent extends AbstractAStarPursuitWorldAgent implements PursuedAgent {
	
	@Override
	protected GoalTest getGoalTest() {
		return new AStarPursuedAgent.PursuedAgentGoalTest();
	}

	protected static class PursuedAgentGoalTest implements GoalTest { // CONSIDER rename SearchPursuedAgentGoalTest and move
		public boolean isGoalState(Object state) {

			boolean isGoal = true;
			
			// TODO PROBLEM 3: FILL THIS IN
			AStarPursuitWorldState currentState = (AStarPursuitWorldState) state;
			XYLocation goalLocation = currentState.getMaze().getSafetyLocation();
			XYLocation currentLocation = currentState.getPursuedAgentLocation();
			
			if(!currentLocation.equals(goalLocation))
				isGoal = false;
			return isGoal;
		}
	}
}
