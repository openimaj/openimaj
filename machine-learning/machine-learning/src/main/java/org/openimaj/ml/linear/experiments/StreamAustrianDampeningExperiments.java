package org.openimaj.ml.linear.experiments;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.SandiaMatrixUtils;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator.Mode;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.evaluation.RootMeanSumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.init.HardCodedInitStrat;
import org.openimaj.ml.linear.learner.init.OnesInitStrategy;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseOnesInitStrategy;
import org.openimaj.ml.linear.learner.init.SparseRowOnesInitStrategy;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.util.pair.Pair;

public class StreamAustrianDampeningExperiments extends BilinearExperiment{
	
//	private static final String BATCH_EXPERIMENT = "batchStreamLossExperiments/batch_1366231606223/experiment.log";
	private static final String BATCH_EXPERIMENT = "batchStreamLossExperiments/batch_1366820115090/experiment.log";
	
	
	public String getExperimentName() {
		return "streamingDampeningExperiments";
	}
	@Override
	public void performExperiment() throws Exception {
		
		Map<Integer,Double> batchLosses = loadBatchLoss();
		BilinearLearnerParameters params = new BilinearLearnerParameters();
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
		HardCodedInitStrat biasInitStrat = new HardCodedInitStrat();
		params.put(BilinearLearnerParameters.BIASINITSTRAT, biasInitStrat);
		BillMatlabFileDataGenerator bmfdg = new BillMatlabFileDataGenerator(
				new File(BILL_DATA()), 
				98,
				true
		);
		prepareExperimentLog(params);
		double dampening = 0.02d;
		double dampeningIncr = 0.1d;
		double dampeningMax = 0.021d;
		int maxItems = 15;
		logger.debug(
			String.format(
				"Beggining dampening experiments: min=%2.5f,max=%2.5f,incr=%2.5f",
				dampening,
				dampeningMax,
				dampeningIncr
			
		));
		while(dampening < dampeningMax){
			params.put(BilinearLearnerParameters.DAMPENING, dampening);
			logger.debug("Dampening is now: " + dampening);
			BilinearSparseOnlineLearner learner = new BilinearSparseOnlineLearner(params);
			dampening+=dampeningIncr;
			int item = 0;
			BilinearEvaluator eval = new RootMeanSumLossEvaluator();
			eval.setLearner(learner);
			bmfdg.setFold(-1, Mode.ALL); // go through all items in day order
			boolean first = true;
			while(true){
				Pair<Matrix> next = bmfdg.generate();
				if(next == null) break;
				if(first){
					first = false;
					biasInitStrat.setMatrix(next.secondObject());
				}
				List<Pair<Matrix>> asList = new ArrayList<Pair<Matrix>>();
				asList.add(next);
				if(learner.getW() != null){
					if(!batchLosses.containsKey(item)){
						logger.debug(String.format("...No batch result found for: %d, done",item));
						break;
					}
					logger.debug("...Calculating regret for item"+item);				
					double loss = eval.evaluate(asList);
					logger.debug(String.format("... loss: %f",loss));
					double batchloss = batchLosses.get(item);
					logger.debug(String.format("... batch loss: %f",batchloss));
					logger.debug(String.format("... regret: %f",(loss-batchloss)));
				}
				if(item >= maxItems) break;
				learner.process(next.firstObject(), next.secondObject());
				Matrix w = learner.getW();
				Matrix u = learner.getU();
				logger.debug("W row sparcity: " + SandiaMatrixUtils.rowSparcity(w));
				logger.debug(String.format("W range: %2.5f -> %2.5f",SandiaMatrixUtils.min(w), SandiaMatrixUtils.max(w)));
				logger.debug("U row sparcity: " + SandiaMatrixUtils.rowSparcity(u));
				logger.debug(String.format("U range: %2.5f -> %2.5f",SandiaMatrixUtils.min(u), SandiaMatrixUtils.max(u)));
				
				logger.debug(String.format("... loss (post addition): %f",eval.evaluate(asList)));
				logger.debug(String.format("Saving learner, Fold %d, Item %d",-1, item));
				File learnerOut = new File(FOLD_ROOT(-1),String.format("learner_%d",item));
				IOUtils.writeBinary(learnerOut, learner);
				
				item++;
			}
			
		}
	}

	private Map<Integer, Double> loadBatchLoss() throws IOException {
		String[] batchExperimentLines = FileUtils.readlines(new File(
			BILL_DATA_ROOT(),
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
		BilinearExperiment exp = new StreamAustrianDampeningExperiments();
		exp.performExperiment();
	}

}
