package org.openimaj.tools.globalfeature.type;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.Naturalness;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Naturalness
 * @see Naturalness
 */
public class NaturalnessExtractor extends GlobalFeatureExtractor {
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		Naturalness f = new Naturalness(mask);
		image.analyseWith(f);
		return f.getFeatureVector();
	}
}
