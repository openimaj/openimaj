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
