package org.openimaj.math.util.distance;

/**
 * Interface for a class that can perform a check on the
 * distance between a pair of items to see if they matches.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public interface DistanceCheck {
	/**
	 * Test the distance
	 * @param distance the distance
	 * @return true if the distance is small enough for a match, false otherwise
	 */
	public boolean check(double distance);
}