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
package org.openimaj.image.processing.algorithm;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Median filter; replaces each pixel with the median of its neighbours.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MedianFilter implements SinglebandImageProcessor<Float, FImage> {
	/**
	 * Offsets for using a 3x3 cross shaped mask to select pixels for computing
	 * median.
	 */
	public final static int[][] CROSS_3x3 = {
			{ 0, -1 },
			{ -1, 0 }, { 0, 0 }, { 1, 0 },
			{ 0, 1 }
	};

	/**
	 * Offsets for using a 3x3 blocked shaped mask to select pixels for
	 * computing median.
	 */
	public final static int[][] BLOCK_3x3 = {
			{ -1, -1 }, { 0, -1 }, { 1, -1 },
			{ -1, 0 }, { 0, 0 }, { 1, 0 },
			{ -1, 1 }, { 0, 1 }, { 1, 1 }
	};

	private int[][] support;
	private DescriptiveStatistics ds;

	/**
	 * Construct with the given support region for selecting pixels to take the
	 * median from. The support mask is a
	 * <code>[n][2]<code> array of <code>n</code> relative x, y offsets from the
	 * pixel currently being processed.
	 * 
	 * @param support
	 *            the support coordinates
	 */
	public MedianFilter(int[][] support) {
		this.support = support;
		this.ds = new DescriptiveStatistics(support.length);
	}

	@Override
	public void processImage(FImage image) {
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				ds.clear();

				for (int i = 0; i < support.length; i++) {
					final int xx = x + support[i][0];
					final int yy = y + support[i][1];

					if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1)
						ds.addValue(image.pixels[yy][xx]);
				}

				image.pixels[y][x] = (float) ds.getPercentile(50);
			}
		}
	}
}
