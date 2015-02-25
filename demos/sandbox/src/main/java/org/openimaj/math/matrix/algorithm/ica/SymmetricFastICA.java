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
package org.openimaj.math.matrix.algorithm.ica;

import java.util.Arrays;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.array.ArrayUtils;

import Jama.Matrix;

public class SymmetricFastICA extends IndependentComponentAnalysis {
	enum NonlinearFunction {
		tanh, pow3, rat1, rat2, gaus
	}

	double epsilon = 0.0001;
	double MaxIt = 100;
	NonlinearFunction g;

	Matrix W;
	private Matrix icasig;

	@Override
	public Matrix getSignalToInterferenceMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix getDemixingMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix getIndependentComponentMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void estimateComponentsWhitened(Matrix Z, double[] mean, Matrix X, Matrix CC) {
		final int dim = X.getRowDimension();
		final int N = X.getColumnDimension();

		final double[] crit = new double[dim];
		int NumIt = 0;
		Matrix WOld = W;

		while (1 - ArrayUtils.minValue(crit) > epsilon && NumIt < MaxIt) {
			NumIt = NumIt + 1;

			switch (g) {
			case tanh:
				final Matrix hypTan = MatrixUtils.tanh(Z.transpose().times(W));
				// W=Z*hypTan/N-ones(dim,1)*sum(1-hypTan.^2).*W/N;

				final double[] sumv = new double[hypTan.getColumnDimension()];
				for (int r = 0; r < hypTan.getRowDimension(); r++) {
					for (int c = 0; c < hypTan.getColumnDimension(); c++) {
						sumv[c] += 1 - hypTan.get(r, c) * hypTan.get(r, c);
					}
				}
				final Matrix weight = new Matrix(W.getRowDimension(), W.getColumnDimension());
				for (int r = 0; r < weight.getRowDimension(); r++) {
					for (int c = 0; c < weight.getColumnDimension(); c++) {
						weight.set(r, c, W.get(r, c) * sumv[c] / N);
					}
				}

				W = MatrixUtils.times(Z.times(hypTan), 1.0 / N).minus(weight);

				break;
			// case pow3:
			// W=(Z*((Z'*W).^ 3))/N-3*W;
			// break;
			// case rat1:
			// U=Z'*W;
			// Usquared=U.^2;
			// RR=4./(4+Usquared);
			// Rati=U.*RR;
			// Rati2=Rati.^2;
			// dRati=RR-Rati2/2;
			// nu=mean(dRati);
			// hlp=Z*Rati/N;
			// W=hlp-ones(dim,1)*nu.*W;
			// break;
			// case rat2:
			// U=Z'*W;
			// Ua=1+sign(U).*U;
			// r1=U./Ua;
			// r2=r1.*sign(r1);
			// Rati=r1.*(2-r2);
			// dRati=(2./Ua).*(1-r2.*(2-r2));
			// nu=mean(dRati);
			// hlp=Z*Rati/N;
			// W=hlp-ones(dim,1)*nu.*W;
			// break;
			// case gaus:
			// U=Z'*W;
			// Usquared=U.^2;
			// ex=exp(-Usquared/2);
			// gauss=U.*ex;
			// dGauss=(1-Usquared).*ex;
			// W=Z*gauss/N-ones(dim,1)*sum(dGauss).*W/N;
			// break;
			}

			// decorrelate W
			// fast symmetric orthogonalization
			final Matrix WtW = W.transpose().times(W);
			W = W.times(MatrixUtils.invSqrtSym(WtW));

			for (int c = 0; c < W.getColumnDimension(); c++) {
				crit[c] = 0;
				for (int r = 0; r < W.getRowDimension(); r++) {
					crit[r] += W.get(r, c) * WOld.get(r, c);
				}
				crit[c] = Math.abs(crit[c]);
			}

			WOld = W;
		}

		// estimate signals
		// s=W'*Z;
		// s = Zt.times(Wt.transpose());

		W = W.transpose().times(CC);

		// icasig=Wefica*X + (Wefica*Xmean)*ones(1,N);
		final Matrix WXmean = W.times(new Matrix(new double[][] { mean }).transpose());
		final Matrix delta = WXmean.times(MatrixUtils.ones(1, N));
		icasig = W.times(X).plus(delta);
	}

	public static void main(String[] args) {
		final int dim = 1000;
		final double[] signal1 = new double[dim];
		final double[] signal2 = new double[dim];
		for (int i = 0; i < dim; i++) {
			signal1[i] = Math.cos(i);
			signal2[i] = Math.tan(i);
		}

		final double[] mix1 = new double[dim];
		final double[] mix2 = new double[dim];
		for (int i = 0; i < dim; i++) {
			mix1[i] = signal1[i] + 0.8 * signal2[i];
			mix2[i] = signal2[i] + 0.5 * signal1[i];
		}

		System.out.println("a=" + Arrays.toString(signal1));
		System.out.println("b=" + Arrays.toString(signal2));
		System.out.println("mixa=" + Arrays.toString(mix1));
		System.out.println("mixb=" + Arrays.toString(mix2));

		final Matrix data = new Matrix(new double[][] { mix1, mix2 });
		final SymmetricFastICA symfica = new SymmetricFastICA();
		symfica.g = NonlinearFunction.tanh;
		symfica.W = Matrix.identity(2, 2);

		symfica.estimateComponents(data);

		symfica.W.print(5, 5);
		symfica.icasig.print(5, 5);
	}
}
