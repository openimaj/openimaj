package org.openimaj.tools.globalfeature.type;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.pixel.statistics.MaskingBlockHistogramModel;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Local (block-based) pixel histograms
 * @see BlockHistogramModel
 */
public class LocalHistogramExtractor extends GlobalFeatureExtractor {
	@Option(name="--color-space", aliases="-c", usage="Specify colorspace model", required=true)
	ColourSpace converter;
	
	@Option(name="--blocks-x", aliases="-bx", usage="Specify number of blocks in x-direction", required=true)
	int blocks_x;
	
	@Option(name="--blocks-y", aliases="-by", usage="Specify number of blocks in y-direction", required=true)
	int blocks_y;
	
	@Argument(required=true, usage="Number of bins per dimension")
	List<Integer> bins = new ArrayList<Integer>();
	
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		MBFImage converted = converter.convert(image);
		
		if (converted.numBands() != bins.size()) {
			throw new RuntimeException("Incorrect number of dimensions - recieved " + bins.size() +", expected " + converted.numBands() +".");
		}
		
		int [] ibins = new int[bins.size()];
		for (int i=0; i<bins.size(); i++)
			ibins[i] = bins.get(i);
		
		BlockHistogramModel hm = null;
		if (mask == null)
			hm = new BlockHistogramModel(blocks_x, blocks_y, ibins);
		else
			hm = new MaskingBlockHistogramModel(mask, blocks_x, blocks_y, ibins);
		
		hm.estimateModel(converted);
		return hm.toSingleHistogram();
	}
}
