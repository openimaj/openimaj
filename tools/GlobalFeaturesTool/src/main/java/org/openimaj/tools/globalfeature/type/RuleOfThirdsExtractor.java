package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.RuleOfThirds;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Rule of thirds feature
 * @see RuleOfThirds
 */
public class RuleOfThirdsExtractor extends GlobalFeatureExtractor {
	@Option(name="--saliency-sigma", aliases="-sals", required=false, usage="the amount of Gaussian blurring for the saliency estimation (default 1.0)")
	float saliencySigma = 1f;

	@Option(name="--segment-sigma", aliases="-segs", required=false, usage="the amount of Gaussian blurring applied prior to segmentation (default 0.5)")
	float segmenterSigma = 0.5f;

	@Option(name="--threshold", aliases="-k", required=false, usage="the segmentation threshold (default 500/255)")
	float k = 500f / 255f;

	@Option(name="--min-size", aliases="-m", required=false, usage="the minimum segment size (default 50)")
	int minSize = 50;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		RuleOfThirds cc = new RuleOfThirds(saliencySigma, segmenterSigma, k, minSize);
		image.analyseWith(cc);
		return cc.getFeatureVector();
	}
}
