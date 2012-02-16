package org.openimaj.image.combiner;

import org.openimaj.image.Image;

/**
 * Interface for classes capable of combining two images into one. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <I1> Type of first {@link Image}
 * @param <I2> Type of second {@link Image}
 * @param <O> Output type of combined {@link Image}
 */
public interface ImageCombiner<I1 extends Image<?,I1>, I2 extends Image<?,I2>, O extends Image<?,O>> {
	/**
	 * Perform processing that combines the two images into
	 * a single image.
	 * 
	 * @param firstImage the first image
	 * @param secondImage the second image
	 * @return the output image
	 */
	public O combine(I1 firstImage, I2 secondImage);
}
