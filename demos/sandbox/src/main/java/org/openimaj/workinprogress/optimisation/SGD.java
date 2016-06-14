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
