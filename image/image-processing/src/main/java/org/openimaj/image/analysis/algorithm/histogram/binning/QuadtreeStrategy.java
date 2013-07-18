package org.openimaj.image.analysis.algorithm.histogram.binning;

import java.util.List;

import org.openimaj.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import org.openimaj.image.pixel.sampling.QuadtreeSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * A {@link SpatialBinningStrategy} that extracts histograms from regions
 * defined by a fixed depth quadtree overlayed over the sampling region and
 * concatenates them together.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class QuadtreeStrategy implements SpatialBinningStrategy {
	int nlevels;

	/**
	 * Construct with the given quadtree depth
	 * 
	 * @param nlevels
	 *            quadtree depth
	 */
	public QuadtreeStrategy(int nlevels) {
		this.nlevels = nlevels;
	}

	@Override
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		final QuadtreeSampler sampler = new QuadtreeSampler(region, nlevels);
		final int blockSize = binnedData.getNumBins();
		final List<Rectangle> rects = sampler.allRectangles();

		if (output == null || output.values.length != blockSize * rects.size())
			output = new Histogram(blockSize * rects.size());

		final Histogram tmp = new Histogram(blockSize);
		for (int i = 0; i < rects.size(); i++) {
			final Rectangle r = rects.get(i);

			binnedData.computeHistogram(r, tmp);

			System.arraycopy(tmp.values, 0, output.values, blockSize * i, blockSize);
		}

		return output;
	}
}
