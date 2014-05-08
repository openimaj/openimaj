package org.openimaj.experiment.gmm.retrieval;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.util.function.Function;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DoGSiftFeatureExtractor implements Function<FImage,  LocalFeatureList<? extends LocalFeature<?,? extends FeatureVector>>  >{
	
	public LocalFeatureList<? extends LocalFeature<?,? extends FeatureVector>> apply(FImage img) {
		DoGSIFTEngine dsift = new DoGSIFTEngine();
		return dsift.findFeatures(img);
	}
}
