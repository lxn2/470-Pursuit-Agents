package cpsc470.pursuit.agent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Heuristic.ManhattanDistance;
import aima.core.agent.Action;
import aima.core.util.datastructure.XYLocation;
import cpsc470.pursuit.environment.Maze;
import cpsc470.pursuit.environment.PursuitWorldAction;

public class OnlineGreedyTabuPursuedAgentProgram extends OnlineGreedyPursuedAgentProgram {
	private static final int TABU_LIST_SIZE = 25; // TODO (p2) make this an argument for at least one constructor
	private ArrayList<XYLocation>  tabuList;
	private ManhattanDistance man;
	
	public OnlineGreedyTabuPursuedAgentProgram() {
		
		// PROBLEM 2: FILL THIS IN
		tabuList = new ArrayList<XYLocation>();
		man = new ManhattanDistance();
	}
	
	public void clearTabuList() {
		// PROBLEM 2: FILL THIS IN
		tabuList.clear();
		
	}
	@Override
	protected Action getGreedyMove(XYLocation currentLocation, XYLocation goalLocation, Maze maze) {
		// PROBLEM 2: FILL THIS IN
		
		
		
		PursuitWorldAction action = PursuitWorldAction.Stay;
		XYLocation path;
		List<XYLocation> availableMoves = new LinkedList<XYLocation>();
		List<XYLocation> initialMoves = new LinkedList<XYLocation>();
		List<Integer> initialHueristics = new LinkedList<Integer>();
		List<Integer> routeHueristics = new LinkedList<Integer>();
		List<XYLocation> tempMoves = new LinkedList<XYLocation>();
		List<Integer> tempHueristics = new LinkedList<Integer>();
		List<Integer> bannedIndex = new LinkedList<Integer>();
		
		initialMoves = getAvailableAdjacentLocations(currentLocation, maze);
		
		//soft counter for the length of the available moves list
		int availableSize = initialMoves.size();
		
		
		//set heuristics for the valid moves
		for(int i=0; i<initialMoves.size(); i++){
			int tempDistance = 0;
			tempDistance = man.getDistance(goalLocation, initialMoves.get(i));
			initialHueristics.add(tempDistance);
		}
		
		while(!initialMoves.isEmpty()){
			XYLocation attemptedMove = initialMoves.get(0);
			int temp = 0;
			boolean validMove = true;
			temp = initialHueristics.get(0);
			for(int b=0; b<tabuList.size(); b++){
				XYLocation bannedMove = tabuList.get(b);
				if (attemptedMove.equals(bannedMove) ){
					tempMoves.add(attemptedMove);
					tempHueristics.add(temp);
					availableSize = availableSize - 1;
					validMove = false;
				}
				
			}
			//if move is not illegal, add it to the acceptable moves list
			if(validMove){
				availableMoves.add(attemptedMove);
				routeHueristics.add(temp);				
			}
			initialHueristics.remove(0);
			initialMoves.remove(0);
		}

		if(availableSize < 1){
			clearTabuList();

			availableSize = tempMoves.size();
			
			for(int b=0; b< availableSize; b++){
				availableMoves.add(tempMoves.get(0));
				routeHueristics.add(tempHueristics.get(0));
				tempMoves.remove(0);
				tempHueristics.remove(0);
				
			}
		}
		
		boolean tied = false;
		int min1pos = 0;
		int min2pos = 0;
		int min2 = 0;
		int min1 = routeHueristics.get(0);

		for (int i=1; i<availableSize; i++){
			if(routeHueristics.get(i) < min1){
				min1 = routeHueristics.get(i);
				min1pos = i;
			}
			else {
				if (routeHueristics.get(i) == min1){
			
				min2 = min1;
				min2pos = i;
				}
			}
		}
		if(min1 == min2){
			tied = true;
		}
		
		if(tied){
			PursuitWorldAction action1 = PursuitWorldAction.getAction(currentLocation, availableMoves.get(min1pos));
			PursuitWorldAction action2 = PursuitWorldAction.getAction(currentLocation, availableMoves.get(min2pos));
			PursuitWorldAction checkDirection = man.getDirection(currentLocation, goalLocation);
		
			if (checkDirection.equals(action1) ){
				action = action1;
				path = availableMoves.get(min1pos);
			}else{
				action = action2;
				path = availableMoves.get(min2pos);
			}
		
		}
		else {		
		path = availableMoves.get(min1pos);
		action = PursuitWorldAction.getAction(currentLocation, path);
		}
		
		
		int addNode = 0;
		
		if(!tabuList.contains(currentLocation))
			addNode++;
		if(!tabuList.contains(path))
			addNode++;
		

		if( tabuList.size() + addNode >= TABU_LIST_SIZE ){
			tabuList.clear();
		}
		else{
			if(!tabuList.contains(currentLocation))

				tabuList.add(currentLocation);
		
			if(!tabuList.contains(path))
				tabuList.add(path);
			
		}
			
		
		return action;
	}
}
