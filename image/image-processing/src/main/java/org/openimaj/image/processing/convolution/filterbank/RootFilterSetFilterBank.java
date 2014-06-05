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

import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.convolution.LaplacianOfGaussian2D;

/**
 * Implementation of the Root Filter Set filter bank described at:
 * http://www.robots.ox.ac.uk/~vgg/research/texclass/filters.html
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */

public class RootFilterSetFilterBank extends FilterBank {
	protected final static float[] SCALES = { 1, 2, 4 };
	protected final static int NUM_ORIENTATIONS = 6;

	/**
	 * Default constructor with a support of 49 pixels.
	 */
	public RootFilterSetFilterBank() {
		this(49);
	}

	/**
	 * Construct with given support (filter size).
	 * 
	 * @param size
	 *            the filter size
	 */
	public RootFilterSetFilterBank(int size) {
		super(makeFilters(size));
	}

	protected static FConvolution[] makeFilters(int size) {
		final int numRotInvariants = 2;
		final int numBar = SCALES.length * NUM_ORIENTATIONS;
		final int numEdge = SCALES.length * NUM_ORIENTATIONS;
		final int numFilters = numBar + numEdge + numRotInvariants;
		final FConvolution[] F = new FConvolution[numFilters];

		int count = 0;
		for (final float scale : SCALES) {
			for (int orient = 0; orient < NUM_ORIENTATIONS; orient++) {
				final float angle = (float) (PI * orient / NUM_ORIENTATIONS);

				F[count] = new FConvolution(LeungMalikFilterBank.makeFilter(scale, 0, 1, angle, size));
				F[count + numEdge] = new FConvolution(LeungMalikFilterBank.makeFilter(scale, 0, 2, angle, size));
				count++;
			}
		}

		F[numBar + numEdge] = new FConvolution(Gaussian2D.createKernelImage(size, 10)); // don't
																						// normalise
		F[numBar + numEdge + 1] = new FConvolution(LeungMalikFilterBank.normalise(LaplacianOfGaussian2D
				.createKernelImage(size, 10)));

		return F;
	}
}
