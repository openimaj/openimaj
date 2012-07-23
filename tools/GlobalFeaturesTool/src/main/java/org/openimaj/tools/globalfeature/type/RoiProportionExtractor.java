package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.ROIProportion;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * ROI proportion
 * @see ROIProportion
 */
public class RoiProportionExtractor extends GlobalFeatureExtractor {
	@Option(name="--saliency-sigma", aliases="-sals", required=false, usage="the amount of Gaussian blurring for the saliency estimation (default 1.0)")
	float saliencySigma = 1f;

	@Option(name="--segment-sigma", aliases="-segs", required=false, usage="the amount of Gaussian blurring applied prior to segmentation (default 0.5)")
	float segmenterSigma = 0.5f;

	@Option(name="--threshold", aliases="-k", required=false, usage="the segmentation threshold (default 500/255)")
	float k = 500f / 255f;

	@Option(name="--min-size", aliases="-m", required=false, usage="the minimum segment size (default 50)")
	int minSize = 50;

	@Option(name="--alpha", aliases="-a", required=false, usage="The proportion of the maximum saliency value at which we choose the ROI components (default 0.67)")
	float alpha = 0.67f;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		ROIProportion cc = new ROIProportion(saliencySigma, segmenterSigma, k, minSize, alpha);
		image.analyseWith(cc);
		return cc.getFeatureVector();
	}
}
