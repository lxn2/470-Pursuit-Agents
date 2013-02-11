package cpsc470.pursuit.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Heuristic.ManhattanDistance;
import aima.core.agent.Action;
import aima.core.search.framework.DefaultStepCostFunction;
import aima.core.search.framework.HeuristicFunction;
import aima.core.search.framework.ResultFunction;
import aima.core.search.framework.StepCostFunction;
import aima.core.util.datastructure.XYLocation;

import cpsc470.pursuit.agent.AbstractAStarPursuitWorldAgent.AStarPursuitWorldState;
import cpsc470.pursuit.environment.Maze;
import cpsc470.pursuit.environment.PursuitWorldAction;
import cpsc470.pursuit.environment.PursuitWorldPercept;

/**
 * Extends AStarPursuedAgent so as to take into account the locations of the pursuer agents in the state space.
 * 
 * @author maynardp
 *
 */
public class SmartAStarPursuedAgent extends AStarPursuedAgent {

	protected static class SmartAStarPursuedAgentState extends AStarPursuitWorldState {
		protected boolean[][] mazeView;
		protected Map<Integer, Integer> pursuersAgentsDistancesToGoal;
		protected Map<Integer, Integer> pursuersAgentsDistancesToPursuedAgent;
		
		public SmartAStarPursuedAgentState(Maze maze, Integer pursuedAgentId, List<Integer> pursuerAgentsIds, Map<Integer, XYLocation> agentsLocations) {
			super(maze, pursuedAgentId, pursuerAgentsIds, agentsLocations);
		}
		
		public SmartAStarPursuedAgentState(SmartAStarPursuedAgentState state) {
			super(state.getMaze(), state.getPursuedAgentId(), state.getPursuerAgentsIds(), state.getAgentsLocations());
			this.pursuersAgentsDistancesToGoal= new HashMap<Integer, Integer>();
			this.pursuersAgentsDistancesToPursuedAgent = new HashMap<Integer,Integer>();
			//updatePursuersAgentsDistancesToGoal();
			//updatePursuersAgentsDistancesToPursuedAgent();
			mazeView = this.getMaze().getIsWall();
		}
		/* 
		public void updatePursuersAgentsDistancesToGoal() {
			XYLocation pursuedAgentLocation = this.getPursuedAgentLocation();
			for(Integer pursuerAgentId : this.getPursuerAgentsIds())
			{
				XYLocation agentLocation =  this.getPursuerAgentLocation(pursuerAgentId);
				
			}
		} 
		
		public void updatePursuersAgentsDistancesToPursuedAgent() {
			
		}*/
		
		/**
		 * Invalidate state by putting agent in a wall.
		 */
		public void invalidate() {
			move(this.getPursuedAgentId(), new XYLocation(0, 0));
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 1;
			hash = hash * prime + getPursuedAgentLocation().hashCode();
			for (int i = 0; i < getNumPursuerAgents(); ++i) {
				hash = hash * prime + getPursuerAgentLocation(i).hashCode();
			}
	
			return hash;
		}
	
