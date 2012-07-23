package org.openimaj.tools.globalfeature.type;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.HueStats;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Hue stats
 * @see HueStats
 */
public class HueStatsExtractor extends GlobalFeatureExtractor {
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		HueStats f = new HueStats(mask);
		image.analyseWith(f);
		return f.getFeatureVector();
	}
}
