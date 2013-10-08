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
