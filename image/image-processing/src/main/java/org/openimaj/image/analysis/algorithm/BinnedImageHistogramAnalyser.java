package org.openimaj.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * This class analyses an image and produces an 2D array of integers with a
 * one-to-one correspondence with the image pixels. Each integer represents the
 * bin of the histogram into which the corresponding pixel would fall.
 * <p>
 * The primary purpose of this analyser is to produce efficient access to
 * histograms of arbitrary windows of the image. A number of methods are
 * provided to extract such histograms.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BinnedImageHistogramAnalyser implements ImageAnalyser<FImage> {
	int[][] binMap;
	int nbins;
	float min = 0;
	float max = 1;

	/**
	 * Construct with the given number of bins. The minimum expected value is
	 * assumed to be 0 and the maximum 1.
	 * 
	 * @param nbins
	 *            number of bins
	 */
	public BinnedImageHistogramAnalyser(int nbins) {
		this.nbins = nbins;
	}

	/**
	 * Construct with the given number of bins, and range.
	 * 
	 * @param nbins
	 *            number of bins
	 * @param min
	 *            minimum expected value
	 * @param max
	 *            maximum expected value
	 */
	public BinnedImageHistogramAnalyser(int nbins, float min, float max) {
		this.nbins = nbins;
		this.min = min;
		this.max = max;
	}

	/**
	 * Computes the bin-map for this image.
	 */
	@Override
	public void analyseImage(FImage image) {
		final int height = image.height;
		final int width = image.width;

		binMap = new int[height][width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int bin = (int) (((image.pixels[y][x] - min) / max) * nbins);

				if (bin > (nbins - 1))
					bin = nbins - 1;

				binMap[y][x] = bin;
			}
		}
	}

	/**
	 * Get the bin-map created in the last call to {@link #analyseImage(FImage)}
	 * .
	 * 
	 * @return the bin map
	 */
	public int[][] getBinMap() {
		return binMap;
	}

	/**
	 * Compute the histogram for the given window. Each pixel contributes 1 to
	 * the corresponding bin.
	 * 
	 * @param roi
	 *            the window
	 * @return the histogram in the window of the last analysed image
	 */
	public Histogram computeHistogram(Rectangle roi) {
		return computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height);
	}

	/**
	 * Compute the histogram for the given window. Each pixel contributes 1 to
	 * the corresponding bin.
	 * 
	 * @param x
	 *            The x-coordinate of the top-left of the window
	 * @param y
	 *            The y-coordinate of the top-left of the window
	 * @param w
	 *            The width of the window
	 * @param h
	 *            The height of the window
	 * @return the histogram in the window of the last analysed image
	 */
	public Histogram computeHistogram(int x, int y, int w, int h) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				hist.values[binMap[r][c]]++;
			}
		}

		return hist;
	}

	/**
	 * Compute the histogram for the given window. The weight for each bin is
	 * taken from the given weights image.
	 * 
	 * @param roi
	 *            the window
	 * @param weights
	 *            the weights image. Must be the same size as the analysed
	 *            image.
	 * @return the histogram in the window of the last analysed image
	 */
	public Histogram computeHistogram(Rectangle roi, FImage weights) {
		return computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height);
	}

	/**
	 * Compute the histogram for the given window. The weight for each bin is
	 * taken from the given weights image.
	 * 
	 * @param x
	 *            The x-coordinate of the top-left of the window
	 * @param y
	 *            The y-coordinate of the top-left of the window
	 * @param w
	 *            The width of the window
	 * @param h
	 *            The height of the window
	 * @param weights
	 *            the weights image. Must be the same size as the analysed
	 *            image.
	 * @return the histogram in the window of the last analysed image
	 */
	public Histogram computeHistogram(int x, int y, int w, int h, FImage weights) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				hist.values[binMap[r][c]] += weights.pixels[r][c];
			}
		}

		return hist;
	}

	/**
	 * Compute the histogram for the given window. The weight for each bin is
	 * taken from the given weights image, and is multiplied by the
	 * corresponding weight in the window image before accumulation. The size of
	 * the window is taken from the window weights image.
	 * <p>
	 * This method primarily allows you to compute a spatially weighted
	 * histogram. For example, the window weights image could be a 2D Gaussian,
	 * and thus the histogram would apply more weight on to the centre pixels.
	 * 
	 * @param x
	 *            The x-coordinate of the top-left of the window
	 * @param y
	 *            The y-coordinate of the top-left of the window
	 * @param weights
	 *            The weights image. Must be the same size as the analysed
	 *            image.
	 * @param windowWeights
	 *            The weights for each pixel in the window.
	 * @return the histogram in the window of the last analysed image
	 */
	public Histogram computeHistogram(int x, int y, FImage weights, FImage windowWeights) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + windowWeights.height);
		final int stopx = Math.min(binMap[0].length, x + windowWeights.width);

		final int startwr = y < 0 ? -y : y;
		final int startwc = x < 0 ? -x : x;

		for (int r = starty, wr = startwr; r < stopy; r++, wr++) {
			for (int c = startx, wc = startwc; c < stopx; c++, wc++) {
				hist.values[binMap[r][c]] += (weights.pixels[r][c] * windowWeights.pixels[wr][wc]);
			}
		}

		return hist;
	}
}
