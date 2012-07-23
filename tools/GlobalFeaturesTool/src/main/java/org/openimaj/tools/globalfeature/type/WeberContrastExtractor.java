package org.openimaj.tools.globalfeature.type;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.global.WeberContrast;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Weber constrast
 * @see WeberContrast
 */
public class WeberContrastExtractor extends GlobalFeatureExtractor {
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		WeberContrast cc = new WeberContrast();
		Transforms.calculateIntensityNTSC(image).analyseWith(cc);
		return cc.getFeatureVector();
	}
}
