package cpsc470.pursuit.environment;

import java.lang.IndexOutOfBoundsException;
import java.lang.NullPointerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import cpsc470.pursuit.agent.PursuedAgent;
import cpsc470.pursuit.agent.PursuerAgent;
import cpsc470.pursuit.agent.AbstractPursuitWorldAgent;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.EnvironmentState;
import aima.core.agent.Percept;
import aima.core.util.datastructure.XYLocation;
import aima.core.environment.xyenv.XYEnvironment;
import aima.core.environment.xyenv.Wall;

public class PursuitWorldEnvironment extends XYEnvironment {

	private Maze maze;
	private PursuedAgent pursuedAgent;
	private List<PursuerAgent> pursuerAgents;
	
	public PursuitWorldEnvironment(Maze maze) {
		super(maze.getWidth(), maze.getHeight());
		this.maze = maze;
		for (int y = 0; y < maze.getHeight(); ++y) {
			for (int x = 0; x < maze.getWidth(); ++x) {
				if (maze.isWall(x, y)) {
					addObjectToLocation(new Wall(), new XYLocation(x, y));
				}
			}
		}
		
		this.pursuedAgent = null;
		this.pursuerAgents = new ArrayList<PursuerAgent>();
	}
	
	// TODO (p2) add constructor that takes initial state
	
	/**
	 * @param anAgent Agent receiving percept. Ignored since environment is fully observable
	 *                to all agents.
	 * @return Percept with following mappings:
	 * 		   "maze" -> Maze,
	 * 		   "myId" -> int, "pursuedAgentId" -> int, "pursuerAgentsIds" -> List<Integer>,
	 *         "agentsLocations" -> HashMap<Integer, XYLocation>.
	 * 
	 */
	@Override
	public Percept getPerceptSeenBy(Agent anAgent) {
		
		// TODO (p2) verify that agent is in environment
		
		int myId = ((AbstractPursuitWorldAgent) anAgent).getId();
		
		Integer pursuedAgentId = pursuedAgent != null ? pursuedAgent.getId() : null;
		
		List<Integer> pursuerAgentsIds = new ArrayList<Integer>(pursuerAgents.size());
		for (PursuerAgent pursuerAgent : pursuerAgents) {
			pursuerAgentsIds.add(pursuerAgent.getId());
		}
		
		HashMap<Integer, XYLocation> agentsLocations = new LinkedHashMap<Integer, XYLocation>(pursuerAgents.size() + 1);
		if (pursuedAgent != null) {
			agentsLocations.put(pursuedAgentId, getCurrentLocationFor(pursuedAgent));
		}
		for (int i = 0; i < pursuerAgents.size(); ++i) {
			agentsLocations.put(pursuerAgentsIds.get(i), getCurrentLocationFor(pursuerAgents.get(i)));
		}
		
		PursuitWorldPercept percept = new PursuitWorldPercept(maze, myId, pursuedAgentId, pursuerAgentsIds, agentsLocations);
		
		return percept;
	}
	
	public int getHeight() {
		return maze.getHeight();
	}

	public int getWidth() {
		return maze.getWidth();
	}
	
	public XYLocation getSafetyLocation() {
		return maze.getSafetyLocation();
	}
	
	public PursuedAgent getPursuedAgent() {
		return pursuedAgent;
	}

	public PursuerAgent getPursuerAgent(int index) {
		if (index < 0 || index >= pursuerAgents.size()) {
			throw new IndexOutOfBoundsException("No agent with index " + index + " exists.");
		}
		return pursuerAgents.get(index);
	}
	
	public int getNumPursuerAgents() {
		return pursuerAgents.size();
	}

	public boolean isPursuedAgentSafe() {
		return getCurrentLocationFor(pursuedAgent).equals(getSafetyLocation());
	}
	
	public boolean isPursuedAgentCaught() {
		boolean isCaught = false;
		if (!isPursuedAgentSafe()) {
			XYLocation pursuedAgentLocation = getCurrentLocationFor(pursuedAgent);
			for (int i = 0; !isCaught && i < pursuerAgents.size(); ++i) {
				XYLocation pursuerAgentLocation = getCurrentLocationFor(pursuerAgents.get(i));
				isCaught = pursuedAgentLocation.equals(pursuerAgentLocation);
			}
		}
		return isCaught;
	}
	
	@Override
	public boolean isDone() {
		return isPursuedAgentSafe() || isPursuedAgentCaught();
	}

	// CONSIDER if needed, extend state to include safety location, etc., and override getCurrentState()
	
	public void addPursuedAgent(PursuedAgent pursuedAgent, XYLocation location) {
		if (pursuedAgent == null) {
			throw new NullPointerException("Null pursued agent not allowed.");
		}
		if (this.pursuedAgent != null) {
			throw new IllegalArgumentException("A pursued agent already exists; only one supported at a time.");
		}
		addObjectToLocation(pursuedAgent, location);
		this.pursuedAgent = pursuedAgent;
	}
	
	public void replacePursuedAgent(PursuedAgent newPursuedAgent) {
		if (newPursuedAgent == null) {
			throw new NullPointerException("Null pursued agent not allowed.");
		}
		if (pursuedAgent == null) {
			throw new IllegalArgumentException("No pursued agent already exists to be replaced; use add instead.");
		}
		XYLocation location = getCurrentLocationFor(pursuedAgent);
		removeAgent(pursuedAgent);
		addObjectToLocation(newPursuedAgent, location);
		pursuedAgent = newPursuedAgent;
	}
	
	public void addPursuerAgent(PursuerAgent pursuerAgent, XYLocation location) {
		if (pursuerAgent == null) {
			throw new NullPointerException("Null pursuer agent not allowed.");
		}
		addAgentToLocation(pursuerAgent, location);
		pursuerAgents.add(pursuerAgent);
	}
	
	public void replacePursuerAgent(int index, PursuerAgent newPursuerAgent) {
		if (newPursuerAgent == null) {
			throw new NullPointerException("Null pursuer agent not allowed.");
		}
		PursuerAgent oldPursuerAgent = pursuerAgents.get(index);
		XYLocation location = getCurrentLocationFor(oldPursuerAgent);
		removeAgent(oldPursuerAgent);
		addObjectToLocation(newPursuerAgent, location);
		pursuerAgents.set(index, newPursuerAgent);
	}
	
	protected void addAgentToLocation(Agent agent, XYLocation location) {
		if (isBlocked(location)) {
			throw new IllegalArgumentException("Can't add agent inside a wall.");
		}
		
		addObjectToLocation(agent, location);
	}

	/**
	 * Execute agent's action if possible, leaves agent as-is if not.
	 * 
	 * @param a
	 * @param action
	 * @return Updated environment state.
	 * @exception ClassCastException if action is not a PursuitWorldAction.
	 */
	@Override
	public EnvironmentState executeAction(Agent a, Action action) {
		PursuitWorldAction pursuitAction = (PursuitWorldAction) action;
		switch (pursuitAction) {
			case Up:
				moveObject(a, XYLocation.Direction.North);
				break;
				
			case Down:
				moveObject(a, XYLocation.Direction.South);
				break;
				
			case Left:
				moveObject(a, XYLocation.Direction.West);
				break;
				
			case Right:
				moveObject(a, XYLocation.Direction.East);
				break;
			
			default:
				// stay put
		}

		return getCurrentState();
	}
}
