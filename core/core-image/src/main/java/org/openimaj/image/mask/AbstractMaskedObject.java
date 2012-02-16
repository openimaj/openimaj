package org.openimaj.image.mask;

import org.openimaj.image.Image;

/**
 * Abstract base implementation of a {@link MaskedObject}.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <M> The {@link Image} type of the mask.
 */
public abstract class AbstractMaskedObject<M extends Image<?,M>> implements MaskedObject<M> {
	protected M mask;
	
	/**
	 * Default constructor with a <code>null</code> mask.
	 */
	public AbstractMaskedObject() {}
	
	/**
	 * Construct with the given mask.
	 * @param mask the mask.
	 */
	public AbstractMaskedObject(M mask) {
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
