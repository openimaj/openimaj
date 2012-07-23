package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.ColourContrast;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Colour contrast
 * @see ColourContrast
 */
public class ColourContrastExtractor extends GlobalFeatureExtractor {
	@Option(name="--sigma", aliases="-s", required=false, usage="the amount of Gaussian blurring applied prior to segmentation (default 0.5)")
	float sigma = 0.5f;

	@Option(name="--threshold", aliases="-k", required=false, usage="the segmentation threshold (default 500/255)")
	float k = 500f / 255f;

	@Option(name="--min-size", aliases="-m", required=false, usage="the minimum segment size (default 50)")
	int minSize = 50;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		ColourContrast cc = new ColourContrast(sigma, k, minSize);
		image.analyseWith(cc);
		return cc.getFeatureVector();
	}
}
