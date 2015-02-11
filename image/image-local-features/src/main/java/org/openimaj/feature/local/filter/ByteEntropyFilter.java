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
package org.openimaj.feature.local.filter;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.ByteFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.util.function.Predicate;

/**
 * Filter {@link LocalFeature}s typed on {@link ByteFV} by rejecting those that
 * have a low feature entropy. Such features are those that tend to have little
 * variation; for example, in the case of SIFT features, the removed features
 * are typically the ones that mismatch easily.
 * <p>
 * This filter is an implementation of the approach described by Dong, Wang and
 * Li; the default threshold is taken from the paper, and will work with
 * standard SIFT features, such as those produced by a {@link DoGSIFTEngine}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		author = { "Wei Dong", "Zhe Wang", "Kai Li" },
		title = "High-Confidence Near-Duplicate Image Detection",
		type = ReferenceType.Inproceedings,
		year = "2012",
		booktitle = "ACM International Conference on Multimedia Retrieval",
		customData = { "location", "Hong Kong, China" })
public class ByteEntropyFilter implements Predicate<LocalFeature<?, ByteFV>> {
	double threshold = 4.4;

	/**
	 * Construct with the default threshold of 4.4 as suggested in the original
	 * paper.
	 */
	public ByteEntropyFilter() {
	}

	/**
	 * Construct with a custom threshold.
	 *
	 * @param threshold
	 *            The threshold.
	 */
	public ByteEntropyFilter(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public boolean test(LocalFeature<?, ByteFV> object) {
		return entropy(object.getFeatureVector().values) >= threshold;
	}

	/**
	 * Compute the entropy of the given byte vector.
	 *
	 * @param vector
	 *            the vector.
	 * @return the entropy.
	 */
	public static double entropy(byte[] vector) {
		final int[] counts = new int[256];
		for (int i = 0; i < vector.length; i++) {
			counts[vector[i] + 128]++;
		}

		final double log2 = Math.log(2);
		double entropy = 0;
		for (int b = 0; b < counts.length; b++) {
			final double p = (double) counts[b] / (double) vector.length;

			entropy -= (p == 0 ? 0 : p * Math.log(p) / log2);
		}
		return entropy;
	}
}
