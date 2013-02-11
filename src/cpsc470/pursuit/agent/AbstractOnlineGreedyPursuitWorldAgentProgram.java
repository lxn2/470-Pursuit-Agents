package cpsc470.pursuit.agent;

import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import javax.tools.JavaFileManager.Location;

import cpsc470.pursuit.environment.Maze;
import cpsc470.pursuit.environment.PursuitWorldAction;
import cpsc470.pursuit.environment.PursuitWorldPercept;

import Heuristic.ManhattanDistance;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.util.datastructure.XYLocation;

public abstract class AbstractOnlineGreedyPursuitWorldAgentProgram implements AgentProgram {
	
	@Override
	public Action execute(Percept percept) {
		
		PursuitWorldPercept pwPercept = (PursuitWorldPercept) percept;
		XYLocation agentLocation = getAgentLocation(pwPercept);
		XYLocation goalLocation = getGoalLocation(pwPercept);
		Maze maze = (Maze) pwPercept.getAttribute(PursuitWorldPercept.AttributeKey.Maze);
		
		return getGreedyMove(agentLocation, goalLocation, maze);
	}
	
	protected XYLocation getAgentLocation(PursuitWorldPercept percept) {
		@SuppressWarnings("unchecked") // TODO (p2) remove need for this
		Map<Integer, XYLocation> agentLocations =
				(Map<Integer, XYLocation>) percept.getAttribute(PursuitWorldPercept.AttributeKey.AgentsLocations);
		int agentId = (Integer) percept.getAttribute(PursuitWorldPercept.AttributeKey.MyId);
		return agentLocations.get(agentId);
	}
	
	abstract protected XYLocation getGoalLocation(PursuitWorldPercept percept);

