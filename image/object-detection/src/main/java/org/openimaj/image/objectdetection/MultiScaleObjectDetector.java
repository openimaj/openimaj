package org.openimaj.image.objectdetection;

import org.openimaj.image.Image;
import org.openimaj.image.objectdetection.filtering.DetectionFilter;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Interface describing a multi-scale object detector. Optional methods are
 * provided for controlling the detection size.
 * <p>
 * Any type of Java object can be used to represent a detection; this could be
 * something as simple as a {@link Rectangle} representing the spatial location
 * of the detection, or it could be much more complex.
 * <p>
 * The interface allows for multiple detections to be returned from the input
 * image. If required, these detections could be filtered by a
 * {@link DetectionFilter} as a post-processing operation.
 * 
 * @see ObjectDetector
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The image on which to perform the detection
 * @param <DETECTED_OBJECT>
 *            the type of object representing a detection.
 */
public interface MultiScaleObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT>
		extends
			ObjectDetector<IMAGE, DETECTED_OBJECT>
{
	/**
	 * (Optional operation).
	 * <p>
	 * Set the minimum detection size.
	 * 
	 * @param size
	 *            the minimum detection size.
	 */
	public void setMinimumDetectionSize(int size);

	/**
	 * (Optional operation).
	 * <p>
	 * Set the maximum detection size. A size less than or equal to 0 indicates
	 * there is no maximum size.
	 * 
	 * @param size
	 *            the maximum detection size.
	 */
	public void setMaximumDetectionSize(int size);

	/**
	 * (Optional operation).
	 * <p>
	 * Get the minimum detection size.
	 * 
	 * @return the minimum detection size.
	 * 
	 */
	public int getMinimumDetectionSize();

	/**
	 * (Optional operation).
	 * <p>
	 * Get the maximum detection size. A size less than or equal to 0 indicates
	 * there is no maximum size.
	 * 
	 * @return the maximum detection size.
	 */
	public int getMaximumDetectionSize();
}
