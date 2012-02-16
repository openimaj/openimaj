package org.openimaj.image.processor;

import org.openimaj.image.Image;

/**
 * Abstract base implementation of a {@link MaskedImageProcessor}.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <I> The {@link Image} type of the image to be processed.
 * @param <M> The {@link Image} type of the mask.
 */
public abstract class AbstractMaskedImageProcessor <I extends Image<?,I>, M extends Image<?,M>> implements MaskedImageProcessor<I, M> {
	protected M mask;
	
	/**
	 * Default constructor with a <code>null</code> mask.
	 */
	public AbstractMaskedImageProcessor() {}
	
	/**
	 * Construct with the given mask.
	 * @param mask the mask.
	 */
	public AbstractMaskedImageProcessor(M mask) {
		this.mask = mask;
	}
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.MaskedImageProcessor#getMask()
	 */
	@Override
	public M getMask() {
		return mask;
	}
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.MaskedImageProcessor#setMask(org.openimaj.image.Image)
	 */
	@Override
	public void setMask(M mask) {
		this.mask = mask;
	}
}
