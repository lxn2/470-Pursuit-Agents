package Cost;

import java.util.Map;
import aima.core.util.datastructure.XYLocation;
import Heuristic.ManhattanDistance;

public class ItoNCost {
	private Map<Integer, XYLocation> agentLocations;
	private Integer GCost;
	private ManhattanDistance manhattanDistance;
	
	public ItoNCost(){
		this.GCost = 0;
	}
	
	public void addNode(XYLocation coordinate){
		agentLocations.put(agentLocations.size() + 1, coordinate);
		Integer stepCost = manhattanDistance.getDistance(coordinate, agentLocations.get(agentLocations.size() - 1));
		GCost += stepCost;
	}
	
	public boolean contains(XYLocation coordinate){
		if (agentLocations.containsValue(coordinate))
			return true;
		return false;
	}
	
	public Integer getGCost(){
		return GCost;
	}
}
