package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.global.HorizontalIntensityDistribution;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Horizontal intensity distribution
 * @see HorizontalIntensityDistribution
 */
public class HorizontalIntensityDistributionExtractor extends GlobalFeatureExtractor {
	@Option(name="--num-bins", aliases="-n", required=false, usage="number of histogram bins (default 64)")
	int nbins = 64;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		HorizontalIntensityDistribution cc = new HorizontalIntensityDistribution(nbins);
		Transforms.calculateIntensityNTSC(image).analyseWith(cc);
		return cc.getFeatureVector();
	}
}
