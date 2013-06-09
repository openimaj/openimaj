package org.openimaj.image.feature.dense.gradient.dsift;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FTriangleFilter;

/**
 * Implementation of an approximate dense SIFT feature extractor. Extracts
 * approximate upright SIFT features at a single scale on a grid. Implementation
 * is approximate because instead of using an exact Gaussian weighting, samples
 * are weighted using a flat windowing function for speed, and then after
 * accumulation are re-weighted by the average of the Gaussian window over the
 * spatial support of the sampling region. The end result is that the extracted
 * features are similar to the exact dense SIFT implementation, but computation
 * is much faster.
 * <p>
 * Implementation directly based on the <a
 * href="http://www.vlfeat.org/api/dsift.html#dsift-usage">VLFeat extractor</a>.
 * <p>
 * <b>Implementation Notes</b>. The analyser is not thread-safe, however, it is
 * safe to reuse the analyser. In multi-threaded environments, a separate
 * instance must be made for each thread. Internally, this implementation
 * allocates memory for the gradient images, and if possible re-uses these
 * between calls. Re-use requires that the input image is the same size between
 * calls to the analyser.
 * 
 * @see "http://www.vlfeat.org/api/dsift.html#dsift-usage"
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ApproximateDenseSIFT extends DenseSIFT {
	/**
	 * Construct with the default configuration: standard SIFT geometry (4x4x8),
	 * 5px x 5px spatial bins, 5px step size, gaussian window size of 2 and
	 * value threshold of 0.2.
	 */
	public ApproximateDenseSIFT() {
		super();
	}

	/**
	 * Construct with the given step size (for both x and y) and binSize. All
	 * other values are the defaults.
	 * 
	 * @param step
	 *            the step size
	 * @param binSize
	 *            the spatial bin size
	 */
	public ApproximateDenseSIFT(int step, int binSize) {
		super(step, binSize);
	}

	/**
	 * Construct with the given configuration. The gaussian window size is set
	 * to 2, and value threshold to 0.2.
	 * 
	 * @param stepX
	 *            step size in x direction
	 * @param stepY
	 *            step size in y direction
	 * @param binWidth
	 *            width of spatial bins
	 * @param binHeight
	 *            height of spatial bins
	 * @param numBinsX
	 *            number of bins in x direction for each descriptor
	 * @param numBinsY
	 *            number of bins in y direction for each descriptor
	 * @param numOriBins
	 *            number of orientation bins for each descriptor
	 */
	public ApproximateDenseSIFT(int stepX, int stepY, int binWidth, int binHeight, int numBinsX, int numBinsY,
			int numOriBins)
	{
		super(stepX, stepY, binWidth, binHeight, numBinsX, numBinsY, numOriBins);
	}

	/**
	 * Construct with the given configuration. The value threshold is set to
	 * 0.2.
	 * 
	 * @param stepX
	 *            step size in x direction
	 * @param stepY
	 *            step size in y direction
	 * @param binWidth
	 *            width of spatial bins
	 * @param binHeight
	 *            height of spatial bins
	 * @param numBinsX
	 *            number of bins in x direction for each descriptor
	 * @param numBinsY
	 *            number of bins in y direction for each descriptor
	 * @param numOriBins
	 *            number of orientation bins for each descriptor
	 * @param gaussianWindowSize
	 *            the size of the gaussian weighting window
	 */
	public ApproximateDenseSIFT(int stepX, int stepY, int binWidth, int binHeight, int numBinsX, int numBinsY,
			int numOriBins,
			float gaussianWindowSize)
	{
		super(stepX, stepY, binWidth, binHeight, numBinsX, numBinsY, numOriBins, gaussianWindowSize);
	}

	/**
	 * Construct with the given configuration. The value threshold is set to
	 * 0.2.
	 * 
	 * @param stepX
	 *            step size in x direction
	 * @param stepY
	 *            step size in y direction
	 * @param binWidth
	 *            width of spatial bins
	 * @param binHeight
	 *            height of spatial bins
	 * @param numBinsX
	 *            number of bins in x direction for each descriptor
	 * @param numBinsY
	 *            number of bins in y direction for each descriptor
	 * @param numOriBins
	 *            number of orientation bins for each descriptor
	 * @param gaussianWindowSize
	 *            the size of the gaussian weighting window
	 * @param valueThreshold
	 *            the threshold for clipping features
	 */
	public ApproximateDenseSIFT(int stepX, int stepY, int binWidth, int binHeight, int numBinsX, int numBinsY,
			int numOriBins,
			float gaussianWindowSize, float valueThreshold)
	{
		super(stepX, stepY, binWidth, binHeight, numBinsX, numBinsY, numOriBins, gaussianWindowSize, valueThreshold);
	}

	private float computeWindowMean(int binSize, int numBins, int binIndex, double windowSize)
	{
		final float delta = binSize * (binIndex - 0.5F * (numBins - 1));
		/* float sigma = 0.5F * ((numBins - 1) * binSize + 1) ; */
		final float sigma = binSize * (float) windowSize;
		int x;

		float acc = 0.0f;
		for (x = -binSize + 1; x <= +binSize - 1; ++x) {
			final float z = (x - delta) / sigma;
			acc += ((binIndex >= 0) ? (float) Math.exp(-0.5F * z * z) : 1.0F);
		}
		return acc /= (2 * binSize - 1);
	}

	@Override
	protected void extractFeatures()
	{
		final int frameSizeX = binWidth * (numBinsX - 1) + 1;
		final int frameSizeY = binHeight * (numBinsY - 1) + 1;

		for (int bint = 0; bint < numOriBins; bint++) {
			final FImage conv = data.gradientMagnitudes[bint].process(new FTriangleFilter(binWidth, binHeight));
			final float[][] src = conv.pixels;

			for (int biny = 0; biny < numBinsY; biny++) {

				// This approximate version of DSIFT does not use a proper
				// Gaussian weighting scheme for the gradients that are
				// accumulated on the spatial bins. Instead each spatial bins is
				// accumulated based on the triangular kernel only, equivalent
				// to bilinear interpolation plus a flat, rather than Gaussian,
				// window. Eventually, however, the magnitude of the spatial
				// bins in the SIFT descriptor is reweighted by the average of
				// the Gaussian window on each bin.
				float wy = computeWindowMean(binHeight, numBinsY, biny, gaussianWindowSize);

				// The triangular convolution functions convolve by a triangular
				// kernel with unit integral; instead for SIFT the triangular
				// kernel should have unit height. This is compensated for by
				// multiplying by the bin size:
				wy *= binHeight;

				for (int binx = 0; binx < numBinsX; ++binx) {
					float wx = computeWindowMean(binWidth, numBinsX, binx, gaussianWindowSize);
					wx *= binWidth;
					final float w = wx * wy;

					final int descriptorOffset = bint + binx * numOriBins + biny * (numBinsX * numOriBins);
					int descriptorIndex = 0;

					for (int framey = data.boundMinY; framey <= data.boundMaxY - frameSizeY + 1; framey += stepY) {
						for (int framex = data.boundMinX; framex <= data.boundMaxX - frameSizeX + 1; framex += stepX) {
							descriptors[descriptorIndex][descriptorOffset] = w
									* src[framey + biny * binHeight][framex + binx * binWidth];
							descriptorIndex++;
						}
					}
				}
			}
		}
	}
}
