package org.openimaj.tools.globalfeature.type;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.global.Sharpness;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Sharpness
 * @see Sharpness
 */
public class SharpnessExtractor extends GlobalFeatureExtractor {
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		Sharpness f = new Sharpness(mask);
		Transforms.calculateIntensityNTSC(image).analyseWith(f);
		return f.getFeatureVector();
	}
}
