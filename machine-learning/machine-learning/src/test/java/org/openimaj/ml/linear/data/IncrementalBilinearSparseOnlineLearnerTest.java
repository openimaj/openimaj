package org.openimaj.ml.linear.data;

import static org.junit.Assert.assertTrue;
import gov.sandia.cognition.math.matrix.Matrix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.openimaj.io.FileUtils;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.evaluation.MeanSumLossEvaluator;
import org.openimaj.ml.linear.evaluation.SumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
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
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "[%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.INFO);
		console.activateOptions();
	  	// add appender to any Logger (here is root)
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.addAppender(console);
	}

	@Test
	public void testBilinear() throws IOException {
		int nfeatures = 10;
		int nusers = 5;
		int ntasks = 3;
		BiconvexIncrementalDataGenerator gen = new BiconvexIncrementalDataGenerator (
				nusers,nfeatures,ntasks, 0.3,0.3, // users, words, tasks, sparcity, xsparcity
				true, true, -1, 0); // indw, indu, seed, noise
		IncrementalBilinearSparseOnlineLearner learner = new IncrementalBilinearSparseOnlineLearner();
		learner.getParams().put(BilinearLearnerParameters.BICONVEX_MAXITER, 5);
		learner.reinitParams();
		int dataitems = 300;
		int halfDataItems = dataitems/2;
		List<Pair<Matrix>> pairs = new ArrayList<Pair<Matrix>>();
		double first100 = 0;
		double second100 = 0;
		for (int i = 0; i < dataitems; i++) {
			IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>> xy = gen.generate();
			if(xy == null) continue;
			learner.process(xy.firstObject(), xy.secondObject());
			pairs.add(learner.asMatrixPair(xy, nfeatures, nusers, ntasks));
			BilinearEvaluator eval = new MeanSumLossEvaluator();
			eval.setLearner(learner.getBilinearLearner(nusers,nfeatures));
			double loss = eval.evaluate(pairs);
			
			if(i / halfDataItems == 0) first100 += loss;
			else if(i / halfDataItems == 1) second100 += loss;
			logger.debug(String.format("Pair %d, Loss = %f", i, loss));
		}
		logger.info("First half:" + first100/halfDataItems);
		logger.info("Second half:" + second100/halfDataItems);
//		assertTrue(first100 > second100);
	}
}
