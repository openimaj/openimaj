package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.LuoSimplicity;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Luo's simplicity feature
 * @see LuoSimplicity
 */
public class LuoSimplicityExtractor extends GlobalFeatureExtractor {
	@Option(name="--bins-per-band", aliases="-bpb", required=false, usage="Number of bins to split the R, G and B bands into when constructing the histogram (default 16)")
	int binsPerBand = 16;

	@Option(name="--gamma", required=false, usage="percentage threshold on the max value of the histogram for counting high-valued bins (default 0.01)")
	float gamma = 0.01f;

	@Option(name="--no-box", required=false, usage="use the actual predicted foreground/background pixels rather than their bounding box (default false)")
	boolean noBoxMode = false;

	@Option(name="--alpha", required=false, usage="alpha parameter for determining bounding box size based on the energy ratio (default 0.9)")
	float alpha = 0.9f;

	@Option(name="--max-kernel-size", required=false, usage="maximum smoothing kernel size (default 50)")
	int maxKernelSize;

	@Option(name="--kernel-size-step", required=false, usage="step size to increment smoothing kernel by (default 1)")
	int kernelSizeStep = 1;

	@Option(name="--num-bins", required=false, usage="number of bins for the gradiant histograms (default 41)")
	int nbins = 41;

	@Option(name="--window-size", required=false, usage="window size for estimating depth of field (default 3)")
	int windowSize = 3;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		LuoSimplicity cc = new LuoSimplicity(binsPerBand, gamma, !noBoxMode, alpha, maxKernelSize, kernelSizeStep, nbins, windowSize);
		image.analyseWith(cc);
		return cc.getFeatureVector();
	}
}
