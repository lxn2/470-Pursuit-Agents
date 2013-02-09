package cpsc470.pursuit.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cpsc470.pursuit.agent.AbstractAStarPursuitWorldAgent.AStarPursuitWorldState;
import cpsc470.pursuit.environment.Maze;
import cpsc470.pursuit.environment.PursuitWorldAction;
import cpsc470.pursuit.environment.PursuitWorldPercept;

import Cost.ItoNCost;
import Heuristic.ManhattanDistance;
import aima.core.agent.Action;
import aima.core.agent.Percept;
import aima.core.agent.State;
import aima.core.search.framework.ActionsFunction;
import aima.core.search.framework.DefaultStepCostFunction;
import aima.core.search.framework.GoalTest;
import aima.core.search.framework.GraphSearch;
import aima.core.search.framework.HeuristicFunction;
import aima.core.search.framework.Problem;
import aima.core.search.framework.QueueSearch;
import aima.core.search.framework.ResultFunction;
import aima.core.search.framework.SearchAgent;
import aima.core.search.framework.StepCostFunction;
import aima.core.search.informed.AStarSearch;
import aima.core.util.datastructure.XYLocation;

/**
 * We wrap a search agent rather than extend it because we want lazy search -- the
 * search shouldn't execute until the first execute() is called -- and we don't know
 * the initial state until then anyway.
 *	
 * @author maynardp
 *
 */
public abstract class AbstractAStarPursuitWorldAgent extends AbstractPursuitWorldAgent {

	protected static class AStarPursuitWorldState implements State {
		
		private Maze maze;
		
		private Integer pursuedAgentId;
		private List<Integer> pursuerAgentsIds;
		private Map<Integer, XYLocation> agentsLocations;
		
		public AStarPursuitWorldState(Maze maze, Integer pursuedAgentId, List<Integer> pursuerAgentsIds, Map<Integer, XYLocation> agentsLocations) {
			this.maze = maze;
			this.pursuedAgentId = pursuedAgentId;
			this.pursuerAgentsIds = new ArrayList<Integer>(pursuerAgentsIds);
			this.agentsLocations = new HashMap<Integer, XYLocation>(agentsLocations);
		}
		
		public AStarPursuitWorldState(AStarPursuitWorldState state) {
			this(state.maze, state.pursuedAgentId, state.pursuerAgentsIds, state.agentsLocations);
		}
		
		public Maze getMaze() {
			return maze;
		}

		public int getPursuedAgentId() {
			return pursuedAgentId;
		}
		
		public List<Integer> getPursuerAgentsIds() {
			return Collections.unmodifiableList(pursuerAgentsIds);
		}
		
		public Map<Integer, XYLocation> getAgentsLocations() {
			return Collections.unmodifiableMap(agentsLocations);
		}
		
		public int getNumPursuerAgents() {
			return pursuerAgentsIds.size();
		}
		
		public XYLocation getAgentLocation(int agentId) {
			return agentsLocations.get(agentId);
		}
		
		public XYLocation getPursuedAgentLocation() {
			return agentsLocations.get(pursuedAgentId);
		}
		
		public XYLocation getPursuerAgentLocation(int pursuerAgentIndex) {
			return agentsLocations.get(pursuerAgentsIds.get(pursuerAgentIndex));
		}

		/**
		 * Checks if state is legal, i.e., no agents are inside walls.
		 * 
		 * @return True iff state is legal.
		 */
		public boolean isLegal() {
			// Check nobody is inside a wall.
			boolean isLegal = !maze.isWall(getPursuedAgentLocation());
			for (int i = 0; isLegal && i < getNumPursuerAgents(); ++i) {
				isLegal = isLegal && !maze.isWall(getPursuerAgentLocation(i));
			}
			return isLegal;
		}
		
		/**
		 * Checks if pursued agent is safe, i.e., is in the safety location (whether or not a pursuer agent is there too).
		 * Pre-computed on construction.
		 * 
		 * @return True iff pursued agent is safe.
		 */
		public boolean isPursuedAgentSafe() {
			return getPursuedAgentLocation().equals(this.maze.getSafetyLocation());
		}
		
