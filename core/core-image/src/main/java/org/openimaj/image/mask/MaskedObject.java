package org.openimaj.image.mask;

import org.openimaj.image.Image;

/**
 * An interface for objects with an image mask.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <M> The {@link Image} type of the mask.
 */
public interface MaskedObject<M extends Image<?,M>> {
	/**
	 * @return the mask image.
	 */
	public M getMask();
	
	/**
	 * Set the mask image.
	 * @param mask the mask.
	 */
	public void setMask(M mask);
}
