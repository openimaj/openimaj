package org.openimaj.image.feature.local.detector.dog.extractor;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.extraction.FeatureExtractor;
import org.openimaj.image.feature.local.extraction.GradientScaleSpaceImageExtractorProperties;

/**
 * Abstract superclass for objects capable of finding the dominant
 * orientation of a point in scale-space.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public abstract class AbstractDominantOrientationExtractor implements FeatureExtractor<FloatFV, GradientScaleSpaceImageExtractorProperties<FImage>> {
	/**
	 * Find the dominant orientations
	 * 
	 * @param props Properties describing the interest point in scale space.
	 * @return an FloatFV containing the angles of the dominant orientations [-PI to PI].
	 */
	@Override
	public FloatFV[] extractFeature(GradientScaleSpaceImageExtractorProperties<FImage> props) {
		return new FloatFV[] { new FloatFV(extractFeatureRaw(props)) };
	}

	/**
	 * Find the dominant orientations.
	 * 
	 * @param properties Properties describing the interest point in scale space.
	 * @return an array of the angles of the dominant orientations [-PI to PI].
	 */
	public abstract float[] extractFeatureRaw(GradientScaleSpaceImageExtractorProperties<FImage> props);
}
