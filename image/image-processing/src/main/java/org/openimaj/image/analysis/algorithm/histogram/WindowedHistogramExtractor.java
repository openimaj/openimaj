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

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * The primary purpose of this interface is to produce efficient access to
 * (rectangular) histograms of arbitrary windows of an image. A number of
 * methods are provided to extract such histograms.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface WindowedHistogramExtractor {

	/**
	 * Get the number of bins
	 * 
	 * @return the number of bins
	 */
	public abstract int getNumBins();

	/**
	 * Compute the histogram for the given window.
	 * 
	 * @param roi
	 *            the window
	 * @return the histogram in the window of the last analysed image
	 */
	public abstract Histogram computeHistogram(Rectangle roi);

	/**
	 * Compute the histogram for the given window.
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
	public abstract Histogram computeHistogram(int x, int y, int w, int h);

	/**
	 * Compute the histogram for the given window, storing the output in the
	 * given {@link Histogram} object, which must have the same length as given
	 * by {@link #getNumBins()}.
	 * 
	 * @param roi
	 *            the window
	 * @param histogram
	 *            the histogram to write to
	 */
	public abstract void computeHistogram(Rectangle roi, Histogram histogram);

	/**
	 * Compute the histogram for the given window, storing the output in the
	 * given {@link Histogram} object, which must have the same length as given
	 * by {@link #getNumBins()}.
	 * 
	 * @param x
	 *            The x-coordinate of the top-left of the window
	 * @param y
	 *            The y-coordinate of the top-left of the window
	 * @param w
	 *            The width of the window
	 * @param h
	 *            The height of the window
	 * @param histogram
	 *            the histogram to write to
	 */
	public abstract void computeHistogram(int x, int y, int w, int h, Histogram histogram);
}
