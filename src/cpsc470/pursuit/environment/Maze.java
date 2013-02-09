package cpsc470.pursuit.environment;

import aima.core.util.datastructure.XYLocation;

public class Maze {
	private boolean[][] isWall;
	private XYLocation safetyLocation;
	
	/**
	 * Input array is assumed to not contain wall border and, thus, coordinates are displaced by (-1, -1). Adds the border.
	 * @param isWall Coordinates of walls. Assumed not to include border walls, so coordinates are displaced by (-1, -1). 
	 * @param safetyLocation Location of safety -- 1-based if wrt to input array. Equivalently, 0-based if accounting for boundary. 
	 */
	public Maze(boolean[][] isWall, XYLocation safetyLocation) {
		int height = isWall.length + 2;
		int width = isWall.length > 0 ? isWall[0].length + 2 : 2;
		this.isWall = new boolean[height][width];
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				this.isWall[y][x] = (x == 0) || (x == width - 1) || (y == 0) || (y == height - 1) || isWall[y - 1][x - 1];
			}
		}
		
		this.safetyLocation = safetyLocation;
	}
	
	public int getHeight() {
		return isWall.length;
	}
	
	public int getWidth() {
		return isWall[0].length;
	}
	
	public boolean isWall(XYLocation location) {
		return isWall(location.getXCoOrdinate(), location.getYCoOrdinate());
	}
	
	public boolean isWall(int x, int y) {
		return isWall[y][x];
	}
	
	public XYLocation getSafetyLocation() {
		return safetyLocation;
	}
}
