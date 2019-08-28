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
package org.openimaj.math.statistics.normalisation;

/**
 * z-score normalisation (standardisation). Upon training, the mean and variance
 * of each dimension is computed; normalisation works by subtracting the mean
 * and dividing by the standard deviation.
 * <p>
 * This implementation includes an optional regularisation parameter that is
 * added to the variance before the division.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ZScore implements TrainableNormaliser, Denormaliser {
	double[] mean;
	double[] sigma;
	double eps = 0;

	/**
	 * Construct without regularisation.
	 */
	public ZScore() {
	}

	/**
	 * Construct with regularisation.
	 *
	 * @param eps
	 *            the variance normalisation regulariser (each dimension is
	 *            divided by sqrt(var + eps).
	 */
	public ZScore(double eps) {
		this.eps = eps;
	}

	@Override
	public void train(double[][] data) {
		mean = new double[data[0].length];
		sigma = new double[data[0].length];

		for (int r = 0; r < data.length; r++)
			for (int c = 0; c < data[0].length; c++)
				mean[c] += data[r][c];

		for (int c = 0; c < data[0].length; c++)
			mean[c] /= data.length;

		for (int r = 0; r < data.length; r++) {
			for (int c = 0; c < data[0].length; c++) {
				final double delta = (data[r][c] - mean[c]);
				sigma[c] += delta * delta;
			}
		}

		for (int c = 0; c < data[0].length; c++)
			sigma[c] = Math.sqrt(eps + (sigma[c] / (data.length - 1)));
	}

	@Override
	public double[] normalise(double[] vector) {
		final double[] out = new double[vector.length];
		for (int c = 0; c < out.length; c++)
			out[c] = (vector[c] - mean[c]) / sigma[c];
		return out;
	}

	@Override
	public double[][] normalise(double[][] data) {
		final double[][] out = new double[data.length][];
		for (int c = 0; c < out.length; c++)
			out[c] = normalise(data[c]);
		return out;
	}

	@Override
	public double[] denormalise(double[] vector) {
		final double[] out = new double[vector.length];
		for (int c = 0; c < out.length; c++)
			out[c] = sigma[c] * vector[c] + mean[c];
		return out;
	}

	@Override
	public double[][] denormalise(double[][] data) {
		final double[][] out = new double[data.length][];
		for (int c = 0; c < out.length; c++)
			out[c] = denormalise(data[c]);
		return out;
	}
}
