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
package org.openimaj.image.feature.dense.binarypattern;

import gnu.trove.list.array.TIntArrayList;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.FloatFV;

/**
 * Class for extracting histograms of Local Uniform Binary Patterns. Uniform
 * patterns have less than one 01 transition and one 01 transition when viewed
 * as a circular buffer.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Ojala, T.", "Pietikainen, M.", "Maenpaa, T." },
		title = "Multiresolution gray-scale and rotation invariant texture classification with local binary patterns",
		year = "2002",
		journal = "Pattern Analysis and Machine Intelligence, IEEE Transactions on",
		pages = { "971 ", "987" },
		month = "jul",
		number = "7",
		volume = "24",
		customData = {
				"doi", "10.1109/TPAMI.2002.1017623",
				"ISSN", "0162-8828"
		})
public class LocalUniformBinaryPatternHistogram {
	protected int blocksize_x;
	protected int blocksize_y;
	FloatFV[][] histograms;

	/**
	 * Construct the extractor with the given block size for each local patch.
	 * 
	 * @param blocksize_x
	 *            the width of the blocks
	 * @param blocksize_y
	 *            the height of the blocks
	 */
	public LocalUniformBinaryPatternHistogram(int blocksize_x, int blocksize_y) {
		this.blocksize_x = blocksize_x;
		this.blocksize_y = blocksize_y;
	}

	/**
	 * Compute the histograms for the given pattern image, encoded with the
	 * given number of bits.
	 * 
	 * @param patternImage
	 *            the pattern data
	 * @param nbits
	 *            the number of bits used to encode the patterns
	 */
	public void calculateHistograms(int[][] patternImage, int nbits) {
		final int height = patternImage.length;
		final int width = patternImage[0].length;
		final TIntArrayList uniformPatterns = UniformBinaryPattern.getUniformPatterns(nbits);

		histograms = new FloatFV[(int) Math.ceil((double) height / (double) blocksize_y)][(int) Math.ceil((double) width
				/ (double) blocksize_x)];

		for (int y = 0, j = 0; y < height; y += blocksize_y, j++) {
			for (int x = 0, i = 0; x < width; x += blocksize_x, i++) {
				histograms[j][i] = new FloatFV(uniformPatterns.size() + 1);

				for (int yy = y; yy < Math.min(height, y + blocksize_y); yy++) {
					for (int xx = x; xx < Math.min(width, x + blocksize_x); xx++) {
						final int idx = uniformPatterns.indexOf(patternImage[yy][xx]);

						histograms[j][i].values[idx + 1]++;
					}
				}
			}
		}
	}

	/**
	 * Get the histograms
	 * 
	 * @return the histograms
	 */
	public FloatFV[][] getHistograms() {
		return histograms;
	}

	/**
	 * Get the histograms as a single {@link FloatFV}.
	 * 
	 * @return the histograms
	 */
	public FloatFV getHistogram() {
		final int len = histograms[0][0].length();
		final FloatFV h = new FloatFV(histograms.length * histograms[0].length * len);

		for (int j = 0; j < histograms.length; j++) {
			for (int i = 0; i < histograms[0].length; i++) {
				final int blkid = i + j * histograms[0].length;
				System.arraycopy(histograms[j][i].values, 0, h.values, blkid * len, len);
			}
		}

		return h;
	}
}
