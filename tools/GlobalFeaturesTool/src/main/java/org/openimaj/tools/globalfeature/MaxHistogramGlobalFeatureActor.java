package org.openimaj.tools.globalfeature;

import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.util.array.ArrayUtils;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class MaxHistogramGlobalFeatureActor extends HistogramGlobalFeatureActor {

	/**
	 * @param converter
	 * @param bins
	 */
	public MaxHistogramGlobalFeatureActor(ColourSpace converter,List<Integer> bins) {
		super(converter, bins);
		// TODO Auto-generated constructor stub
	}

	@Override
	public FeatureVector enact(MBFImage image, FImage mask) {
		FeatureVector fv = super.enact(image, mask);
		double[] vals = fv.asDoubleVector();
		int index = ArrayUtils.maxIndex(vals);
		return new FloatFV(hm.colourAverage(index));
	}

}
