package org.openimaj.image.feature.dense.gradient.binning;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.BinnedImageHistogramAnalyser;
import org.openimaj.image.pixel.sampling.RectangleSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

public class SimpleRectangularStrategy implements SpatialBinningStrategy {
	int numBlocksX;
	int numBlocksY;
	boolean useMagnitudes;

	@Override
	public Histogram extract(BinnedImageHistogramAnalyser binnedData, FImage magnitudes, Rectangle region) {
		final float dx = region.width / numBlocksX;
		final float dy = region.height / numBlocksY;

		final RectangleSampler rs = new RectangleSampler(region, dx, dy, dx, dy);
		int block = 0;
		final Histogram[] histograms = new Histogram[numBlocksX * numBlocksY];

		if (useMagnitudes) {
			for (final Rectangle r : rs) {
				histograms[block++] = binnedData.computeHistogram(r, magnitudes);
			}
		} else {
			for (final Rectangle r : rs) {
				histograms[block++] = binnedData.computeHistogram(r);
			}
		}

		return new Histogram(histograms);
	}
}
