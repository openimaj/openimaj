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
package org.openimaj.workinprogress;

import java.util.Arrays;
import java.util.Random;

import org.openimaj.data.AbstractDataSource;
import org.openimaj.math.matrix.ThinSingularValueDecomposition;
import org.openimaj.workinprogress.optimisation.DifferentiableObjectiveFunction;
import org.openimaj.workinprogress.optimisation.EpochAnnealedLearningRate;
import org.openimaj.workinprogress.optimisation.SGD;
import org.openimaj.workinprogress.optimisation.params.KeyedParameters;
import org.openimaj.workinprogress.optimisation.params.KeyedParameters.ObjectDoubleEntry;

import Jama.Matrix;

public class GD_SVD2 {
	static class GD_SVD2_DOF implements DifferentiableObjectiveFunction<GD_SVD2, double[], KeyedParameters<String>> {
		public int k;

		@Override
		public double value(GD_SVD2 model, double[] data) {
			final int i = (int) data[0];
			final int j = (int) data[1];
			final int m = (int) data[2];

			final double error = m - model.predict(i, j, k);

			return error * error;
		}

		@Override
		public KeyedParameters<String> derivative(GD_SVD2 model, double[] data) {
			final int i = (int) data[0];
			final int j = (int) data[1];
			final double m = data[2];
			final double[][] Uprime = model.UprimeM.getArray();
			final double[][] Vprime = model.VprimeM.getArray();

			final double error = m - model.predict(i, j, k);
			final double uTemp = Uprime[i][k];
			final double vTemp = Vprime[j][k];

			System.out.println("Error: " + error + " " + m);

			final KeyedParameters<String> params = new KeyedParameters<String>();
			params.set("i" + i, error * vTemp);
			params.set("j" + j, error * uTemp);
			return params;
		}

		@Override
		public void updateModel(GD_SVD2 model, KeyedParameters<String> weights) {
			final double[][] Uprime = model.UprimeM.getArray();
			final double[][] Vprime = model.VprimeM.getArray();

			// for (final Entry e : weights.firstObject().entries()) {
			// Uprime[e.index][k] += e.value;
			//
			// System.out.println(e.index + " " + e.value);
			// }
			// for (final Entry e : weights.secondObject().entries()) {
			// Vprime[e.index][k] += e.value;
			// }
			for (final ObjectDoubleEntry<String> e : weights) {
				final char type = e.key.charAt(0);
				final int idx = Integer.parseInt(e.key.substring(1));

				if (type == 'i') {
					Uprime[idx][k] += e.value;
				} else {
					Vprime[idx][k] += e.value;
				}
			}

			// model.UprimeM.print(5, 5);
		}
	}

	private static final int maxEpochs = 300;
	private static final double initialLearningRate = 0.01;
	private static final double annealingRate = maxEpochs * 0.1;

	Matrix UprimeM;
	Matrix VprimeM;
	private Matrix UM;
	private Matrix VM;
	private Matrix SM;

	protected double predict(int i, int j, int k) {
		final double[][] Uprime = UprimeM.getArray();
		final double[][] Vprime = VprimeM.getArray();

		double pred = 0;
		for (int kk = 0; kk <= k; kk++)
			pred += Uprime[i][kk] * Vprime[j][kk];

		return pred;
	}

	public GD_SVD2(Matrix MM, int maxOrder) {
		final Random random = new Random(0);
		final double initValue = 1 / Math.sqrt(maxOrder);

		final int m = MM.getRowDimension();
		final int n = MM.getColumnDimension();

		UprimeM = new Matrix(m, maxOrder);
		VprimeM = new Matrix(n, maxOrder);
		final double[][] Uprime = UprimeM.getArray();
		final double[][] Vprime = VprimeM.getArray();
		final double[][] M = MM.getArray();

		final SGD<GD_SVD2, double[], KeyedParameters<String>> sgd = new SGD<GD_SVD2, double[], KeyedParameters<String>>();
		sgd.fcn = new GD_SVD2_DOF();
		sgd.batchSize = 1;
		sgd.maxEpochs = 300;
		sgd.learningRate = new EpochAnnealedLearningRate(0.01, 300);
		sgd.model = this;

		for (((GD_SVD2_DOF) sgd.fcn).k = 0; ((GD_SVD2_DOF) sgd.fcn).k < maxOrder; ((GD_SVD2_DOF) sgd.fcn).k++) {
			for (int i = 0; i < m; i++)
				Uprime[i][((GD_SVD2_DOF) sgd.fcn).k] = random.nextGaussian() * initValue;
			for (int j = 0; j < n; j++)
				Vprime[j][((GD_SVD2_DOF) sgd.fcn).k] = random.nextGaussian() * initValue;

			sgd.train(new AbstractDataSource<double[]>() {

				@Override
				public void getData(int startRow, int stopRow, double[][] data) {
					for (int idx = startRow, kkk = 0; idx < stopRow; idx++, kkk++) {
						final int row = idx / M[0].length;
						final int col = idx % M[0].length;
						data[kkk][0] = row;
						data[kkk][1] = col;
						data[kkk][2] = M[row][col];
					}
				}

				@Override
				public double[] getData(int idx) {
					final int row = idx / M[0].length;
					final int col = idx % M[0].length;

					return new double[] { row, col, M[row][col] };
				}

				@Override
				public int numDimensions() {
					return 3;
				}

				@Override
				public int size() {
					return M[0].length * M.length;
				}

				@Override
				public double[][] createTemporaryArray(int size) {
					return new double[size][3];
				}

			});
		}

		UM = new Matrix(m, maxOrder);
		final double[][] U = UM.getArray();
		SM = new Matrix(maxOrder, maxOrder);
		final double[][] S = SM.getArray();
		VM = new Matrix(maxOrder, n);
		final double[][] V = VM.getArray();
		for (int i = 0; i < maxOrder; i++) {
			double un = 0;
			double vn = 0;
			for (int j = 0; j < m; j++) {
				un += (Uprime[j][i] * Uprime[j][i]);
			}
			for (int j = 0; j < n; j++) {
				vn += (Vprime[j][i] * Vprime[j][i]);
			}

			un = Math.sqrt(un);
			vn = Math.sqrt(vn);

			for (int j = 0; j < m; j++) {
				U[j][i] = Uprime[j][i] / un;
			}
			for (int j = 0; j < n; j++) {
				V[i][j] = Vprime[j][i] / vn;
			}

			S[i][i] = un * vn;
		}
	}

	public static void main(String[] args) {
		final Matrix m = new Matrix(new double[][] { { 0.5, 0.4 }, { 0.1, 0.7 } });

		final GD_SVD2 gdsvd = new GD_SVD2(m, 2);

		// m.print(5, 5);
		// gdsvd.UprimeM.print(5, 5);
		// gdsvd.UprimeM.times(gdsvd.VprimeM.transpose()).print(5, 5);
		// gdsvd.UM.times(gdsvd.SM.times(gdsvd.VM)).print(5, 5);
		// gdsvd.UM.print(5, 5);
		gdsvd.SM.print(5, 5);
		// gdsvd.VM.print(5, 5);

		final ThinSingularValueDecomposition tsvd = new ThinSingularValueDecomposition(m, 2);
		// tsvd.U.print(5, 5);
		System.out.println(Arrays.toString(tsvd.S));
	}
}
