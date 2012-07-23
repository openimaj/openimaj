package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.global.SharpPixelProportion;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Sharp pixel proportion
 * @see SharpPixelProportion
 */
public class SharpPixelProportionExtractor extends GlobalFeatureExtractor {
	@Option(name="--threshold", aliases="-t", required=false, usage="frequency power threshold (default 2.0)")
	float thresh = 2f;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		SharpPixelProportion cc = new SharpPixelProportion(thresh);
		Transforms.calculateIntensityNTSC(image).analyseWith(cc);
		return cc.getFeatureVector();
	}
}
