package org.openimaj.image;

/**
 * Interface for objects that can provide images
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            Type of image provided
 */
public interface ImageProvider<IMAGE extends Image<?, IMAGE>> {
	/**
	 * Get the image that this provider provides.
	 * 
	 * @return the image
	 */
	public IMAGE getImage();
}
