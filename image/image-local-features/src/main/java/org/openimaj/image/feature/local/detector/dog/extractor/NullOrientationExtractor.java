package org.openimaj.image.feature.local.detector.dog.extractor;

import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.extraction.GradientScaleSpaceImageExtractorProperties;

/**
 * An orientation extractor that always returns 0. Useful for
 * non-rotation invariant features.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class NullOrientationExtractor extends AbstractDominantOrientationExtractor {
	private static final float[] fv = {0};
	
	@Override
	public float[] extractFeatureRaw(GradientScaleSpaceImageExtractorProperties<FImage> props) {
		return fv;
	}
}
