package org.openimaj.image.feature.dense.gradient.binning;

import org.openimaj.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import org.openimaj.image.analysis.algorithm.histogram.binning.SpatialBinningStrategy;
import org.openimaj.image.pixel.sampling.RectangleSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

public class SimpleRectangularStrategy implements SpatialBinningStrategy {
	int numBlocksX;
	int numBlocksY;

	@Override
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		final float dx = region.width / numBlocksX;
		final float dy = region.height / numBlocksY;

		final RectangleSampler rs = new RectangleSampler(region, dx, dy, dx, dy);
		int block = 0;
		final Histogram[] histograms = new Histogram[numBlocksX * numBlocksY];

		for (final Rectangle r : rs) {
			histograms[block++] = binnedData.computeHistogram(r);
		}

		return new Histogram(histograms);
	}
}
