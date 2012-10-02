package org.openimaj.picslurper.client;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.util.filter.FilterUtils;

public class SIFTTrendFeatureMode implements TrendDetectorFeatureExtractor {


	private DoGSIFTEngine engine;

	public SIFTTrendFeatureMode() {
		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
	}
	@Override
	public List<Keypoint> extractFeatures(File imageFile) throws IOException {
		final ByteEntropyFilter filter = new ByteEntropyFilter();

		final FImage image = ResizeProcessor.resizeMax(ImageUtilities.readF(imageFile), 150);
		final List<Keypoint> features = engine.findFeatures(image);
		return FilterUtils.filter(features, filter);
	}
	@Override
	public boolean logScale() {
		return true;
	}
	@Override
	public int nDimensions() {
		return 128;
	}
}
