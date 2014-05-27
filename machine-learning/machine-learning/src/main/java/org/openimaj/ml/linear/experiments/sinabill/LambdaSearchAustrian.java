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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator.Fold;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator.Mode;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.evaluation.RootMeanSumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.ml.linear.learner.loss.MatSquareLossFunction;
import org.openimaj.util.pair.Pair;

import com.google.common.primitives.Doubles;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;

/**
 * Optimise lambda and eta0 and learning rates with a line search
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class LambdaSearchAustrian {

	private static final int NFOLDS = 1;
	private static final String ROOT = "/Users/ss/Experiments/bilinear/austrian/";
	private static final String OUTPUT_ROOT = "/Users/ss/Dropbox/TrendMiner/Collaboration/StreamingBilinear2014/experiments";
	private final Logger logger = Logger.getLogger(getClass());

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final LambdaSearchAustrian exp = new LambdaSearchAustrian();
		exp.performExperiment();
	}

	private long expStartTime = System.currentTimeMillis();

	/**
	 * @throws IOException
	 */
	public void performExperiment() throws IOException {
		final List<BillMatlabFileDataGenerator.Fold> folds = prepareFolds();
		final BillMatlabFileDataGenerator bmfdg = new BillMatlabFileDataGenerator(
				new File(dataFromRoot("normalised.mat")), "user_vsr_for_polls_SINA",
				new File(dataFromRoot("unnormalised.mat")),
				98, false,
				folds
				);
		prepareExperimentLog();
		final BilinearEvaluator eval = new RootMeanSumLossEvaluator();
		for (int i = 0; i < bmfdg.nFolds(); i++) {
			logger.info("Starting Fold: " + i);
			final BilinearSparseOnlineLearner best = lineSearchParams(i, bmfdg);
			logger.debug("Best params found! Starting test...");
			bmfdg.setFold(i, Mode.TEST);
			eval.setLearner(best);
			final double ev = eval.evaluate(bmfdg.generateAll());
			logger.debug("Test RMSE: " + ev);

		}
	}

	private BilinearSparseOnlineLearner lineSearchParams(int fold, BillMatlabFileDataGenerator source) {
		BilinearSparseOnlineLearner best = null;
		double bestScore = Double.MAX_VALUE;
		final BilinearEvaluator eval = new RootMeanSumLossEvaluator();
		int j = 0;
		final List<BilinearLearnerParameters> parameterLineSearch = parameterLineSearch();
		logger.info("Optimising params, searching: " + parameterLineSearch.size());
		for (final BilinearLearnerParameters next : parameterLineSearch) {
			logger.info(String.format("Optimising params %d/%d", j + 1, parameterLineSearch.size()));
			logger.debug("Current Params:\n" + next.toString());
			final BilinearSparseOnlineLearner learner = new BilinearSparseOnlineLearner(next);
			// Train the model with the new parameters
			source.setFold(fold, Mode.TRAINING);
			Pair<Matrix> pair = null;
			logger.debug("Training...");
			while ((pair = source.generate()) != null) {
				learner.process(pair.firstObject(), pair.secondObject());
			}
			logger.debug("Generating score of validation set");
			// validate with the validation set
			source.setFold(fold, Mode.VALIDATION);
			eval.setLearner(learner);
			final double loss = eval.evaluate(source.generateAll());
			logger.debug("Total RMSE: " + loss);
			logger.debug("U sparcity: " + CFMatrixUtils.sparsity(learner.getU()));
			logger.debug("W sparcity: " + CFMatrixUtils.sparsity(learner.getW()));
			// record the best
			if (loss < bestScore) {
				logger.info("New best score detected!");
				bestScore = loss;
				best = learner;
				logger.info("New Best Config:\n" + best.getParams());
				logger.info("New Best Loss:" + loss);
				saveFoldParameterLearner(fold, j, learner);
			}
			j++;
		}
		return best;
	}

	private void saveFoldParameterLearner(int fold, int j, BilinearSparseOnlineLearner learner) {
		// save the state
		final File learnerOut = new File(String.format("%s/fold_%d", currentOutputRoot(), fold), String.format(
				"learner_%d", j));
		final File learnerOutMat = new File(String.format("%s/fold_%d", currentOutputRoot(), fold), String.format(
				"learner_%d.mat", j));
		learnerOut.getParentFile().mkdirs();
		try {
			IOUtils.writeBinary(learnerOut, learner);
			final Collection<MLArray> data = new ArrayList<MLArray>();
			data.add(CFMatrixUtils.toMLArray("u", learner.getU()));
			data.add(CFMatrixUtils.toMLArray("w", learner.getW()));
			if (learner.getBias() != null) {
				data.add(CFMatrixUtils.toMLArray("b", learner.getBias()));
			}
			final MatFileWriter writer = new MatFileWriter(learnerOutMat, data);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<BilinearLearnerParameters> parameterLineSearch() {
		final BilinearLearnerParameters params = prepareParams();
		final BilinearLearnerParametersLineSearch iter = new BilinearLearnerParametersLineSearch(params);

		iter.addIteration(BilinearLearnerParameters.ETA0_U, Doubles.asList(new double[] { 0.0001 }));
		iter.addIteration(BilinearLearnerParameters.ETA0_W, Doubles.asList(new double[] { 0.005 }));
		iter.addIteration(BilinearLearnerParameters.ETA0_BIAS, Doubles.asList(new double[] { 50 }));
		iter.addIteration(BilinearLearnerParameters.LAMBDA_U, Doubles.asList(new double[] { 0.00001 }));
		iter.addIteration(BilinearLearnerParameters.LAMBDA_W, Doubles.asList(new double[] { 0.00001 }));

		final List<BilinearLearnerParameters> ret = new ArrayList<BilinearLearnerParameters>();
		for (final BilinearLearnerParameters param : iter) {
			ret.add(param);
		}
		return ret;
	}

	private List<Fold> prepareFolds() {
		final List<Fold> set_fold = new ArrayList<BillMatlabFileDataGenerator.Fold>();

		// [24/02/2014 16:58:23] .@bill:
		final int step = 5; // % test_size
		final int t_size = 48; // % training_size
		final int v_size = 8;
		for (int i = 0; i < NFOLDS; i++) {
			final int total = i * step + t_size;
			final int[] training = new int[total - v_size];
			final int[] test = new int[step];
			final int[] validation = new int[v_size];
			int j = 0;
			int traini = 0;
			final int tt = (int) Math.round(total / 2.) - 1;
			for (; j < tt - v_size / 2; j++, traini++) {
				training[traini] = j;
			}
			for (int k = 0; k < validation.length; k++, j++) {
				validation[k] = j;
			}
			for (; j < total; j++, traini++) {
				training[traini] = j;
			}
			for (int k = 0; k < test.length; k++, j++) {
				test[k] = j;
			}
			final Fold foldi = new Fold(training, test, validation);
			set_fold.add(foldi);
		}
		// [24/02/2014 16:59:07] .@bill: set_fold{1,1}
		return set_fold;
	}

	private BilinearLearnerParameters prepareParams() {
		final BilinearLearnerParameters params = new BilinearLearnerParameters();

		params.put(BilinearLearnerParameters.ETA0_U, null);
		params.put(BilinearLearnerParameters.ETA0_W, null);
		params.put(BilinearLearnerParameters.LAMBDA_U, null);
		params.put(BilinearLearnerParameters.LAMBDA_W, null);
		params.put(BilinearLearnerParameters.ETA0_BIAS, null);

		params.put(BilinearLearnerParameters.BICONVEX_TOL, 0.01);
		params.put(BilinearLearnerParameters.BICONVEX_MAXITER, 10);
		params.put(BilinearLearnerParameters.BIAS, true);
		params.put(BilinearLearnerParameters.WINITSTRAT, new SparseZerosInitStrategy());
		params.put(BilinearLearnerParameters.UINITSTRAT, new SparseZerosInitStrategy());
		params.put(BilinearLearnerParameters.LOSS, new MatSquareLossFunction());
		return params;
	}

	/**
	 * @param data
	 * @return the data file from the root
	 */
	public static String dataFromRoot(String data) {
		return String.format("%s/%s", ROOT, data);
	}

	protected void prepareExperimentLog() throws IOException {
		final ConsoleAppender console = new ConsoleAppender(); // create
																// appender
		// configure the appender
		final String PATTERN = "[%p->%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
		final File expRoot = prepareExperimentRoot();

		final File logFile = new File(expRoot, "log");
		if (logFile.exists())
			logFile.delete();
		final String TIMED_PATTERN = "[%d{HH:mm:ss} %p->%C{1}] %m%n";
		final FileAppender file = new FileAppender(new PatternLayout(TIMED_PATTERN), logFile.getAbsolutePath());
		file.setThreshold(Level.DEBUG);
		file.activateOptions();
		Logger.getRootLogger().addAppender(file);
		logger.info("Experiment root: " + expRoot);

	}

	/**
	 * @return
	 * @throws IOException
	 */
	public File prepareExperimentRoot() throws IOException {
		final String experimentRoot = currentOutputRoot();
		final File expRoot = new File(experimentRoot);
		if (expRoot.exists() && expRoot.isDirectory())
			return expRoot;
		logger.debug("Experiment root: " + expRoot);
		if (!expRoot.mkdirs())
			throw new IOException("Couldn't prepare experiment output");
		return expRoot;
	}

	private String currentOutputRoot() {
		return String.format("%s/%s/%s", OUTPUT_ROOT, getExperimentSetName(), "" + currentExperimentTime());
	}

	private long currentExperimentTime() {
		return expStartTime;
	}

	private String getExperimentSetName() {
		return "streamingBilinear/optimiselambda";
	}
}
