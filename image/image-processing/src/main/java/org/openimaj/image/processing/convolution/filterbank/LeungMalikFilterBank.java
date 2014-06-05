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
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.convolution.LaplacianOfGaussian2D;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Implementation of a the filter bank described in: T. Leung and J. Malik.
 * Representing and recognizing the visual appearance of materials using
 * three-dimensional textons. IJCV, 2001
 * 
 * Inspired by the matlab implementation from
 * http://www.robots.ox.ac.uk/~vgg/research/texclass/filters.html
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class LeungMalikFilterBank extends FilterBank {
	/**
	 * Default constructor with a filter support of 49 pixels
	 */
	public LeungMalikFilterBank() {
		this(49);
	}

	/**
	 * Construct with given support (filter size).
	 * 
	 * @param size
	 *            the filter size
	 */
	public LeungMalikFilterBank(int size) {
		super(makeFilters(size));
	}

	protected static FConvolution[] makeFilters(int size) {
		final int nScales = 3;
		final int nOrientations = 6;

		final int NROTINV = 12;
		final int NBAR = nScales * nOrientations;
		final int NEDGE = nScales * nOrientations;
		final int NF = NBAR + NEDGE + NROTINV;

		final FConvolution F[] = new FConvolution[NF];

		int count = 0;
		for (int i = 1; i <= nScales; i++) {
			final float scale = (float) pow(sqrt(2), i);

			for (int orient = 0; orient < nOrientations; orient++) {
				final float angle = (float) (PI * orient / nOrientations);

				F[count] = new FConvolution(makeFilter(scale, 0, 1, angle, size));
				F[count + NEDGE] = new FConvolution(makeFilter(scale, 0, 2, angle, size));
				count++;
			}
		}

		count = NBAR + NEDGE;
		for (int i = 1; i <= 4; i++) {
			final float scale = (float) pow(sqrt(2), i);

			F[count] = new FConvolution(normalise(Gaussian2D.createKernelImage(size, scale)));
			F[count + 1] = new FConvolution(normalise(LaplacianOfGaussian2D.createKernelImage(size, scale)));
			F[count + 2] = new FConvolution(normalise(LaplacianOfGaussian2D.createKernelImage(size, 3 * scale)));
			count += 3;
		}

		return F;
	}

	protected static FImage makeFilter(float scale, int phasex, int phasey, float angle, int size) {
		final int hs = (size - 1) / 2;

		final FImage filter = new FImage(size, size);
		for (int y = -hs, j = 0; y < hs; y++, j++) {
			for (int x = -hs, i = 0; x < hs; x++, i++) {
				final float cos = (float) cos(angle);
				final float sin = (float) sin(angle);

				final float rx = cos * x - sin * y;
				final float ry = sin * x + cos * y;

				final float gx = gaussian1D(3 * scale, 0, rx, phasex);
				final float gy = gaussian1D(scale, 0, ry, phasey);

				filter.pixels[j][i] = gx * gy;
			}
		}
		return normalise(filter);
	}

	protected static float gaussian1D(float sigma, float mean, float x, int order) {
		x = x - mean;
		final float num = x * x;

		final float variance = sigma * sigma;
		final float denom = 2 * variance;
		final float g = (float) (exp(-num / denom) / pow(PI * denom, 0.5));

		switch (order) {
		case 0:
			return g;
		case 1:
			return -g * (x / variance);
		case 2:
			return g * ((num - variance) / (variance * variance));
		default:
			throw new IllegalArgumentException("order must be 0, 1 or 2.");
		}
	}

	protected static FImage normalise(FImage f) {
		final float mean = FloatArrayStatsUtils.mean(f.pixels);
		f.subtractInplace(mean);
		final float sumabs = FloatArrayStatsUtils.sumAbs(f.pixels);
		return f.divideInplace(sumabs);
	}
}
