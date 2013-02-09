package cpsc470.pursuit.environment;

import java.util.LinkedHashSet;
import java.util.Set;

import aima.core.agent.Action;
import aima.core.util.datastructure.XYLocation;

public enum PursuitWorldAction implements Action {
	Up,
	Down,
	Left,
	Right,
	Stay;

	private static final Set<Action> actions = new LinkedHashSet<Action>(); // TODO (p2) make this an unmodifiable set
	static {
		for (PursuitWorldAction action : PursuitWorldAction.values()) {
			actions.add(action);
		}
	}

	public static final Set<Action> getActions() {
		return actions;
	}

	public XYLocation getDestinationLocation(XYLocation sourceLocation) {
		XYLocation destinationLocation;
		switch (this) {
			case Up:
				destinationLocation = sourceLocation.up();
				break;
				
			case Down:
				destinationLocation = sourceLocation.down();
				break;
				
			case Left:
				destinationLocation = sourceLocation.left();
				break;
				
			case Right:
				destinationLocation = sourceLocation.right();
				break;
				
			case Stay:
				destinationLocation = sourceLocation;
				break;
				
			default:
				throw new UnsupportedOperationException("Unexpected action '" + this + "'; please debug.");
		}
		return destinationLocation;
	}
	
	public static XYLocation getDestinationLocation(XYLocation sourceLocation, PursuitWorldAction action) {
		return action.getDestinationLocation(sourceLocation);
	}
	
	public static PursuitWorldAction getAction(XYLocation sourceLocation, XYLocation destinationLocation) {
		for (PursuitWorldAction action : PursuitWorldAction.values()) {
			if (destinationLocation.equals(action.getDestinationLocation(sourceLocation))) {
				return action;
			}
		}
		return null;
	}
	public boolean isNoOp() {
		return this == Stay;
	}
}
