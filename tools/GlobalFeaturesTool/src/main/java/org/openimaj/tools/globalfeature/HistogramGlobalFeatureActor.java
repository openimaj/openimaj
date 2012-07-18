package org.openimaj.tools.globalfeature;

import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.pixel.statistics.MaskingHistogramModel;

/**
 * Create a global colour histogram and output a feature
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class HistogramGlobalFeatureActor implements GlobalFeatureActor{

	private ColourSpace converter;
	private List<Integer> bins;
	private HistogramModel hm;

	/**
	 * The colour space of the histogram and number of bins
	 * @param converter
	 * @param bins
	 */
	public HistogramGlobalFeatureActor(ColourSpace converter, List<Integer> bins) {
		this.converter = converter;
		this.bins = bins;
	}
	
	@Override
	public FeatureVector enact(MBFImage image, FImage mask){
		MBFImage converted = converter.convert(image);
		
		if (converted.numBands() != bins.size()) {
			throw new RuntimeException("Incorrect number of dimensions - recieved " + bins.size() +", expected " + converted.numBands() +".");
		}
		
		int [] ibins = new int[bins.size()];
		for (int i=0; i<bins.size(); i++)
			ibins[i] = bins.get(i);
		
		hm = null; 
		if (mask == null)
			hm = new HistogramModel(ibins);
		else 
			hm = new MaskingHistogramModel(mask, ibins);
		
		hm.estimateModel(converted);
		return hm.histogram;
	}

}
