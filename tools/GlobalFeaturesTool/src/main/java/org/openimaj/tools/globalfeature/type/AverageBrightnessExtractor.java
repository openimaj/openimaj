package org.openimaj.tools.globalfeature.type;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.AvgBrightness;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Average brightness
 * @see AvgBrightness
 */
public class AverageBrightnessExtractor extends GlobalFeatureExtractor {
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		AvgBrightness f = new AvgBrightness(mask);
		return f.getFeatureVector();
	}
}
