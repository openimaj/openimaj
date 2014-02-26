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
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator.Mode;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.evaluation.RootMeanSumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.util.pair.Pair;

public class RegretExperiment extends BilinearExperiment{
	
	private static final String BATCH_EXPERIMENT = "batchStreamLossExperiments/batch_1366231606223/experiment.log";

	@Override
	public void performExperiment() throws Exception {
		
		Map<Integer,Double> batchLosses = loadBatchLoss();
		BilinearLearnerParameters params = new BilinearLearnerParameters();
		params.put(BilinearLearnerParameters.ETA0_U, 0.02);
		params.put(BilinearLearnerParameters.ETA0_W, 0.02);
		params.put(BilinearLearnerParameters.LAMBDA, 0.001);
		params.put(BilinearLearnerParameters.BICONVEX_TOL, 0.01);
		params.put(BilinearLearnerParameters.BICONVEX_MAXITER, 10);
		params.put(BilinearLearnerParameters.BIAS, true);
		params.put(BilinearLearnerParameters.ETA0_BIAS, 0.5);
		params.put(BilinearLearnerParameters.WINITSTRAT, new SingleValueInitStrat(0.1));
		params.put(BilinearLearnerParameters.UINITSTRAT, new SparseZerosInitStrategy());
		BillMatlabFileDataGenerator bmfdg = new BillMatlabFileDataGenerator(
				new File(MATLAB_DATA()), 
				98,
				true
		);
		prepareExperimentLog(params);
		BilinearSparseOnlineLearner learner = new BilinearSparseOnlineLearner(params);
		bmfdg.setFold(-1, Mode.ALL); // go through all items in day order
		int j = 0;
		BilinearEvaluator eval = new RootMeanSumLossEvaluator();
		eval.setLearner(learner);
		while(true){
			Pair<Matrix> next = bmfdg.generate();
			if(next == null) break;
			List<Pair<Matrix>> asList = new ArrayList<Pair<Matrix>>();
			asList.add(next);
			if(learner.getW() != null){
				if(!batchLosses.containsKey(j)){
					logger.debug(String.format("...No batch result found for: %d, done",j));
					break;
				}
				logger.debug("...Calculating regret for item"+j);				
				double loss = eval.evaluate(asList);
				logger.debug(String.format("... loss: %f",loss));
				double batchloss = batchLosses.get(j);
				logger.debug(String.format("... batch loss: %f",batchloss));
				logger.debug(String.format("... regret: %f",(loss-batchloss)));
			}
			learner.process(next.firstObject(), next.secondObject());
			logger.debug(String.format("... loss (post addition): %f",eval.evaluate(asList)));
			logger.debug(String.format("Saving learner, Fold %d, Item %d",-1, j));
			File learnerOut = new File(FOLD_ROOT(-1),String.format("learner_%d",j));
			IOUtils.writeBinary(learnerOut, learner);
			
			j++;
		}
	}

	private Map<Integer, Double> loadBatchLoss() throws IOException {
		String[] batchExperimentLines = FileUtils.readlines(new File(
			DATA_ROOT(),
			BATCH_EXPERIMENT
		));
		int seenItems = 0;
		Map<Integer, Double> ret = new HashMap<Integer, Double>();
		for (String line : batchExperimentLines) {
			
			if(line.contains("New Item Seen: ")){
				seenItems = Integer.parseInt(line.split(":")[1].trim());
			}
			
			if(line.contains("Loss:")){
				ret.put(seenItems, Double.parseDouble(line.split(":")[1].trim()));
			}
		}
		return ret;
	}
	
	public static void main(String[] args) throws Exception {
		BilinearExperiment exp = new RegretExperiment();
		exp.performExperiment();
	}

}
