package org.openimaj.ml.linear.experiments;

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
				new File(BILL_DATA()), 
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
		BilinearExperiment exp = new RegretExperiment();
		exp.performExperiment();
	}

}
