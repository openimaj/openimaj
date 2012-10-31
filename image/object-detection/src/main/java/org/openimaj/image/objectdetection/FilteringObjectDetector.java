package org.openimaj.image.objectdetection;

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.objectdetection.filtering.DetectionFilter;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * An {@link ObjectDetector} that wraps another {@link ObjectDetector} and
 * performs filtering with a {@link DetectionFilter}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            Type of image in which the detection happens
 * @param <DETECTED_OBJECT>
 *            The type of object emitted by the inner detector
 * @param <FILTERED_OBJECT>
 *            The type of object emitted by the filter
 */
public class FilteringObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT, FILTERED_OBJECT>
		implements
		ObjectDetector<IMAGE, FILTERED_OBJECT>
{
	private ObjectDetector<IMAGE, DETECTED_OBJECT> detector;
	private DetectionFilter<DETECTED_OBJECT, FILTERED_OBJECT> filter;

	/**
	 * Construct with the given detector and filter.
	 * 
	 * @param detector
	 *            the detector
	 * @param filter
	 *            the filter
	 */
	public FilteringObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector,
			DetectionFilter<DETECTED_OBJECT, FILTERED_OBJECT> filter)
	{
		super();
		this.detector = detector;
		this.filter = filter;
	}

	@Override
	public List<FILTERED_OBJECT> detect(IMAGE image) {
		return filter.apply(detector.detect(image));
	}

	@Override
	public void setROI(Rectangle roi) {
		detector.setROI(roi);
	}
}
