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
package org.openimaj.math.matrix.algorithm.whitening;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

/**
 * Abstract base class for whitening transforms ("sphering"). Whitening
 * transforms rescale data to remove correlations; more specifically the idea of
 * whitening is that it completely removes the second-order information
 * (correlations and (co)-variances) of the data.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@Reference(
		type = ReferenceType.Book,
		author = { "Hyvrinen, Aapo", "Hurri, Jarmo", "Hoyer, Patrick O." },
		title = "Natural Image Statistics: A Probabilistic Approach to Early Computational Vision.",
		year = "2009",
		edition = "1st",
		publisher = "Springer Publishing Company, Incorporated",
		customData = {
				"isbn", "1848824904, 9781848824904"
		})
public abstract class WhiteningTransform {
	/**
	 * Apply the whitening transform to the given vector.
	 *
	 * @param vector
	 *            the vector
	 * @return the whitened vector
	 */
	public abstract double[] whiten(double[] vector);

	/**
	 * Apply the whitening transform to the given vectors.
	 *
	 * @param vector
	 *            the vectors; one per row
	 * @return the whitened vector
	 */
	public double[][] whiten(double[][] vector) {
		final double[][] out = new double[vector.length][];
		for (int i = 0; i < vector.length; i++) {
			out[i] = whiten(vector[i]);
		}
		return out;
	}

	/**
	 * Train the whitening transform with the given features.
	 *
	 * @param data
	 *            the data (one feature per row)
	 */
	public abstract void train(double[][] data);
}
