package org.openimaj.image.combiner;

import org.openimaj.image.Image;

/**
 * Interface for classes capable of combining multiple images into
 * one. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <INPUT> Type of input {@link Image}s
 * @param <OUTPUT> Output type of combined {@link Image}
 */
public interface AccumulatingImageCombiner<INPUT extends Image<?,INPUT>, OUTPUT extends Image<?,OUTPUT>> {
	/**
	 * Perform processing that combines the two images into
	 * a single image.
	 * 
	 * @param image the first image
	 */
	public void accumulate(INPUT image);
	
	/**
	 * Combine the accumulated images and produce the output.
	 * @return combined image.
	 */
	public OUTPUT combine();
}
