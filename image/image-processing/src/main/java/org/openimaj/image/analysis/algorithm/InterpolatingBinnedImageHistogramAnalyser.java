package org.openimaj.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.math.statistics.distribution.Histogram;

public class InterpolatingBinnedImageHistogramAnalyser extends BinnedImageHistogramAnalyser {
	/**
	 * The weight to apply to the left bin (i.e. the one that was stored). The
	 * weight of the right bin is 1-this.
	 */
	float[][] weights;

	/**
	 * Are the histograms cyclic?
	 */
	boolean wrap = false;

	/**
	 * Construct with the given number of bins. The minimum expected value is
	 * assumed to be 0 and the maximum 1.
	 * 
	 * @param nbins
	 *            number of bins
	 */
	public InterpolatingBinnedImageHistogramAnalyser(int nbins) {
		super(nbins);
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
	public InterpolatingBinnedImageHistogramAnalyser(int nbins, float min, float max) {
		super(nbins, min, max);
	}

	/**
	 * Computes the bin-map for this image.
	 */
	@Override
	public void analyseImage(FImage image) {
		final int height = image.height;
		final int width = image.width;

		binMap = new int[height][width];
		weights = new float[height][width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float val = ((image.pixels[y][x] - min) / max) * nbins;
				final int bin = (int) Math.floor(val);

				int lbin;
				float lweight;
				if (val - bin < 0.5) {
					// right bin
					lbin = bin - 1;
					lweight = 0.5f + (val - bin);
				} else {
					// left bin
					lbin = bin;
					lweight = 1.5f - (val - bin);
				}

				if (wrap) {
					if (lbin < 0)
						lbin = nbins - 1;
					else if (lbin >= nbins)
						lbin = 0;
				} else {
					if (lbin < 0) {
						lbin = 0;
						lweight = 1;
					}
					else if (bin >= nbins) {
						lbin = nbins - 1;
						lweight = 1;
					}
				}

				binMap[y][x] = lbin;

				weights[y][x] = lweight;
			}
		}
	}

	@Override
	public Histogram computeHistogram(int x, int y, int w, int h) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				final int bin = binMap[r][c];
				hist.values[bin] += weights[r][c];

				if (wrap) {
					if (bin + 1 == nbins) {
						hist.values[0] += (1 - weights[r][c]);
					}
				} else if (bin + 1 < nbins) {
					hist.values[bin + 1] += (1 - weights[r][c]);
				}
			}
		}

		return hist;
	}

	@Override
	public Histogram computeHistogram(int x, int y, int w, int h, FImage extWeights) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				final int bin = binMap[r][c];
				hist.values[bin] += (extWeights.pixels[r][c] * weights[r][c]);

				if (wrap) {
					if (bin + 1 == nbins) {
						hist.values[0] += (extWeights.pixels[r][c] * (1 - weights[r][c]));
					}
				} else if (bin + 1 < nbins) {
					hist.values[bin + 1] += (extWeights.pixels[r][c] * (1 - weights[r][c]));
				}
			}
		}

		return hist;
	}

	@Override
	public Histogram computeHistogram(int x, int y, FImage extWeights, FImage windowWeights)
	{
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + windowWeights.height);
		final int stopx = Math.min(binMap[0].length, x + windowWeights.width);

		final int startwr = y < 0 ? -y : y;
		final int startwc = x < 0 ? -x : x;

		for (int r = starty, wr = startwr; r < stopy; r++, wr++) {
			for (int c = startx, wc = startwc; c < stopx; c++, wc++) {
				final int bin = binMap[r][c];
				hist.values[bin] += (extWeights.pixels[r][c] * weights[r][c] * windowWeights.pixels[wr][wc]);

				if (wrap) {
					if (bin + 1 == nbins) {
						hist.values[0] += (extWeights.pixels[r][c] * (1 - weights[r][c]) * windowWeights.pixels[wr][wc]);
					}
				} else if (bin + 1 < nbins) {
					hist.values[bin + 1] += (extWeights.pixels[r][c] * (1 - weights[r][c]) * windowWeights.pixels[wr][wc]);
				}
			}
		}

		return hist;
	}
}
