package org.openimaj.workinprogress.optimisation;

import java.util.Random;

import org.openimaj.data.DataSource;
import org.openimaj.data.DoubleArrayBackedDataSource;
import org.openimaj.workinprogress.optimisation.params.Parameters;
import org.openimaj.workinprogress.optimisation.params.VectorParameters;

import scala.actors.threadpool.Arrays;

public class SGD<MODEL, DATATYPE, PTYPE extends Parameters<PTYPE>> {
	public int maxEpochs = 100;
	public int batchSize = 1;
	public LearningRate<PTYPE> learningRate;
	public MODEL model;
	public DifferentiableObjectiveFunction<MODEL, DATATYPE, PTYPE> fcn;

	public void train(DataSource<DATATYPE> data) {
		final DATATYPE[] batch = data.createTemporaryArray(batchSize);

		for (int e = 0; e < maxEpochs; e++) {
			for (int i = 0; i < data.size(); i += batchSize) {
				final int currentBatchStop = Math.min(data.size(), i + batchSize);
				final int currentBatchSize = currentBatchStop - i;
				data.getData(i, currentBatchStop, batch);

				final PTYPE grads = fcn.derivative(model, batch[0]);
				for (int j = 1; j < currentBatchSize; j++) {
					grads.addInplace(fcn.derivative(model, batch[j]));
				}
				grads.multiplyInplace(learningRate.getRate(e, i, grads));
				fcn.updateModel(model, grads);
			}
		}
	}

	public double value(MODEL model, DATATYPE data) {
		return 0;
	}

	public static void main(String[] args) {
		final double[][] data = new double[1000][2];
		final Random rng = new Random();
		for (int i = 0; i < data.length; i++) {
			final double x = rng.nextDouble();
			data[i][0] = x;
			data[i][1] = 0.3 * x + 20 + (rng.nextGaussian() * 0.01);
		}
		final DoubleArrayBackedDataSource ds = new DoubleArrayBackedDataSource(data);

		final double[] model = { 0, 0 };

		final DifferentiableObjectiveFunction<double[], double[], VectorParameters> fcn = new DifferentiableObjectiveFunction<double[], double[], VectorParameters>()
		{
			@Override
			public double value(double[] model, double[] data) {
				final double diff = data[1] - (model[0] * data[0] + model[1]);
				return diff * diff;
			}

			@Override
			public VectorParameters derivative(double[] model, double[] data) {
				final double[] der = {
						2 * data[0] * (-data[1] + model[0] * data[0] + model[1]),
						2 * (-data[1] + model[0] * data[0] + model[1])
				};

				return new VectorParameters(der);
			}

			@Override
			public void updateModel(double[] model, VectorParameters weights) {
				model[0] -= weights.vector[0];
				model[1] -= weights.vector[1];
			}
		};

		final SGD<double[], double[], VectorParameters> sgd = new SGD<double[], double[], VectorParameters>();
		sgd.model = model;
		sgd.fcn = fcn;
		sgd.learningRate = new StaticLearningRate<VectorParameters>(0.01);
		sgd.batchSize = 1;
		sgd.maxEpochs = 10;

		sgd.train(ds);

		System.out.println(Arrays.toString(model));
	}
}
