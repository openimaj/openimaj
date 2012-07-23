package org.openimaj.tools.globalfeature.type;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.pixel.statistics.MaskingHistogramModel;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Create a global colour histogram and output a feature
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk) 
 *
 */
public class HistogramExtractor extends GlobalFeatureExtractor {
	@Option(name="--color-space", aliases="-c", usage="Specify colorspace model", required=true)
	ColourSpace converter;
	
	@Argument(required=true, usage="Number of bins per dimension")
	List<Integer> bins = new ArrayList<Integer>();
	
	/**
	 * The histogram model 
	 */
	public HistogramModel hm;
	
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
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
