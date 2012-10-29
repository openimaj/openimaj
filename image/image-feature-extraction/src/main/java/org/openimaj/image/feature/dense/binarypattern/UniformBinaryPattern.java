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
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;

/**
 * Class for determining whether specific binary patterns are "uniform". Uniform
 * patterns have less than one 01 transition and one 01 transition when viewed
 * as a circular buffer.
 * <p>
 * The class caches lookup tables of uniform patterns on demand, with the
 * exception of the commonly used 8-bit patterns which are cached on
 * initialization.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
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
public class UniformBinaryPattern {
	protected static TIntObjectHashMap<TIntArrayList> lut = new TIntObjectHashMap<TIntArrayList>();

	static {
		// pre-cache the table for 8-bit patterns as it is common
		lut.put(8, calculateUniformPatterns(8));
		// other patterns will be cached on demand
	}

	protected static TIntArrayList calculateUniformPatterns(int nbits) {
		final TIntArrayList result = new TIntArrayList();

		final boolean[] bits = new boolean[nbits];

		for (int i = 0; i < Math.pow(2, nbits); i++) {
			Arrays.fill(bits, false);

			for (int temp = i, j = 1; j <= nbits; j++) {
				final int pow = (int) Math.pow(2, (nbits - j));

				if (temp / pow > 0) {
					bits[j - 1] = true;
				}
				temp = temp % pow;
			}

			if (isUniform(bits)) {
				result.add(i);
			}
		}

		return result;
	}

	protected static boolean isUniform(boolean[] pattern) {
		int count = 0;

		for (int i = 0; i < pattern.length - 1; i++) {
			if (pattern[i] != pattern[i + 1]) {
				count++;
			}
		}

		return count <= 2;
	}

	/**
	 * Get a list of all the binary patterns of a given length that are
	 * "uniform". Uniform patterns have less than one 01 transition and one 01
	 * transition when viewed as a circular buffer.
	 * 
	 * The length must be between 1 and 32 bits.
	 * 
	 * @param nbits
	 *            pattern length
	 * @return set of patterns encoded as integers
	 */
	public static TIntArrayList getUniformPatterns(int nbits) {
		if (nbits < 1 || nbits > 32)
			throw new IllegalArgumentException("Only patterns with lengths between 1 and 32 bits are supported");

		TIntArrayList patterns = lut.get(nbits);

		if (patterns == null) {
			patterns = calculateUniformPatterns(nbits);
			lut.put(nbits, patterns);
		}

		return patterns;
	}

	/**
	 * Check whether the given nbits pattern is uniform.
	 * 
	 * @param pattern
	 *            the pattern
	 * @param nbits
	 *            the pattern length
	 * @return true if uniform; false otherwise.
	 */
	public static boolean isPatternUniform(int pattern, int nbits) {
		return getUniformPatterns(nbits).contains(pattern);
	}

	/**
	 * Compute a binary map showing the locations of the specified pattern code.
	 * 
	 * @param patternImage
	 *            the pattern data
	 * @param code
	 *            the code to extract
	 * @return a binary {@link FImage} depicting the locations of the given code
	 */
	public static FImage extractPatternImage(int[][] patternImage, int code) {
		final FImage image = new FImage(patternImage[0].length, patternImage.length);

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				if (patternImage[y][x] == code) {
					image.pixels[y][x] = 1;
				}
			}
		}

		return image;
	}

	/**
	 * Compute all binary maps for each possible pattern code given the number
	 * of bits used to encode patterns.
	 * 
	 * @param patternImage
	 *            the pattern data
	 * @param nbits
	 *            the number of bits for the patterns
	 * @return an array of binary maps corresponding to each pattern
	 */
	public static FImage[] extractPatternImages(int[][] patternImage, int nbits) {
		final TIntArrayList uniformPatterns = getUniformPatterns(nbits);

		final FImage[] images = new FImage[uniformPatterns.size() + 1];
		final int width = patternImage[0].length;
		final int height = patternImage.length;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int idx = uniformPatterns.indexOf(patternImage[y][x]);

				if (images[idx + 1] == null) {
					images[idx + 1] = new FImage(width, height);
				}

				images[idx + 1].pixels[y][x] = 1;
			}
		}

		return images;
	}

	/**
	 * Compute all binary maps for each possible pattern code given the number
	 * of bits used to encode patterns.
	 * 
	 * @param patternImage
	 *            the pattern data
	 * @param nbits
	 *            the number of bits for the patterns
	 * @return an array of binary maps corresponding to each pattern
	 */
	public static boolean[][][] extractPatternMaps(int[][] patternImage, int nbits) {
		final TIntArrayList uniformPatterns = getUniformPatterns(nbits);

		final int width = patternImage[0].length;
		final int height = patternImage.length;
		final boolean[][][] maps = new boolean[uniformPatterns.size() + 1][height][width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int idx = uniformPatterns.indexOf(patternImage[y][x]);

				maps[idx + 1][y][x] = true;
			}
		}

		return maps;
	}

	/**
	 * Compute all pixels matching each possible pattern code given the number
	 * of bits used to encode patterns.
	 * 
	 * @param patternImage
	 *            the pattern data
	 * @param nbits
	 *            the number of bits for the patterns
	 * @return an array of binary maps corresponding to each pattern
	 */
	public static List<List<Pixel>> extractPatternPixels(int[][] patternImage, int nbits) {
		final TIntArrayList uniformPatterns = getUniformPatterns(nbits);

		final List<List<Pixel>> images = new ArrayList<List<Pixel>>(uniformPatterns.size() + 1);
		final int width = patternImage[0].length;
		final int height = patternImage.length;

		for (int i = 0; i < uniformPatterns.size() + 1; i++)
			images.add(null);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int idx = uniformPatterns.indexOf(patternImage[y][x]);

				if (images.get(idx + 1) == null) {
					images.set(idx + 1, new ArrayList<Pixel>());
				}

				images.get(idx + 1).add(new Pixel(x, y));
			}
		}

		return images;
	}
}
