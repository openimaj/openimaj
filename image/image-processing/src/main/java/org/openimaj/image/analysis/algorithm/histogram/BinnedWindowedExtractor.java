/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.analysis.algorithm.histogram;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * This class implements a {@link WindowedHistogramExtractor} with the primary
 * purpose of of producing efficient access to histograms of arbitrary windows
 * of the image.
 * <p>
 * This class analyses an image and produces an 2D array of integers with a
 * one-to-one correspondence with the image pixels. Each integer represents the
 * bin of the histogram into which the corresponding pixel would fall.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BinnedWindowedExtractor implements ImageAnalyser<FImage>, WindowedHistogramExtractor {
	protected int[][] binMap;
	protected int nbins;
	protected float min = 0;
	protected float max = 1;

	/**
	 * Construct with the given number of bins. The minimum expected value is
	 * assumed to be 0 and the maximum 1.
	 * 
	 * @param nbins
	 *            number of bins
	 */
	public BinnedWindowedExtractor(int nbins) {
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
	public BinnedWindowedExtractor(int nbins, float min, float max) {
		this.nbins = nbins;
		this.min = min;
		this.max = max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.analysis.algorithm.ImageHistogramAnalyser#getNumBins()
	 */
	@Override
	public int getNumBins() {
		return nbins;
	}

	/**
	 * Set the number of bins. The new value will not take effect until
	 * {@link #analyseImage(FImage)} is called.
	 * 
	 * @param nbins
	 *            the number of bins to set
	 */
	public void setNbins(int nbins) {
		this.nbins = nbins;
	}

	/**
	 * Get the expected minimum value in the input image
	 * 
	 * @return the expected minimum value
	 */
	public float getMin() {
		return min;
	}

	/**
	 * Set the expected minimum value. The new value will not take effect until
	 * {@link #analyseImage(FImage)} is called.
	 * 
	 * @param min
	 *            the minimum to set
	 */
	public void setMin(float min) {
		this.min = min;
	}

	/**
	 * Get the expected maximum value in the input image.
	 * 
	 * @return the expected maximum value
	 */
	public float getMax() {
		return max;
	}

	/**
	 * Set the expected maximum value. The new value will not take effect until
	 * {@link #analyseImage(FImage)} is called.
	 * 
	 * @param max
	 *            the maximum to set
	 */
	public void setMax(float max) {
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
				int bin = (int) (((image.pixels[y][x] - min) / (max - min)) * nbins);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.analysis.algorithm.ImageHistogramAnalyser#computeHistogram
	 * (org.openimaj.math.geometry.shape.Rectangle)
	 */
	@Override
	public Histogram computeHistogram(Rectangle roi) {
		return computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.analysis.algorithm.ImageHistogramAnalyser#computeHistogram
	 * (int, int, int, int)
	 */
	@Override
	public Histogram computeHistogram(int x, int y, int w, int h) {
		final Histogram hist = new Histogram(nbins);

		computeHistogram(x, y, w, h, hist);

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
		return computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height, weights);
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

	@Override
	public void computeHistogram(Rectangle roi, Histogram histogram) {
		computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height, histogram);
	}

	@Override
	public void computeHistogram(int x, int y, int w, int h, Histogram histogram) {
		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				histogram.values[binMap[r][c]]++;
			}
		}
	}
}
