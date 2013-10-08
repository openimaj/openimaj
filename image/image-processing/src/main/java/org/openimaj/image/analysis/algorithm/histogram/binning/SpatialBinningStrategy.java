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
package org.openimaj.image.analysis.algorithm.histogram.binning;

import org.openimaj.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Interface that describes the creation of a histogram from a spatial region of
 * an image based on sub-histograms extracted using a
 * {@link WindowedHistogramExtractor}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface SpatialBinningStrategy {
	/**
	 * Extract a histogram describing image content in the given region using
	 * the given {@link WindowedHistogramExtractor} to extract (sub) histograms
	 * from which to build the output.
	 * <p>
	 * For efficiency, this method allows the output histogram to be specified
	 * as an input. This means that implementors of this interface can attempt
	 * to fill the output histogram rather than creating a new instance
	 * (although care should be taken to ensure that the porivded output
	 * histogram is the correct size and not <code>null</code>).
	 * <p>
	 * Users of {@link SpatialBinningStrategy}s should use the following style
	 * for maximum efficiency: <code><pre>
	 * Histogram h = null;
	 * ...
	 * for (Rectangle region : lots_of_regions)
	 * 	h = strategy.extract(binnedData, region, h);
	 * </pre></code>
	 * 
	 * @param binnedData
	 *            the {@link WindowedHistogramExtractor} to extract
	 *            sub-histograms from
	 * @param region
	 *            the region to extract from
	 * @param output
	 *            the output histogram to fill (can be null)
	 * @return the extracted histogram (preferably <code>output</code>)
	 */
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output);
}
