package cpsc470.pursuit;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import cpsc470.pursuit.environment.Maze;
import aima.core.util.datastructure.XYLocation;

public class PursuitWorldGame {
	
	public static void main(String[] args) {
		
		// Load maze and agent specs from file.
		
		// TODO (p2) refactor loading into separate method.
		
		if (args.length < 1) {
			throw new IllegalArgumentException("Missing environment file path.");
		}

		List<String[]> mazeRows = new LinkedList<String[]>();
		XYLocation safetyLocation = null;
		XYLocation initialPursuedAgentLocation = null;
		List<XYLocation> initialPursuerAgentLocations = new LinkedList<XYLocation>();

		String filename = args[0];
		Path file = FileSystems.getDefault().getPath(filename);
		BufferedReader reader = null;
		try {
			reader = Files.newBufferedReader(file, Charset.defaultCharset());
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("0") || line.startsWith("1")) {
					mazeRows.add(line.split(" "));
				} else if (line.startsWith("goal")) {
					safetyLocation = getLocationFromConfigLine(line);
				} else if (line.startsWith("pursued")) {
					if (initialPursuedAgentLocation != null) {
						throw new IllegalArgumentException("Multiple pursued agents not allowed: " + line);
					}
					initialPursuedAgentLocation = getLocationFromConfigLine(line);
				} else if (line.startsWith("pursuer")) {
					initialPursuerAgentLocations.add(getLocationFromConfigLine(line));
				} else if (line.isEmpty() || line.startsWith("//")) {
					// skip empty and commented lines
				} else {
					throw new IllegalArgumentException("Unrecognized environment configuration file line: " + line);
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException("I/O problem loading environment configuration.\n" + getUsage(), ioe);
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("Illegal argument.\n" + getUsage(), iae);
		} catch (Exception e) {
			throw new RuntimeException("Problem loading environment.\n" + getUsage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe2) {
					System.err.println("Problem closing environment configuration file.\n" + ioe2);
				}
			}
		}

		// Construct maze. Note: Input maze coordinates start at (1, 1) in top left corner.
		Maze maze = null;
		try {
			int numRows = mazeRows.size();
			if (numRows == 0) {
				throw new IllegalArgumentException("No maze specified in configuration file!");
			}
			int numCols = mazeRows.get(0).length;
			boolean[][] isWall = new boolean[numRows][numCols];
			for (int y = 1; y <= numRows; ++y) {
				String[] row = mazeRows.get(y - 1);
				if (row.length != numCols) {
					throw new IllegalArgumentException(
						"Row " + y + " (" + row.length + " cols) has different number of cols from row 0 (" + numCols + " cols).");
				}
				
				for (int x = 1; x <= numCols; ++x) {
					isWall[y - 1][x - 1] = "1".equals(row[x - 1]);
				}
			}
			maze = new Maze(isWall, safetyLocation);
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("Illegal argument.\n" + getUsage(), iae);
		} catch (Exception e) {
			throw new RuntimeException("Problem loading environment.\n" + getUsage(), e);
		}

		// Start GUI
		try {
		    new PursuitWorldGUI(maze, initialPursuedAgentLocation, initialPursuerAgentLocations);
		} catch (Exception e) {
			throw new RuntimeException("Problem running game.", e);
		}
	}
	
	protected static String getUsage() {
		return
			"Usage: java PursuitWorldGame <environment file path>"; // TODO (p2) check
	}
	
	/**
	 * 
	 * @param line String containing "(<x>, <y>)" in it; white space is ignored.
	 * @return Location object for first set of coordinates in line.
	 * @throw IllegalArgumentException If can't parse location from line.
	 */
	protected static XYLocation getLocationFromConfigLine(String line) {
		// Get x-coordinate.
		int xCoordinateStart = line.indexOf('(') + 1;
		if (xCoordinateStart == 0) {
			throw new IllegalArgumentException("Not a valid location configuration line: " + line);
		}
		int xCoordinateEnd = line.indexOf(',', xCoordinateStart + 1);
		if (xCoordinateEnd < 0) {
			throw new IllegalArgumentException("Not a valid location configuration line: " + line);
		}
		String xCoordinateStr = line.substring(xCoordinateStart, xCoordinateEnd).trim();
		int xCoordinate;
		try {
			xCoordinate = Integer.parseInt(xCoordinateStr);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(
					"Invalid x-coordinate '" + xCoordinateStr + "' in location configuration line: " + line, nfe);
		}
		
		// Get y-coordinate.
		int yCoordinateStart = xCoordinateEnd + 1;
		int yCoordinateEnd = line.indexOf(')', yCoordinateStart + 1);
		if (yCoordinateEnd < 0) {
			throw new IllegalArgumentException("Not a valid location configuration line: " + line);
		}
		String yCoordinateStr = line.substring(yCoordinateStart, yCoordinateEnd).trim();
		int yCoordinate;
		try {
			yCoordinate = Integer.parseInt(yCoordinateStr);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(
					"Invalid y-coordinate '" + yCoordinateStr + "' in location configuration line: " + line, nfe);
		}

		// Create location object.
		return new XYLocation(xCoordinate, yCoordinate);
	}

}
