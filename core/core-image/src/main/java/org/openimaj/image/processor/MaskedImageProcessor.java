package org.openimaj.image.processor;

import org.openimaj.image.Image;

/**
 * An {@link ImageProcessor} that can be "masked" to perform
 * processing only on a sub-part of the image.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <I> The {@link Image} type of the image to be processed.
 * @param <M> The {@link Image} type of the mask.
 */
public interface MaskedImageProcessor<I extends Image<?,I>, M extends Image<?,M>> extends ImageProcessor<I> {
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
