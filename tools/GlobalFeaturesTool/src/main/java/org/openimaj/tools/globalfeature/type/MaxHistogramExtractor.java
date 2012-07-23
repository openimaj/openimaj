package org.openimaj.tools.globalfeature.type;

import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.tools.globalfeature.GlobalFeatureType;
import org.openimaj.util.array.ArrayUtils;

/**
 * Using a pixel histogram (see {@link GlobalFeatureType#HISTOGRAM}) find
 * the maximum bin. This can be interpreted as the image's dominant colour.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class MaxHistogramExtractor extends HistogramExtractor {
	MaxHistogramExtractor(ColourSpace rgb, List<Integer> asList) {
		this.converter = rgb;
		this.bins = asList;
	}

	/**
	 * Default constructor
	 */
	public MaxHistogramExtractor() {

	}

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		FeatureVector fv = super.extract(image, mask);
		double[] vals = fv.asDoubleVector();
		int index = ArrayUtils.maxIndex(vals);
		return new FloatFV(hm.colourAverage(index));
	}
}