		@Override
		public boolean equals(Object obj) {
			boolean isEqual = true;
			if((obj == null) || (obj.getClass() != this.getClass())) {
		        return false;
			}
			
			// TODO PROBLEM 4: FILL THIS IN
			SmartAStarPursuedAgentState state = (SmartAStarPursuedAgentState) obj;
			for(Integer agentId : this.getPursuerAgentsIds()) {
				XYLocation thisAgentLocation = this.getAgentLocation(agentId);
				XYLocation stateAgentLocation = state.getAgentLocation(agentId);
				if(thisAgentLocation.equals(stateAgentLocation)) 
				{
					isEqual = false;
					break;
				}
			}
			
			return isEqual;
		}
	
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("[pursued:(");
			buf.append(getPursuedAgentLocation().getXCoOrdinate());
			buf.append(", ");
			buf.append(getPursuedAgentLocation().getYCoOrdinate());
			buf.append(')');
			for (int i = 0; i < getNumPursuerAgents(); ++i) {
				XYLocation loc = getPursuerAgentLocation(i);
				buf.append("; pursuer ");
				buf.append(i);
				buf.append(":(");
				buf.append(loc.getXCoOrdinate());
				buf.append(", ");
				buf.append(loc.getYCoOrdinate());
				buf.append(')');
			}
			buf.append(']');
			return buf.toString();
		}
	
	}
	
	/**
	 * Result/transition function that returns the updated state of the world given the agent's move.
	 * It also accounts for the moves made by the pursuer agents, assuming they are greedy.
	 * Also, if an action would move the agent into a wall, the agent is left in its current location.
	 */
	protected static class SmartAStarPursuerResultFunction extends AbstractAStarPursuitWorldAgent.PursuitResultFunction {
		
		public SmartAStarPursuerResultFunction(AbstractPursuitWorldAgent agent) {
			super(agent);
		}

		@Override
		public Object result(Object state, Action action) {
			SmartAStarPursuedAgentState oldState = (SmartAStarPursuedAgentState) state;
			PursuitWorldAction pursuitAction = (PursuitWorldAction) action;
			
			SmartAStarPursuedAgentState newState = null;
			
			// TODO PROBLEM 4: FILL THIS IN
			newState = new SmartAStarPursuedAgentState(oldState.getMaze(), oldState.getPursuedAgentId(), oldState.getPursuerAgentsIds(), oldState.getAgentsLocations());
			
			// update location of each pursuer agent based on greedy algorithm
			newState.move(this.agent.getId(), pursuitAction);
			XYLocation pursuedAgentNewLocation = newState.getPursuedAgentLocation();
			for(Integer agentId : newState.getPursuerAgentsIds()) {
				XYLocation agentLocation = newState.getAgentLocation(agentId);
				AbstractOnlineGreedyPursuitWorldAgentProgram greedyProgram = new OnlineGreedyPursuerAgentProgram();
				PursuitWorldAction pursuerNextAction = (PursuitWorldAction) greedyProgram.getGreedyMove(agentLocation, pursuedAgentNewLocation, newState.getMaze());
				newState.move(agentId, pursuerNextAction);
			}
			
			for(Integer agentId : newState.getPursuerAgentsIds()) {
				XYLocation pursuerLocation = newState.getAgentLocation(agentId);
				if (pursuerLocation == pursuedAgentNewLocation) 
				{
					newState.invalidate(); //Double.POSITIVE_INFINITY;
					break;
				}
			}
			
			return newState;
		}
		
	}

	@Override
	protected ResultFunction getResultFunction() {
		return new SmartAStarPursuerResultFunction(this);
	}
	
	@Override
	protected StepCostFunction getStepCostFunction() {
		return new SmartAStarStepCostFunction();
	}

	/*
	@Override
	protected HeuristicFunction getHeuristicFunction() {
		return new SmartAStarHeuristicFunction(this);
	} */

	protected SmartAStarPursuedAgentState getStateFromPercept(PursuitWorldPercept percept) {
		Maze maze = (Maze) percept.getAttribute(PursuitWorldPercept.AttributeKey.Maze);
		Integer pursuedAgentId = (Integer) percept.getAttribute(PursuitWorldPercept.AttributeKey.PursuedAgentId);
		@SuppressWarnings("unchecked") // TODO (p2) remove need for this
		List<Integer> pursuerAgentsIds = (List<Integer>) percept.getAttribute(PursuitWorldPercept.AttributeKey.PursuerAgentsIds);
		@SuppressWarnings("unchecked") // TODO (p2) remove need for this
		HashMap<Integer, XYLocation> agentLocations =
			(HashMap<Integer, XYLocation>) percept.getAttribute(PursuitWorldPercept.AttributeKey.AgentsLocations);
		
		return new SmartAStarPursuedAgentState(maze, pursuedAgentId, pursuerAgentsIds, agentLocations);
	}

	/*
	protected static class SmartAStarHeuristicFunction implements HeuristicFunction {
		private SmartAStarPursuedAgent agent;
		private SmartAStarPursuedAgentState currentState;
		
		public SmartAStarHeuristicFunction(SmartAStarPursuedAgent agent) {
			this.agent = agent;
		}	
		
		@Override
		public double h(Object state) {
			double h = 0;
			this.currentState = (SmartAStarPursuedAgentState) state;
			
			// 
			ManhattanDistance manhattanDistance = new ManhattanDistance();
			XYLocation goal = this.currentState.getMaze().getSafetyLocation();
			h = (double) manhattanDistance.getDistance(this.currentState.getAgentLocation(this.agent.getId()), goal);
			
			for(Integer agentId : this.currentState.getPursuerAgentsIds()) {
				XYLocation agentLocation = this.currentState.getAgentLocation(agentId);
				AbstractOnlineGreedyPursuitWorldAgentProgram greedyProgram = new OnlineGreedyPursuerAgentProgram();
				Action pursuerNextAction = greedyProgram.getGreedyMove(agentLocation, this.currentState.getPursuedAgentLocation(), this.currentState.getMaze());
				if(pursuerNextAction == PursuitWorldAction.Stay)
					h--;
			}
			
			return h;
		}
		
	} */
	
	/**
	 * Use the step cost function to make captures and illegal states extremely unpalatable.
	 * 
	 * @author maynardp
	 *
	 */
	protected static class SmartAStarStepCostFunction extends DefaultStepCostFunction {

		// TODO (p2) this should probably be the default step cost function
		
		@Override
		public double c(Object stateFrom, Action action, Object stateTo) {

			double cost = 1;
			
			// TODO PROBLEM 4: FILL THIS IN
			SmartAStarPursuedAgentState state = (SmartAStarPursuedAgentState) stateTo;
			List<Integer> pursuerIds = state.getPursuerAgentsIds();
			for(Integer pursuerId : pursuerIds) {
				XYLocation pursuerLocation = state.getAgentLocation(pursuerId);
				if (pursuerLocation == state.getPursuedAgentLocation()) 
				{
					cost = 100000.00; //Double.POSITIVE_INFINITY;
					break;
				}
			}
			return cost;
		}
		
	}

}
