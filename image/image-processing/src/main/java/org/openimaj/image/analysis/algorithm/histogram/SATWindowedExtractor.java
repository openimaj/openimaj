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
import org.openimaj.image.analysis.algorithm.SummedAreaTable;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * This class implements a {@link WindowedHistogramExtractor} with the primary
 * purpose of of producing efficient access to histograms of arbitrary windows
 * of the image.
 * <p>
 * This implementation is based on a stack of {@link SummedAreaTable}s, with one
 * {@link SummedAreaTable} per histogram bin. Obviously this is quite memory
 * intensive, so should probably only be used with small numbers of bins.
 * However, the advantage over a {@link BinnedWindowedExtractor} is that the
 * histogram extraction is an O(1) operation, and it is thus very quick for
 * evaluating many windows.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SATWindowedExtractor implements WindowedHistogramExtractor {
	protected final SummedAreaTable[] sats;
	protected final int nbins;

	/**
	 * Protected constructor for subclasses to use if they don't wan't to
	 * compute the SATs at construction time
	 *
	 * @param nbins
	 *            the number of histogram bins.
	 */
	protected SATWindowedExtractor(int nbins) {
		this.nbins = nbins;
		this.sats = new SummedAreaTable[nbins];
	}

	/**
	 * Construct with the given spatial histogram magnitude maps. Each image
	 * represents a bin of the histogram, and each element is the histogram
	 * weight at the respective spatial location for the corresponding bin
	 * (usually the images will be very sparse).
	 *
	 * @param magnitudeMaps
	 *            array of images, one per bin, with each pixel set to the
	 *            histogram magnitude at that bin
	 */
	public SATWindowedExtractor(FImage[] magnitudeMaps) {
		this.nbins = magnitudeMaps.length;

		sats = new SummedAreaTable[nbins];
		computeSATs(magnitudeMaps);
	}

	protected void computeSATs(FImage[] magnitudeMaps) {
		for (int i = 0; i < nbins; i++) {
			sats[i] = new SummedAreaTable(magnitudeMaps[i]);
		}
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
	public Histogram computeHistogram(final int x, final int y, final int w, final int h) {
		final Histogram hist = new Histogram(nbins);
		final int x2 = x + w;
		final int y2 = y + h;

		for (int i = 0; i < nbins; i++) {
			final float val = sats[i].calculateArea(x, y, x2, y2);
			// rounding errors in the SAT can lead to small values that should
			// actually be zero
			hist.values[i] = val < 1e-4 ? 0 : val;

		}

		return hist;
	}

	@Override
	public void computeHistogram(final int x, final int y, final int w, final int h, final Histogram hist) {
		final int x2 = x + w;
		final int y2 = y + h;
		final double[] values = hist.values;

		for (int i = 0; i < values.length; i++) {
			final float val = sats[i].calculateArea(x, y, x2, y2);
			values[i] = Math.max(0, val); // rounding errors in the SAT
			// might lead to small -ve's
		}
	}

	@Override
	public void computeHistogram(Rectangle roi, Histogram histogram) {
		computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height, histogram);
	}
}
