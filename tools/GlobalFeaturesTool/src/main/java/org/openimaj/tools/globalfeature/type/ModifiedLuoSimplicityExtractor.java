package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.ModifiedLuoSimplicity;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Modified version of Luo's simplicity feature
 * @see ModifiedLuoSimplicity
 */
public class ModifiedLuoSimplicityExtractor extends GlobalFeatureExtractor {
	@Option(name="--bins-per-band", aliases="-bpb", required=false, usage="Number of bins to split the R, G and B bands into when constructing the histogram (default 16)")
	int binsPerBand = 16;

	@Option(name="--gamma", required=false, usage="percentage threshold on the max value of the histogram for counting high-valued bins (default 0.01)")
	float gamma = 0.01f;

	@Option(name="--no-box", required=false, usage="use the actual predicted foreground/background pixels rather than their bounding box (default false)")
	boolean noBoxMode = false;

	@Option(name="--alpha", aliases="-a", required=false, usage="The proportion of the maximum saliency value at which we choose the ROI components (default 0.67)")
	float alpha = 0.67f;

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
		ModifiedLuoSimplicity cc = new ModifiedLuoSimplicity(binsPerBand, gamma, !noBoxMode, alpha, saliencySigma, segmenterSigma, k, minSize);
		image.analyseWith(cc);
		return cc.getFeatureVector();
	}
}
