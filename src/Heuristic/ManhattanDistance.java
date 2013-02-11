package Heuristic;


import aima.core.util.datastructure.XYLocation;
import cpsc470.pursuit.environment.PursuitWorldAction;


import java.lang.Math;


public class ManhattanDistance {
	
	private XYLocation first;
	private XYLocation second;
	private int distance;
	
	public ManhattanDistance(XYLocation first, XYLocation second){
		this.first = first;
		this.second = second;
		distance = 0;
	}
	public ManhattanDistance(){
		distance = 0;
	}
	
	public int getDistance(){
		int firstX = first.getXCoOrdinate();
		int firstY = first.getYCoOrdinate();
		
		int secondX = second.getXCoOrdinate();
		int secondY = second.getYCoOrdinate();
		
		int finalX = Math.abs(firstX - secondX);
		int finalY = Math.abs(firstY - secondY);
		
		distance = finalX + finalY;
		
		return distance;
	}
	public int getDistance(XYLocation iFirst, XYLocation iSecond){
		int firstX = iFirst.getXCoOrdinate();
		int firstY = iFirst.getYCoOrdinate();
		
		int secondX = iSecond.getXCoOrdinate();
		int secondY = iSecond.getYCoOrdinate();
		
		int finalX = Math.abs(firstX - secondX);
		int finalY = Math.abs(firstY - secondY);
		
		distance = finalX + finalY;
		
		return distance;
	}

	public int getXDistance(XYLocation iFirst, XYLocation iSecond){
		int firstX = iFirst.getXCoOrdinate();
		int secondX = iSecond.getXCoOrdinate();
		
		int finalX = Math.abs(firstX - secondX);
				
		distance = finalX;
		
		return distance;
	}
	public int getYDistance(XYLocation iFirst, XYLocation iSecond){
		int firstY = iFirst.getYCoOrdinate();
		int secondY = iSecond.getYCoOrdinate();

		int finalY = Math.abs(firstY - secondY);
		
		distance = finalY;
		
		return distance;
	}
	
	
}
