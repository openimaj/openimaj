package org.openimaj.image.processing.background;

import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Basic background subtraction
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <I>
 */
public class BasicBackgroundSubtract<I extends Image<?,I>> implements ImageProcessor<I> {
	I background;

	/**
	 * Default constructor
	 */
	public BasicBackgroundSubtract() {
		//do nothing
	}
	/**
	 * Default constructor
	 */
	public BasicBackgroundSubtract(I background) {
		this.background = background;
	}
	
	/**
	 * Set the background image
	 */
	public void setBackground(I background) {
		this.background = background;
	}
	
	/**
	 * Process the input by subtracting the background
	 * 
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image, org.openimaj.image.Image<?,?>[])
	 */
	@Override
	public void processImage(I image, Image<?, ?>... otherimages) {
		image.subtractInline(background);
	}
}
