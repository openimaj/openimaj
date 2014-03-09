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
package org.openimaj.demos.sandbox;

import org.openimaj.data.RandomData;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.ml.gmm.GaussianMixtureModelEM;
import org.openimaj.ml.gmm.GaussianMixtureModelEM.CovarianceType;

public class GmmEmTesting {
	// public static void main(String[] args) {
	// final Random rng = new Random();
	// final double[][] data = new double[1000][4];
	// for (int j = 0; j < data.length / 2; j++) {
	// for (int i = 0; i < data[0].length; i++) {
	// data[j][i] = rng.nextGaussian() * (i + 1);
	// }
	// }
	//
	// for (int j = data.length / 2; j < data.length; j++) {
	// for (int i = 0; i < data[0].length; i++) {
	// data[j][i] = 10 + rng.nextGaussian() * (i + 1);
	// }
	// }
	//
	// final GaussianMixtureModelEM gmmem = new GaussianMixtureModelEM(2,
	// CovarianceType.Spherical);
	// final MixtureOfGaussians model = gmmem.estimate(data);
	//
	// System.out.println(MatrixUtils.toString(model.gaussians[0].getCovariance()));
	// System.out.println();
	// System.out.println(MatrixUtils.toString(model.gaussians[1].getCovariance()));
	// }

	public static void main(String[] args) {
		final double[][] data = RandomData.getRandomDoubleArray(10000, 64, -1d, 1d);

		final GaussianMixtureModelEM gmmem = new GaussianMixtureModelEM(512, CovarianceType.Diagonal);
		final MixtureOfGaussians model = gmmem.estimate(data);

		System.out.println(MatrixUtils.toString(model.gaussians[0].getCovariance()));
		System.out.println();
		System.out.println(MatrixUtils.toString(model.gaussians[1].getCovariance()));
	}
}
