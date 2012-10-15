package org.openimaj.image.objectdetection.filtering;

import java.util.List;

import org.openimaj.image.objectdetection.ObjectDetector;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Interface describing an algorithm capable of filtering detections from a
 * {@link ObjectDetector}. Typically this might mean finding the biggest
 * detection response, or grouping multiple responses into smaller sets.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IN>
 *            Input type (often {@link Rectangle}s).
 * @param <OUT>
 *            Output type (often {@link Rectangle}s).
 */
public interface DetectionFilter<IN, OUT> {
	/**
	 * Perform the filtering operation on the input and return the output.
	 * 
	 * @param input
	 *            the input detections
	 * @return the output detections
	 */
	public List<OUT> apply(List<IN> input);
}
