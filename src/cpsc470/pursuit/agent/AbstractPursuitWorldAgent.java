package cpsc470.pursuit.agent;

import java.util.Properties;

import cpsc470.pursuit.environment.PursuitWorldAction;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractAgent;
import aima.core.agent.impl.NoOpAction;

public abstract class AbstractPursuitWorldAgent extends AbstractAgent implements PursuitWorldAgent {

	public AbstractPursuitWorldAgent() {
		super();
	}
	
	public AbstractPursuitWorldAgent(AgentProgram agentProgram) {
		super(agentProgram);
	}

	@Override
	public int getId() {
		return System.identityHashCode(this);
	}


	/**
	 * @return Empty instrumentation property bag by default.
	 */
	@Override
	public Properties getInstrumentation() {
		Properties retVal = new Properties();
		
		return retVal;
	}

	@Override
	public Action execute(Percept p) {
		Action action = super.execute(p);
		return action == NoOpAction.NO_OP ? PursuitWorldAction.Stay : action;
	}
	
}
