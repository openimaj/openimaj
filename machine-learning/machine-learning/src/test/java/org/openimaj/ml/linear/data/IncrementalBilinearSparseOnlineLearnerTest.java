package org.openimaj.ml.linear.data;

import gov.sandia.cognition.math.matrix.Matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.evaluation.MeanSumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

public class IncrementalBilinearSparseOnlineLearnerTest {
	Logger logger = Logger.getLogger(IncrementalBilinearSparseOnlineLearnerTest.class);
	/**
	 * the output folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void before() throws IOException {
		final ConsoleAppender console = new ConsoleAppender(); // create
																// appender
		// configure the appender
		final String PATTERN = "[%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		// add appender to any Logger (here is root)
		final Logger rootLogger = Logger.getRootLogger();
		rootLogger.addAppender(console);
	}

	@Test
	public void testBilinear() throws IOException {
		final int nfeatures = 10;
		final int nusers = 5;
		final int ntasks = 3;
		final BiconvexIncrementalDataGenerator gen = new BiconvexIncrementalDataGenerator(
				nusers, nfeatures, ntasks, 0.3, 0.3, // users, words, tasks,
														// sparcity, xsparcity
				true, true, -1, 0); // indw, indu, seed, noise
		final IncrementalBilinearSparseOnlineLearner learner = new IncrementalBilinearSparseOnlineLearner();
		learner.getParams().put(BilinearLearnerParameters.BICONVEX_MAXITER, 5);
		learner.reinitParams();
		final int dataitems = 300;
		final int halfDataItems = dataitems / 2;
		final List<Pair<Matrix>> pairs = new ArrayList<Pair<Matrix>>();
		double first100 = 0;
		double second100 = 0;
		for (int i = 0; i < dataitems; i++) {
			final IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>> xy = gen.generate();
			if (xy == null)
				continue;
			learner.process(xy.firstObject(), xy.secondObject());
			pairs.add(learner.asMatrixPair(xy, nfeatures, nusers, ntasks));
			final BilinearEvaluator eval = new MeanSumLossEvaluator();
			eval.setLearner(learner.getBilinearLearner(nusers, nfeatures));
			final double loss = eval.evaluate(pairs);

			if (i / halfDataItems == 0)
				first100 += loss;
			else if (i / halfDataItems == 1)
				second100 += loss;
			logger.debug(String.format("Pair %d, Loss = %f", i, loss));
		}
		logger.info("First half:" + first100 / halfDataItems);
		logger.info("Second half:" + second100 / halfDataItems);
		// assertTrue(first100 > second100);
	}
}
