package org.openimaj.image.objectdetection;

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.objectdetection.filtering.DetectionFilter;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Interface describing a basic object detector for images. Any type of Java
 * object can be used to represent a detection; this could be something as
 * simple as a {@link Rectangle} representing the spatial location of the
 * detection, or it could be much more complex.
 * <p>
 * The interface allows for multiple detections to be returned from the input
 * image. If required, these detections could be filtered by a
 * {@link DetectionFilter} as a post-processing operation.
 * <p>
 * This interface is implicitly for single-scale detection. If your detector
 * works at multiple scales, then implementing the
 * {@link MultiScaleObjectDetector} would be a better idea.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The image on which to perform the detection
 * @param <DETECTED_OBJECT>
 *            the type of object representing a detection.
 */
public interface ObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT> {
	/**
	 * Detect objects in the given image and return representations of them.
	 * Representations often cover things like the spatial location in the image
	 * of the detected object, however, this is implementation defined.
	 * <p>
	 * If no objects are detected, <code>null</code> or an empty list may be
	 * returned.
	 * 
	 * @param image
	 *            the image to detect the object in.
	 * @return a list of detections or <code>null</code> if none are found.
	 */
	public List<DETECTED_OBJECT> detect(IMAGE image);

	/**
	 * (Optional operation).
	 * <p>
	 * Set the region of interest within the image to search for objects.
	 * Setting to <code>null</code> means to search the whole image.
	 * 
	 * @param roi
	 *            the region of interest
	 */
	public void setROI(Rectangle roi);
}
