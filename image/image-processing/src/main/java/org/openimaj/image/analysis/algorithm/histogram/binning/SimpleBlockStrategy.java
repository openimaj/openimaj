package org.openimaj.image.analysis.algorithm.histogram.binning;

import org.openimaj.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import org.openimaj.image.pixel.sampling.RectangleSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * A {@link SpatialBinningStrategy} that extracts histograms from a number of
 * equally-sized, non-overlapping within the sample region and concatenates them
 * together. Each sub-histogram is L2 normalised.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SimpleBlockStrategy implements SpatialBinningStrategy {
	int numBlocksX;
	int numBlocksY;

	/**
	 * Construct with the given number of blocks in both the x and y directions.
	 * 
	 * @param numBlocks
	 *            number of blocks in each direction
	 */
	public SimpleBlockStrategy(int numBlocks) {
		this(numBlocks, numBlocks);
	}

	/**
	 * Construct with the given number of blocks in the x and y directions.
	 * 
	 * @param numBlocksX
	 *            number of blocks in the x directions
	 * @param numBlocksY
	 *            number of blocks in the y directions
	 */
	public SimpleBlockStrategy(int numBlocksX, int numBlocksY) {
		this.numBlocksX = numBlocksX;
		this.numBlocksY = numBlocksY;
	}

	@Override
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		final float dx = region.width / numBlocksX;
		final float dy = region.height / numBlocksY;
		final int blockSize = binnedData.getNumBins();

		if (output == null || output.values.length != blockSize * numBlocksX * numBlocksY)
			output = new Histogram(blockSize * numBlocksX * numBlocksY);

		final RectangleSampler rs = new RectangleSampler(region, dx, dy, dx, dy);
		int block = 0;
		final Histogram tmp = new Histogram(blockSize);

		for (final Rectangle r : rs) {
			binnedData.computeHistogram(r, tmp);
			tmp.normaliseL2();

			System.arraycopy(tmp.values, 0, output.values, blockSize * block, blockSize);
			block++;
		}

		return output;
	}
}
