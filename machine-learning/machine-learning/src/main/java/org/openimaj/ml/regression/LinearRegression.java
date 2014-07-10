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
package org.openimaj.ml.regression;

import java.util.Arrays;
import java.util.List;

import no.uib.cipr.matrix.NotConvergedException;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.model.EstimatableModel;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Given a set of independant variables a linear regressions finds the optimal
 * vector B such that: (Y - Xb)^2 = 0 (Y - Xb)^{T}(Y-Xb) = 0
 * 
 * calculated by assuming a convex shape of (Y - Xb) with varying values of b
 * (reasonable as the function is linear) and then calculating the point at
 * which the first derivative of this function is 0. i.e.:
 * 
 * d/db (y - Xb)^{T} (y - Xb) = -X^{T}(y - Xb) - X^{T}(y - Xb) = - 2 * X^{T}(y -
 * Xb)
 * 
 * which at the 0 is: - 2 * X^{T}(y - Xb) = 0 X^{T}(y - Xb) X^{T}y - X^{T}Xb = 0
 * X^{T}y = X^{T}Xb b = (X^{T} X)^{-1} X^{T} y
 * 
 * Calculating this function directly behaves numerically badly when X is
 * extremely skinny and tall (i.e. lots of data, fewer dimentions) so we
 * calculate this using the SVD, using the SVD we can decompose X as:
 * 
 * X = UDV^{T}
 * 
 * s.t. U and V are orthonormal from this we can calculate: b = V D^{-1} U^{T} y
 * 
 * which is equivilant but more numerically stable.
 * 
 * Note that upon input any vector of independent variables x_n are
 * automatically to turned into an n + 1 vector {1,x0,x1,...,xn} which handles
 * the constant values added to y
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LinearRegression implements EstimatableModel<double[], double[]> {

	private Matrix weights;

	/**
	 * linear regression model
	 */
	public LinearRegression() {
	}

	@Override
	public boolean estimate(List<? extends IndependentPair<double[], double[]>> data) {
		if (data.size() == 0)
			return false;

		final int correctedx = data.get(0).firstObject().length + 1;
		final int correctedy = data.get(0).secondObject().length;
		final double[][] y = new double[data.size()][correctedy];
		final double[][] x = new double[data.size()][correctedx];

		int i = 0;
		for (final IndependentPair<double[], double[]> item : data) {
			y[i] = item.secondObject();
			x[i][0] = 1;
			System.arraycopy(item.firstObject(), 0, x[i], 1, item.firstObject().length);
			i += 1;
		}

		estimate_internal(new Matrix(y), new Matrix(x));

		return true;
	}

	/**
	 * As in {@link #estimate(List)} but using double arrays for efficiency.
	 * 
	 * @param yd
	 * @param xd
	 */
	public void estimate(double[][] yd, double[][] xd) {
		final double[][] x = appendConstant(xd);
		estimate_internal(new Matrix(yd), new Matrix(x));
	}

	private double[][] appendConstant(double[][] xd) {
		final int corrected = xd[0].length + 1;
		final double[][] x = new double[xd.length][corrected];

		for (int i = 0; i < xd.length; i++) {
			x[i][0] = 1;
			System.arraycopy(xd[i], 0, x[i], 1, xd[i].length);
		}
		return x;
	}

	/**
	 * As in {@link #estimate(List)} but using double arrays for efficiency.
	 * Estimates: b = V D^{-1} U^{T} y s.t. X = UDV^{T}
	 * 
	 * @param y
	 * @param x
	 */
	public void estimate(Matrix y, Matrix x) {
		estimate(y.getArray(), x.getArray());
	}

	private void estimate_internal(Matrix y, Matrix x) {
		try {
			final no.uib.cipr.matrix.DenseMatrix mjtX = new no.uib.cipr.matrix.DenseMatrix(x.getArray());
			no.uib.cipr.matrix.SVD svd;
			svd = no.uib.cipr.matrix.SVD.factorize(mjtX);
			final Matrix u = MatrixUtils.convert(svd.getU(), svd.getU().numRows(), svd.getS().length);
			final Matrix v = MatrixUtils.convert(svd.getVt(), svd.getS().length, svd.getVt().numColumns()).transpose();
			final Matrix d = MatrixUtils.diag(svd.getS());

			weights = v.times(MatrixUtils.pseudoInverse(d)).times(u.transpose()).times(y);
		} catch (final NotConvergedException e) {
			throw new RuntimeException(e.getMessage());
		}

	}

	@Override
	public double[] predict(double[] data) {
		final double[][] corrected = new double[][] { new double[data.length + 1] };
		corrected[0][0] = 1;
		System.arraycopy(data, 0, corrected[0], 1, data.length);
		final Matrix x = new Matrix(corrected);

		return x.times(this.weights).transpose().getArray()[0];
	}

	/**
	 * Helper function which adds the constant component to x and returns
	 * predicted values for y, one per row
	 * 
	 * @param x
	 * @return predicted y
	 */
	public Matrix predict(Matrix x) {
		x = new Matrix(appendConstant(x.getArray()));
		return x.times(this.weights);
	}

	@Override
	public int numItemsToEstimate() {
		return 2;
	}

	@Override
	public LinearRegression clone() {
		return new LinearRegression();
	}

	@Override
	public boolean equals(Object obj) {
		if ((!(obj instanceof LinearRegression)))
			return false;
		final LinearRegression that = (LinearRegression) obj;
		final double[][] thatw = that.weights.getArray();
		final double[][] thisw = this.weights.getArray();
		for (int i = 0; i < thisw.length; i++) {
			if (!Arrays.equals(thatw[i], thisw[i]))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "LinearRegression with coefficients: " + Arrays.toString(this.weights.transpose().getArray()[0]);
	}

}
