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
package org.openimaj.workinprogress.featlearn;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix.Norm;
import no.uib.cipr.matrix.Vector;

import org.openimaj.workinprogress.optimisation.DifferentiableObjectiveFunction;
import org.openimaj.workinprogress.optimisation.params.VectorParameters;

public class SparseAutoencoder
		implements
		DifferentiableObjectiveFunction<SparseAutoencoder.Model, double[], VectorParameters>
{
	public static class Model {
		DenseMatrix w1;
		DenseMatrix w2;
		DenseVector b1;
		DenseVector b2;

		public Model(int nvisible, int nhidden) {
			w1 = (DenseMatrix) Matrices.random(nhidden, nhidden);
			w2 = (DenseMatrix) Matrices.random(nvisible, nhidden);
			b1 = (DenseVector) Matrices.random(nhidden);
			b2 = (DenseVector) Matrices.random(nvisible);
		}

		double[] feedforward(double[] input) {
			final DenseVector iv = new DenseVector(input, false);
			final DenseVector a1 = sigmoid(w1.multAdd(iv, b1.copy()));
			final DenseVector a2 = sigmoid(w2.multAdd(a1, b2.copy()));

			return a2.getData();
		}

		double[][] getLayerActivations(double[] input) {
			final DenseVector iv = new DenseVector(input, false);
			final DenseVector a1 = sigmoid(w1.multAdd(iv, b1.copy()));
			final DenseVector a2 = sigmoid(w2.multAdd(a1, b2.copy()));
			return new double[][] { a1.getData(), a2.getData() };
		}

		private DenseVector sigmoid(Vector vector) {
			final DenseVector out = (DenseVector) vector.copy();

			final double[] xd = out.getData();

			for (int i = 0; i < xd.length; i++)
				xd[i] = 1 / (1 + Math.exp(-xd[i]));

			return out;
		}
	}

	private double lamda; // regularization for weight decay

	@Override
	public double value(Model model, double[] data) {
		final double[] predict = model.feedforward(data);

		double err = 0;
		for (int i = 0; i < predict.length; i++) {
			final double diff = predict[i] - data[i];
			err += (diff * diff);
		}

		// Note that this is rather expensive each iter... can it be improved?
		final double n1 = model.w1.norm(Norm.Frobenius);
		final double n2 = model.w2.norm(Norm.Frobenius);
		final double reg = n1 * n1 + n2 * n2;

		return 0.5 * (err + lamda * reg);
	}

	@Override
	public VectorParameters derivative(Model model, double[] data) {
		final double[][] as = model.getLayerActivations(data);

		final double[] d2 = new double[as[1].length];
		for (int i = 0; i < d2.length; i++) {
			d2[i] = -(data[i] - as[1][i]) * (as[1][i] * (1 - as[1][i]));
		}

		final DenseVector wd = (DenseVector) model.w1.transMult(new DenseVector(d2),
				new DenseVector(model.w1.numColumns()));
		final double[] d1 = wd.getData();
		for (int i = 0; i < d1.length; i++) {
			d1[i] *= (as[0][i] * (1 - as[0][i]));
		}

		return null;
	}

	@Override
	public void updateModel(Model model, VectorParameters weights) {
		// TODO Auto-generated method stub

	}
}
