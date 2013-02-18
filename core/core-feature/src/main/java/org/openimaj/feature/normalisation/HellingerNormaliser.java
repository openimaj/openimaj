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
package org.openimaj.feature.normalisation;

import org.openimaj.feature.FloatFV;

/**
 * This {@link Normaliser} normalises vectors such that the Euclidean distance
 * between normalised vectors is equivalent to computing the similarity using
 * the Hellinger kernel on the un-normalised vectors.
 * <p>
 * The normalisation works by optionally adding an offset to the vectors (to
 * deal with input vectors that have negative values), L1 normalising the
 * vectors and finally performing an element-wise sqrt.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HellingerNormaliser implements Normaliser<FloatFV> {
	protected int offset;

	/**
	 * Construct with no offset
	 */
	public HellingerNormaliser() {
		this.offset = 0;
	}

	/**
	 * Construct with the given offset
	 * 
	 * @param offset
	 *            the offset
	 */
	public HellingerNormaliser(int offset) {
		this.offset = offset;
	}

	@Override
	public void normalise(FloatFV feature) {
		normalise(feature.values, offset);
	}

	/**
	 * Static utility function to perform Hellinger normalisation.
	 * 
	 * @param values
	 *            the values to normalise
	 * @param offset
	 *            the offset to add to the values before normalisation (to
	 *            ensure they are +ve).
	 */
	public static void normalise(float[] values, int offset) {
		double sum = 0;

		for (int i = 0; i < values.length; i++) {
			values[i] += offset;
			sum += values[i];
		}

		for (int i = 0; i < values.length; i++) {
			values[i] = (float) Math.sqrt(values[i] / sum);
		}
	}
}
