package org.openimaj.vis.general;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> the pixel type returned
 *
 */
public interface StrokeColourProvider<T> {

	/**
	 * @param row
	 * @return Given an index provide a colour
	 */
	T getStrokeColour(int row);
}
