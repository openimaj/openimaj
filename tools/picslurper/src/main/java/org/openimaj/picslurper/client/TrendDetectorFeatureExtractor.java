package org.openimaj.picslurper.client;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FeatureVectorProvider;

public interface TrendDetectorFeatureExtractor {
	public List<? extends FeatureVectorProvider<? extends FeatureVector>> extractFeatures(File imageFile) throws IOException;

	public boolean logScale();

	public int nDimensions();
}
