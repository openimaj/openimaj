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
package org.openimaj.ml.linear.experiments.sinabill;

import gov.sandia.cognition.math.matrix.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator.Mode;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.evaluation.RootMeanSumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.init.HardCodedInitStrat;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.util.pair.Pair;

public class StreamAustrianDampeningExperiments extends BilinearExperiment {

	// private static final String BATCH_EXPERIMENT =
	// "batchStreamLossExperiments/batch_1366231606223/experiment.log";
	private static final String BATCH_EXPERIMENT = "batchStreamLossExperiments/batch_1366820115090/experiment.log";

	@Override
	public String getExperimentName() {
		return "streamingDampeningExperiments";
	}

	@Override
	public void performExperiment() throws Exception {

		final Map<Integer, Double> batchLosses = loadBatchLoss();
		final BilinearLearnerParameters params = new BilinearLearnerParameters();
		params.put(BilinearLearnerParameters.ETA0_U, 0.01);
		params.put(BilinearLearnerParameters.ETA0_W, 0.01);
		params.put(BilinearLearnerParameters.LAMBDA, 0.001);
		params.put(BilinearLearnerParameters.LAMBDA_W, 0.006);
		params.put(BilinearLearnerParameters.BICONVEX_TOL, 0.01);
		params.put(BilinearLearnerParameters.BICONVEX_MAXITER, 10);
		params.put(BilinearLearnerParameters.BIAS, true);
		params.put(BilinearLearnerParameters.ETA0_BIAS, 0.5);
		params.put(BilinearLearnerParameters.WINITSTRAT, new SingleValueInitStrat(0.1));
		params.put(BilinearLearnerParameters.UINITSTRAT, new SparseZerosInitStrategy());
		final HardCodedInitStrat biasInitStrat = new HardCodedInitStrat();
		params.put(BilinearLearnerParameters.BIASINITSTRAT, biasInitStrat);
		final BillMatlabFileDataGenerator bmfdg = new BillMatlabFileDataGenerator(
				new File(MATLAB_DATA()),
				98,
				true
				);
		prepareExperimentLog(params);
		double dampening = 0.02d;
		final double dampeningIncr = 0.1d;
		final double dampeningMax = 0.021d;
		final int maxItems = 15;
		logger.debug(
				String.format(
						"Beggining dampening experiments: min=%2.5f,max=%2.5f,incr=%2.5f",
						dampening,
						dampeningMax,
						dampeningIncr

						));
		while (dampening < dampeningMax) {
			params.put(BilinearLearnerParameters.DAMPENING, dampening);
			logger.debug("Dampening is now: " + dampening);
			final BilinearSparseOnlineLearner learner = new BilinearSparseOnlineLearner(params);
			dampening += dampeningIncr;
			int item = 0;
			final BilinearEvaluator eval = new RootMeanSumLossEvaluator();
			eval.setLearner(learner);
			bmfdg.setFold(-1, Mode.ALL); // go through all items in day order
			boolean first = true;
			while (true) {
				final Pair<Matrix> next = bmfdg.generate();
				if (next == null)
					break;
				if (first) {
					first = false;
					biasInitStrat.setMatrix(next.secondObject());
				}
				final List<Pair<Matrix>> asList = new ArrayList<Pair<Matrix>>();
				asList.add(next);
				if (learner.getW() != null) {
					if (!batchLosses.containsKey(item)) {
						logger.debug(String.format("...No batch result found for: %d, done", item));
						break;
					}
					logger.debug("...Calculating regret for item" + item);
					final double loss = eval.evaluate(asList);
					logger.debug(String.format("... loss: %f", loss));
					final double batchloss = batchLosses.get(item);
					logger.debug(String.format("... batch loss: %f", batchloss));
					logger.debug(String.format("... regret: %f", (loss - batchloss)));
				}
				if (item >= maxItems)
					break;
				learner.process(next.firstObject(), next.secondObject());
				final Matrix w = learner.getW();
				final Matrix u = learner.getU();
				logger.debug("W row sparcity: " + CFMatrixUtils.rowSparsity(w));
				logger.debug(String.format("W range: %2.5f -> %2.5f", CFMatrixUtils.min(w), CFMatrixUtils.max(w)));
				logger.debug("U row sparcity: " + CFMatrixUtils.rowSparsity(u));
				logger.debug(String.format("U range: %2.5f -> %2.5f", CFMatrixUtils.min(u), CFMatrixUtils.max(u)));

				logger.debug(String.format("... loss (post addition): %f", eval.evaluate(asList)));
				logger.debug(String.format("Saving learner, Fold %d, Item %d", -1, item));
				final File learnerOut = new File(FOLD_ROOT(-1), String.format("learner_%d", item));
				IOUtils.writeBinary(learnerOut, learner);

				item++;
			}

		}
	}

	private Map<Integer, Double> loadBatchLoss() throws IOException {
		final String[] batchExperimentLines = FileUtils.readlines(new File(
				DATA_ROOT(),
				BATCH_EXPERIMENT
				));
		int seenItems = 0;
		final Map<Integer, Double> ret = new HashMap<Integer, Double>();
		for (final String line : batchExperimentLines) {

			if (line.contains("New Item Seen: ")) {
				seenItems = Integer.parseInt(line.split(":")[1].trim());
			}

			if (line.contains("Loss:")) {
				ret.put(seenItems, Double.parseDouble(line.split(":")[1].trim()));
			}
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		final BilinearExperiment exp = new StreamAustrianDampeningExperiments();
		exp.performExperiment();
	}

}
