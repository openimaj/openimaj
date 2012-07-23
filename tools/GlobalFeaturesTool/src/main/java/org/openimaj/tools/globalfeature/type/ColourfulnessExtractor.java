package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.Colorfulness;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Colorfulness
 * @see Colorfulness
 */
public class ColourfulnessExtractor extends GlobalFeatureExtractor {
	@Option(name="--classes", usage="output class value (i.e. extremely, ..., not) instead of actual value.")
	boolean classMode = false;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		Colorfulness f = new Colorfulness();

		if (mask == null)
			image.analyseWith(f);
		else
			image.analyseWithMasked(mask, f);

		if (classMode)
			return f.getColorfulnessAttribute().getFeatureVector();
		return f.getFeatureVector();
	}
}
