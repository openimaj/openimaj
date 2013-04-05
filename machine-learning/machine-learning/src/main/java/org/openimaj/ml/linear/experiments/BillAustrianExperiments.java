package org.openimaj.ml.linear.experiments;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.SandiaMatrixUtils;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator.Mode;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.evaluation.RootMeanSumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.init.SparseOnesInitStrategy;
import org.openimaj.ml.linear.learner.init.SparseRowOnesInitStrategy;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.util.pair.Pair;

public class BillAustrianExperiments {
	private static final String EXPERIMENT_NAME = "%s/streamingExperiments/experiment_%s";
	private static final String PARAMS_NAME = ".paramsascii";
	private static final String PARAMS_DATA_NAME = ".params";

	private static final String BILL_DATA_ROOT = "%s/TrendMiner/deliverables/year2-18month/Austrian Data/";
	private static final String BILL_DATA = "%s/data.mat";
	
	static Logger logger = Logger.getLogger(BillAustrianExperiments.class);
	
	private static void prepareExperimentLog(BilinearLearnerParameters params) throws IOException {
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "[%p->%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.DEBUG);
		console.activateOptions();
	  	// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
		String experimentRoot = String.format(EXPERIMENT_NAME,BILL_DATA_ROOT(),""+System.currentTimeMillis());
		File expRoot = new File(experimentRoot);
		logger.debug("Experiment root: " + expRoot);
		if(!expRoot.mkdirs()) throw new IOException("Couldn't prepare experiment output");
		IOUtils.write(params, new DataOutputStream(new FileOutputStream(new File(expRoot,PARAMS_DATA_NAME))));
		IOUtils.writeASCII(new File(expRoot,PARAMS_NAME), params);
		
		File logFile = new File(expRoot,"log");
		if(logFile.exists())logFile.delete();
		FileAppender file = new FileAppender(new PatternLayout(PATTERN), logFile.getAbsolutePath()); 
		file.setThreshold(Level.DEBUG);
		file.activateOptions();
		Logger.getRootLogger().addAppender(file );
		
	}
	
	public static void main(String[] args) throws IOException {
		
		BilinearLearnerParameters params = new BilinearLearnerParameters();
		params.put(BilinearLearnerParameters.ETA0_U, 0.0002);
		params.put(BilinearLearnerParameters.ETA0_W, 0.0002);
		params.put(BilinearLearnerParameters.LAMBDA, 0.001);
		params.put(BilinearLearnerParameters.BICONVEX_TOL, 0.01);
		params.put(BilinearLearnerParameters.BICONVEX_MAXITER, 10);
		params.put(BilinearLearnerParameters.BIAS, true);
		params.put(BilinearLearnerParameters.BIASETA0, 0.05);
		Random initRandom = new Random(1);
		params.put(BilinearLearnerParameters.WINITSTRAT, new SparseOnesInitStrategy(0.6,initRandom));
		params.put(BilinearLearnerParameters.UINITSTRAT, new SparseZerosInitStrategy());
		BillMatlabFileDataGenerator bmfdg = new BillMatlabFileDataGenerator(new File(BILL_DATA()), 98);
		prepareExperimentLog(params);
		for (int i = 0; i < bmfdg.nFolds(); i++) {
			logger.debug("Fold: " + i);
			BilinearSparseOnlineLearner learner = new BilinearSparseOnlineLearner(params);
			learner.reinitParams();
			
			bmfdg.setFold(i, Mode.TEST);
			List<Pair<Matrix>> testpairs = new ArrayList<Pair<Matrix>>(); 
			while(true){
				Pair<Matrix> next = bmfdg.generate();
				if(next == null) break;
				testpairs.add(next);
			}
			logger.debug("...training");
			bmfdg.setFold(i, Mode.TRAINING);
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
				logger.debug("W row sparcity: " + SandiaMatrixUtils.rowSparcity(w));
				logger.debug("U row sparcity: " + SandiaMatrixUtils.rowSparcity(u));
				Boolean biasMode = learner.getParams().getTyped(BilinearLearnerParameters.BIAS);
				if(biasMode){
					logger.debug("Bias: " + SandiaMatrixUtils.diag(bias));
				}
				logger.debug(String.format("... loss: %f",loss));
			}
		}		
	}
	
	private static String BILL_DATA() {
		
		return String.format(BILL_DATA,BILL_DATA_ROOT());
	}
	
	private static String BILL_DATA_ROOT() {
		
		return String.format(BILL_DATA_ROOT,DROPBOX_HOME());
	}

	private static String DROPBOX_HOME() {
		String home = System.getProperty("user.home");

		return String.format("%s/Dropbox",home);
	}
}
