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
package org.openimaj.image.processing.convolution.filterbank;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FConvolution;

/**
 * Implementation of the MR8 filter bank described in: C. Schmid. Constructing
 * models for content-based image retrieval. In Proceedings of the IEEE
 * Conference on Computer Vision and Pattern Recognition, volume 2, pages 39-45,
 * 2001.
 * 
 * Inspired by the matlab implementation from
 * http://www.robots.ox.ac.uk/~vgg/research/texclass/filters.html
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */

public class SchmidFilterBank extends FilterBank {
	/**
	 * Default constructor with a support of 49 pixels.
	 */
	public SchmidFilterBank() {
		this(49);
	}

	/**
	 * Construct with given support (filter size).
	 * 
	 * @param size
	 *            the filter size
	 */
	public SchmidFilterBank(int size) {
		super(makeFilters(size));
	}

	protected static FConvolution[] makeFilters(int SUP) {
		final FConvolution[] F = new FConvolution[13];

		F[0] = makeFilter(SUP, 2, 1);
		F[1] = makeFilter(SUP, 4, 1);
		F[2] = makeFilter(SUP, 4, 2);
		F[3] = makeFilter(SUP, 6, 1);
		F[4] = makeFilter(SUP, 6, 2);
		F[5] = makeFilter(SUP, 6, 3);
		F[6] = makeFilter(SUP, 8, 1);
		F[7] = makeFilter(SUP, 8, 2);
		F[8] = makeFilter(SUP, 8, 3);
		F[9] = makeFilter(SUP, 10, 1);
		F[10] = makeFilter(SUP, 10, 2);
		F[11] = makeFilter(SUP, 10, 3);
		F[12] = makeFilter(SUP, 10, 4);

		return F;
	}

	private static FConvolution makeFilter(int sup, float sigma, float tau) {
		final int hs = (sup - 1) / 2;

		final FImage filter = new FImage(sup, sup);
		for (int y = -hs, j = 0; y < hs; y++, j++) {
			for (int x = -hs, i = 0; x < hs; x++, i++) {
				final float r = (float) sqrt(x * x + y * y);

				filter.pixels[j][i] = (float) (cos(r * (PI * tau / sigma)) * exp(-(r * r) / (2 * sigma * sigma)));
			}
		}

		return new FConvolution(LeungMalikFilterBank.normalise(filter));
	}
}
