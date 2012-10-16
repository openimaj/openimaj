package org.openimaj.image.objectdetection;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Abstract base class for implementations of {@link MultiScaleObjectDetector}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The image on which to perform the detection
 * @param <DETECTED_OBJECT>
 *            the type of object representing a detection.
 */
public abstract class AbstractMultiScaleObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT>
		implements
			MultiScaleObjectDetector<IMAGE, DETECTED_OBJECT>
{
	protected Rectangle roi;
	protected int minSize = 0;
	protected int maxSize = 0;

	/**
	 * Construct with the initial minimum and maximum size set to zero.
	 */
	protected AbstractMultiScaleObjectDetector() {
	}

	/**
	 * Construct with the given initial minimum and maximum detection sizes.
	 * 
	 * @param minSize
	 *            minimum size
	 * @param maxSize
	 *            maximum size
	 */
	protected AbstractMultiScaleObjectDetector(int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	@Override
	public void setROI(Rectangle roi) {
		this.roi = roi;
	}

	@Override
	public void setMinimumDetectionSize(int size) {
		this.minSize = size;
	}

	@Override
	public void setMaximumDetectionSize(int size) {
		this.maxSize = size;
	}

	@Override
	public int getMinimumDetectionSize() {
		return minSize;
	}

	@Override
	public int getMaximumDetectionSize() {
		return maxSize;
	}
}