	/**
	 * Cycle through neighboring locations and greedily pick the one that satisfies the following rules:
	 *	1.	Make no illegal moves (never try to walk through walls).
	 *	2.	Use greedy hill-climbing with a Manhattan distance heuristic function to the goal location.
	 *	3.	If there is a tie between two axes, try to minimize the distance along the axis which is farther from the goal.
	 *		For example, if the agent is at (2,2), the goal is at (3,10), and the agent can move either right or down, choose down.
	 *	4.	If still tied, prefer vertical movement to horizontal movement.
	 *	5.	If all one-step moves take the agent away from the goal, then the agent stays put.
	 *
	 * @param initialLocation
	 * @param goalLocation
	 * @param maze
	 * @return PursuitAction agent should take.
	 */
	protected Action getGreedyMove(XYLocation initialLocation, XYLocation goalLocation, Maze maze) {

		PursuitWorldAction action = PursuitWorldAction.Stay;
		// TODO PROBLEM 1: FILL THIS IN
		//int resultX = goalLocation.getXCoOrdinate() - initialLocation.getXCoOrdinate();
		//int resultY = goalLocation.getYCoOrdinate() - initialLocation.getYCoOrdinate();
		
		
		ManhattanDistance man = new ManhattanDistance();
		List<XYLocation> availableMoves = new LinkedList<XYLocation>();
		List<Integer> routeHueristics = new LinkedList<Integer>();
		availableMoves = getAvailableAdjacentLocations(initialLocation, maze);
		for(int i=0; i<availableMoves.size(); i++){
			int tempDistance;
			tempDistance = man.getDistance(goalLocation, availableMoves.get(i));
			routeHueristics.add(tempDistance);
		}

		boolean tied = false;
		int min1pos = 0;
		int min2pos = 0;
		int min2 = 0;
		int min1 = routeHueristics.get(0);

		for (int i=0; i<routeHueristics.size(); i++){
			if(routeHueristics.get(i) < min1){
				min1 = routeHueristics.get(i);
				min1pos = i;
			}
			else if (routeHueristics.get(i) == min1){
				min2 = min1;
				min2pos = i;

			}
		}
		if(min1 == min2){
			tied = true;
		}
		
		if(tied){
			PursuitWorldAction action1 = PursuitWorldAction.getAction(initialLocation, availableMoves.get(min1pos));
			PursuitWorldAction action2 = PursuitWorldAction.getAction(initialLocation, availableMoves.get(min2pos));
			
			boolean sameAxis = checkAxis(action1, action2);
			
			
			int firstX = initialLocation.getXCoOrdinate();
			int firstY = initialLocation.getYCoOrdinate();
			int secondX = goalLocation.getXCoOrdinate();
			int secondY = goalLocation.getYCoOrdinate();
			int finalX = Math.abs(firstX - secondX);
			int finalY = Math.abs(firstY - secondY);
			
			if (finalX > finalY){
				if(sameAxis){
					if(action1.equals(PursuitWorldAction.Left))
						action = action1;
					else 
						action = action2;
				}
				else{
					if(action1.equals(PursuitWorldAction.Left) || action1.equals(PursuitWorldAction.Right) )
						action = action1;
					else 
						action = action2;			
				}
				
			}
			else if(finalX < finalY) {
				if(sameAxis){
					if(action1.equals(PursuitWorldAction.Down))
						action = action1;
					else 
						action = action2;
				}
				else{
					if(action1.equals(PursuitWorldAction.Down) || action1.equals(PursuitWorldAction.Up) )
						action = action1;
					else 
						action = action2;			
				}
			}
			else{
				if(sameAxis){
					if(action1.equals(PursuitWorldAction.Down))
						action = action1;
					else 
						action = action2;
				}
				else{
					if(action1.equals(PursuitWorldAction.Down) || action1.equals(PursuitWorldAction.Up) )
						action = action1;
					else 
						action = action2;			
				}

			}
		
			if (finalX == 0){
				if(sameAxis){
					if(action1.equals(PursuitWorldAction.Left))
						action = action1;
					else 
						action = action2;
				}
				else{
					if(action1.equals(PursuitWorldAction.Left) || action1.equals(PursuitWorldAction.Right) )
						action = action1;
					else 
						action = action2;			
				}
				
				
				


			}
			if (finalY == 0){

				if(sameAxis){
					if(action1.equals(PursuitWorldAction.Down))
						action = action1;
					else 
						action = action2;
				}
				else{
					if(action1.equals(PursuitWorldAction.Down) || action1.equals(PursuitWorldAction.Up) )
						action = action1;
					else 
						action = action2;			
				}
			}
					
			
		}
		else {		
		XYLocation path = availableMoves.get(min1pos);
		action = PursuitWorldAction.getAction(initialLocation, path);
		}
		
		
		return action;
	}
	
	protected List<XYLocation> getAvailableAdjacentLocations(XYLocation initialLocation, Maze maze) {
		
		int initX = initialLocation.getXCoOrdinate();
		int initY = initialLocation.getYCoOrdinate();
		List<XYLocation> accessibleLocations = new LinkedList<XYLocation>();
		accessibleLocations.add(initialLocation); // agent could choose not to move
		for (int x = initX - 1; x <= initX + 1; ++x) {
			for (int y = initY - 1; y <= initY + 1; ++y) {
				if (!maze.isWall(x, y) && (Math.abs(x - initX) + Math.abs(y - initY) == 1)) {
					// Move is legitimate (not blocked, diagonal, or noop).
					accessibleLocations.add(new XYLocation(x, y));
				}
			}
		}
		return accessibleLocations;
	}
	
	
	protected boolean checkAxis(PursuitWorldAction action1, PursuitWorldAction action2){
		boolean direction = false;
		boolean action1axis = false; // false = horizontal
		boolean action2axis = false; // true = vertical
		if(	action1.equals(PursuitWorldAction.Left ) || action1.equals(PursuitWorldAction.Right)  ) 
			action1axis = false;
		else
			action1axis = true;
		if(	action2.equals(PursuitWorldAction.Left ) || action2.equals(PursuitWorldAction.Right)  ) 	
			action2axis = false;
		else
			action2axis = true;
		
		if(action2axis == action1axis)
			direction = true;
		else
			direction = false;
				
		return direction;
		
		
	}
	
	
	

	
}