		/**
		 * Checks if pursuer agents have caught pursued agent, i.e., pursued agent shares a non-safety location with a pursuer agent.
		 * Pre-computed on construction.
		 * 
		 * @return True iff pursued agent is caught.
		 */
		public boolean isPursuedAgentCaught() {
			boolean isPursuedAgentCaught = false;
			if (!isPursuedAgentSafe()) {
				for (int i = 0; !isPursuedAgentCaught && i < this.pursuerAgentsIds.size(); ++i) {
					isPursuedAgentCaught = getPursuedAgentLocation().equals(getPursuerAgentLocation(i));
				}
			}
			
			return isPursuedAgentCaught;
		}
		
		public boolean canMove(int agentId, XYLocation.Direction direction) {
			XYLocation agentLocation = getAgentLocation(agentId);
			XYLocation newLocation = agentLocation.locationAt(direction);
			return !maze.isWall(newLocation);
		}

		/**
		 * @param agentId
		 * @param action
		 * @return True iff move is legal (i.e., not into a wall). Doesn't apply move if it is illegal.
		 */
		public boolean move(int agentId, PursuitWorldAction action) {
			XYLocation agentLocation = getAgentLocation(agentId);
			XYLocation newLocation = action.getDestinationLocation(agentLocation);
			if (!maze.isWall(newLocation)) {
				return move(agentId, newLocation);
			} else {
				return false;
			}
		}

