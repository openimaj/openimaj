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
