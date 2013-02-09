package cpsc470.pursuit.agent;

import java.util.Properties;

public class AbstractNoOpPursuitWorldAgent extends AbstractPursuitWorldAgent {

	/**
	 * @return Empty property bag since agent does nothing.
	 */
	@Override
	public Properties getInstrumentation() {
		return new Properties();
	}

}
