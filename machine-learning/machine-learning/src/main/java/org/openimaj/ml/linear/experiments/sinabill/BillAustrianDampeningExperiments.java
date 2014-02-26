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
import gov.sandia.cognition.math.matrix.MatrixFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator.Mode;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.evaluation.RootMeanSumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.util.pair.Pair;


public class BillAustrianDampeningExperiments extends BilinearExperiment{
		
	public static void main(String[] args) throws Exception {
		BilinearExperiment exp = new BillAustrianDampeningExperiments();
		exp.performExperiment();
	}

	@Override
	public void performExperiment() throws IOException {
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
//		params.put(BilinearLearnerParameters.DAMPENING, 0.1);
		BillMatlabFileDataGenerator bmfdg = new BillMatlabFileDataGenerator(
				new File(MATLAB_DATA()), 
				98,
				true
		);
		prepareExperimentLog(params);
		int foldNumber = 5;
		logger.debug("Starting dampening experiments");
		logger.debug("Fold: " + foldNumber);
		bmfdg.setFold(foldNumber, Mode.TEST);
		List<Pair<Matrix>> testpairs = new ArrayList<Pair<Matrix>>(); 
		while(true){
			Pair<Matrix> next = bmfdg.generate();
			if(next == null) break;
			testpairs.add(next);
		}
		double dampening = 0d;
		double dampeningIncr = 0.0001d;
		double dampeningMax = 0.02d;
		logger.debug(
			String.format(
				"Beggining dampening experiments: min=%2.5f,max=%2.5f,incr=%2.5f",
				dampening,
				dampeningMax,
				dampeningIncr
			
		));
		while(dampening < dampeningMax){
			params.put(BilinearLearnerParameters.DAMPENING, dampening);
			BilinearSparseOnlineLearner learner = new BilinearSparseOnlineLearner(params);
			learner.reinitParams();
			
			logger.debug("Dampening is now: " + dampening);
			logger.debug("...training");
			bmfdg.setFold(foldNumber, Mode.TRAINING);
			int j = 0;
			while(true){
				Pair<Matrix> next = bmfdg.generate();
				if(next == null) break;
				logger.debug("...trying item "+j++);
				learner.process(next.firstObject(), next.secondObject());
				Matrix u = learner.getU();
				Matrix w = learner.getW();
				Matrix bias = MatrixFactory.getDenseDefault().copyMatrix(learner.getBias());
				BilinearEvaluator eval = new RootMeanSumLossEvaluator();
				eval.setLearner(learner);
				double loss = eval.evaluate(testpairs);
				logger.debug(String.format("Saving learner, Fold %d, Item %d",foldNumber, j));
				File learnerOut = new File(FOLD_ROOT(foldNumber),String.format("learner_%d_dampening=%2.5f",j,dampening));
				IOUtils.writeBinary(learnerOut, learner);
				logger.debug("W row sparcity: " + CFMatrixUtils.rowSparsity(w));
				logger.debug(String.format("W range: %2.5f -> %2.5f",CFMatrixUtils.min(w), CFMatrixUtils.max(w)));
				logger.debug("U row sparcity: " + CFMatrixUtils.rowSparsity(u));
				logger.debug(String.format("U range: %2.5f -> %2.5f",CFMatrixUtils.min(u), CFMatrixUtils.max(u)));
				Boolean biasMode = learner.getParams().getTyped(BilinearLearnerParameters.BIAS);
				if(biasMode){
					logger.debug("Bias: " + CFMatrixUtils.diag(bias));
				}
				logger.debug(String.format("... loss: %f",loss));
			}
			
			dampening+=dampeningIncr;
		}
	}
	
} 


