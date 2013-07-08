/**
 *
 */
package org.openimaj.vis.general;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 5 Jul 2013
 */
public interface CameraPositionProvider
{
	/**
	 * 	Returns the camera position (x, y, z) position,
	 * 	(x, y, z) look at position, (x, y, z) camera normal (up).
	 *
	 *	@return The camera position as 9 floats.
	 */
	public float[] getCameraPosition();
}
