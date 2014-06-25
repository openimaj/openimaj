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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class LinearRegressionTest {
	@Test
	public void testLinearRegression() throws Exception {
		final double[][] xt = new double[][] { new double[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 } };
		// function params
		final double m = 2;
		final double c = -2;

		final double[][] perfectyt = new double[1][xt[0].length];
		for (int i = 0; i < perfectyt[0].length; i++) {
			perfectyt[0][i] = xt[0][i] * m + c;
		}

		final Matrix x = new Matrix(xt).transpose();
		final Matrix perfecty = new Matrix(perfectyt).transpose();

		final Matrix noise = new Matrix(RandomData.getRandomDoubleArray(perfecty.getRowDimension(), 1, -1, 1, 1));
		final Matrix y = perfecty.plus(noise);

		final List<IndependentPair<double[], double[]>> ipairs = aspairs(x, y);

		final LinearRegression lr = new LinearRegression();
		lr.estimate(y, x);

		final LinearRegression lr2 = new LinearRegression();
		lr2.estimate(ipairs);
		assertTrue(lr.equals(lr2));
	}

	private List<IndependentPair<double[], double[]>> aspairs(Matrix x, Matrix y) {
		final List<IndependentPair<double[], double[]>> ret = new ArrayList<IndependentPair<double[], double[]>>();
		final double[][] xd = x.getArray();
		final double[][] yd = y.getArray();
		for (int i = 0; i < xd.length; i++) {
			ret.add(IndependentPair.pair(xd[i], yd[i]));
		}
		return ret;
	}
}
