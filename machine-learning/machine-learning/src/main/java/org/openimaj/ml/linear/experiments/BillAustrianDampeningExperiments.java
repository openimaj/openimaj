package org.openimaj.ml.linear.experiments;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.SandiaMatrixUtils;
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
				new File(BILL_DATA()), 
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
				logger.debug("W row sparcity: " + SandiaMatrixUtils.rowSparcity(w));
				logger.debug(String.format("W range: %2.5f -> %2.5f",SandiaMatrixUtils.min(w), SandiaMatrixUtils.max(w)));
				logger.debug("U row sparcity: " + SandiaMatrixUtils.rowSparcity(u));
				logger.debug(String.format("U range: %2.5f -> %2.5f",SandiaMatrixUtils.min(u), SandiaMatrixUtils.max(u)));
				Boolean biasMode = learner.getParams().getTyped(BilinearLearnerParameters.BIAS);
				if(biasMode){
					logger.debug("Bias: " + SandiaMatrixUtils.diag(bias));
				}
				logger.debug(String.format("... loss: %f",loss));
			}
			
			dampening+=dampeningIncr;
		}
	}
	
} 


