package cpsc470.pursuit.agent;

import java.util.Properties;

import aima.core.agent.Agent;

public interface PursuitWorldAgent extends Agent {

	int getId();

	Properties getInstrumentation();
	
}