		/**
		 * @param agentId
		 * @param destination
		 * @return True iff move is legal (i.e., not into a wall).
		 */
		protected boolean move(int agentId, XYLocation newLocation) {
			agentsLocations.put(agentId, newLocation);
			return !maze.isWall(newLocation);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 1;
			hash = hash * prime + getPursuedAgentLocation().getXCoOrdinate(); // note that XYLocation.hashCode() isn't very good (bug probably)
			hash = hash * prime + getPursuedAgentLocation().getYCoOrdinate();

			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if((obj == null) || (obj.getClass() != this.getClass())) {
		        return false;
			}
			
			AStarPursuitWorldState state = (AStarPursuitWorldState) obj;
			return state.getPursuedAgentLocation().equals(getPursuedAgentLocation());
		}

		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("[pursued: (");
			buf.append(getPursuedAgentLocation().getXCoOrdinate());
			buf.append(", ");
			buf.append(getPursuedAgentLocation().getYCoOrdinate());
			buf.append(")]");
			return buf.toString();
		}
	}

	private ItoNCost visitedStates;
	private ActionsFunction actionsFunction;
	private ResultFunction resultFunction;
	private GoalTest goalTest;
	private StepCostFunction stepCostFunction;
	
	private QueueSearch queueSearch;
	private HeuristicFunction heuristicFunction;
	private AStarSearch search;

	private SearchAgent searchAgent;

	public AbstractAStarPursuitWorldAgent() {
		actionsFunction = new PursuitActionsFunction(visitedStates);
		resultFunction = getResultFunction();
		goalTest = getGoalTest();
		stepCostFunction = getStepCostFunction();
		
		queueSearch = new GraphSearch();
		heuristicFunction = getHeuristicFunction();
		search = new AStarSearch(queueSearch, heuristicFunction);
		
		searchAgent = null;
	}
	
	@Override
	public Action execute(Percept percept) {
		if (searchAgent == null) {
			State initialState = getStateFromPercept((PursuitWorldPercept) percept);
			Problem problem = // note: we are using the default step cost function of 1 per *time* step (even no-ops)
				new Problem(
					initialState, actionsFunction, resultFunction, goalTest, stepCostFunction);
			try {
				searchAgent = new SearchAgent(problem, search);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return searchAgent.execute(percept);
	}

	@Override
	public Properties getInstrumentation() {
		return searchAgent.getInstrumentation();
	}

	protected ResultFunction getResultFunction() {
		return new PursuitResultFunction(this);
	}
	
	protected StepCostFunction getStepCostFunction() {
		return new DefaultStepCostFunction();
	}
	
	abstract protected GoalTest getGoalTest();

	protected HeuristicFunction getHeuristicFunction() {
		return new PursuitWorldManhattanHeuristicFunction(this);
	}
	
	protected AStarPursuitWorldState getStateFromPercept(PursuitWorldPercept percept) {
		Maze maze = (Maze) percept.getAttribute(PursuitWorldPercept.AttributeKey.Maze);
		Integer pursuedAgentId = (Integer) percept.getAttribute(PursuitWorldPercept.AttributeKey.PursuedAgentId);
		@SuppressWarnings("unchecked") // TODO (p2) remove need for this
		List<Integer> pursuerAgentsIds = (List<Integer>) percept.getAttribute(PursuitWorldPercept.AttributeKey.PursuerAgentsIds);
		@SuppressWarnings("unchecked") // TODO (p2) remove need for this
		HashMap<Integer, XYLocation> agentLocations =
			(HashMap<Integer, XYLocation>) percept.getAttribute(PursuitWorldPercept.AttributeKey.AgentsLocations);
		
		return new AStarPursuitWorldState(maze, pursuedAgentId, pursuerAgentsIds, agentLocations);
	}
	
	/**
	 * We return all actions and rely on the result/transition function to address actions that would move agent into a wall.
	 */
	protected static class PursuitActionsFunction implements ActionsFunction {
		private ItoNCost history;
		private AStarPursuitWorldState state;
		
		public PursuitActionsFunction(ItoNCost history){
			super();
			this.history = history;
		}
		
		@Override
		public Set<Action> actions(Object state) {
			Set<Action> actions = new HashSet<Action>();
			
			// TODO PROBLEM 3: FILL THIS IN
			this.state = (AStarPursuitWorldState) state;
			XYLocation agentInitialLocation = this.state.getPursuedAgentLocation();
			List<XYLocation> availableAdjacentLocations = getAvailableAdjacentLocations(agentInitialLocation, this.state.getMaze());
			for (XYLocation location : availableAdjacentLocations)
			{
				//if(!this.history.contains(nextLocation))
				//{
				PursuitWorldAction action = PursuitWorldAction.getAction(agentInitialLocation, location);
				actions.add(action);
				//}
			}
			return actions;
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
	}
	
	
	/**
	 * Result/transition function that returns the updated state of the world given the agent's move.
	 * It assumes only the agent's actions affect the world.
	 * Also, if an action would move the agent into a wall, the agent is left in its current location.
	 */
	protected static class PursuitResultFunction implements ResultFunction {
		
		private AbstractPursuitWorldAgent agent;
		private PursuitWorldAction action;
		
		public PursuitResultFunction(AbstractPursuitWorldAgent agent) {
			this.agent = agent;
		}

		@Override
		public Object result(Object state, Action action) {
			AStarPursuitWorldState oldState = (AStarPursuitWorldState) state;
			AStarPursuitWorldState newState = null;
			this.action = (PursuitWorldAction) action;
			
			// TODO PROBLEM 3: FILL THIS IN
			newState = new AStarPursuitWorldState(oldState.getMaze(), oldState.getPursuedAgentId(), oldState.getPursuerAgentsIds(), oldState.getAgentsLocations());
			newState.move(agent.getId(), this.action);
			
			return newState;
		}
		
		protected AbstractPursuitWorldAgent getAgent() {
			return agent;
		}
		
	}
	
	protected class PursuitWorldManhattanHeuristicFunction implements HeuristicFunction {
		
		private AbstractPursuitWorldAgent agent;
		private AStarPursuitWorldState currentState;
		
		public PursuitWorldManhattanHeuristicFunction(AbstractPursuitWorldAgent agent) {
			this.agent = agent;
		}

		@Override
		public double h(Object state) {
			double h = 0;
			this.currentState = (AStarPursuitWorldState) state;
			
			// TODO PROBLEM 3: FILL THIS IN
			ManhattanDistance manhattanDistance = new ManhattanDistance();
			XYLocation goal = this.currentState.getMaze().getSafetyLocation();
			h = (double) manhattanDistance.getDistance(this.currentState.getAgentLocation(this.agent.getId()), goal);
			return h; 
		}
		
	}

}
