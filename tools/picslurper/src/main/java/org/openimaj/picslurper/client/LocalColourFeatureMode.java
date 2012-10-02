package org.openimaj.picslurper.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class LocalColourFeatureMode implements TrendDetectorFeatureExtractor {


	private int blocks_x = 4;
	private int blocks_y = 4;
	private int[] ibins = new int[]{4,4,4};
	private BlockHistogramModel hm;
	public LocalColourFeatureMode() {
		hm = new BlockHistogramModel(blocks_x , blocks_y , ibins );
	}
	@Override
	public List<? extends FeatureVectorProvider<? extends FeatureVector>> extractFeatures(File imageFile) throws IOException {
		MBFImage image = ImageUtilities.readMBF(imageFile);
		if(image.getWidth() < 50){
			throw new IOException("image too small,skipping");
		}
		image.processInplace(new ResizeProcessor(150));
		hm.estimateModel(image);

		FeatureVectorProvider<FeatureVector> fvp = new FeatureVectorProvider<FeatureVector>() {

			@Override
			public FeatureVector getFeatureVector() {
				return hm.toSingleHistogram();
			}
		};

		List<FeatureVectorProvider<FeatureVector>> ret = new ArrayList<FeatureVectorProvider<FeatureVector>>();
		ret.add(fvp);
		return ret ;
	}
	@Override
	public boolean logScale() {
		return false;
	}
	@Override
	public int nDimensions() {
		int mult = blocks_x * blocks_y;
		for (int bindim: ibins) {
			mult *= bindim;
		}
		return mult;
	}

}
