package org.openimaj.image.feature.dense.gradient;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.algorithm.BinnedImageHistogramAnalyser;
import org.openimaj.image.pixel.sampling.RectanglePyramidSampler;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

public class PyramidHistogramOfGradients implements ImageAnalyser<FImage> {
	BinnedImageHistogramAnalyser histExtractor;
	FImage magnitudes;
	int nlevels;

	@Override
	public void analyseImage(FImage image) {
		final FImage edges = image.process(new CannyEdgeDetector());
		final FImageGradients gmo = FImageGradients.getGradientMagnitudesAndOrientations(image);

		this.magnitudes = gmo.magnitudes.multiplyInplace(edges);
		this.histExtractor.analyseImage(gmo.orientations);
	}

	public Histogram extractFeature(Rectangle rect) {
		final RectanglePyramidSampler sampler = new RectanglePyramidSampler(rect, nlevels);
		final List<float[]> parts = new ArrayList<float[]>();
		final Histogram hist = new Histogram(0);

		for (final Rectangle r : sampler) {
			hist.combine(histExtractor.computeHistogram(r, magnitudes));
		}

		return hist;
	}
}
