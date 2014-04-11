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
package org.openimaj.ml.linear.data;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
		// final int nfeatures = 10;
		// final int nusers = 5;
		// final int ntasks = 3;
		// final BiconvexIncrementalDataGenerator gen = new
		// BiconvexIncrementalDataGenerator(
		// nusers, nfeatures, ntasks, 0.3, 0.3, // users, words, tasks,
		// // sparcity, xsparcity
		// true, true, -1, 0); // indw, indu, seed, noise
		// final IncrementalBilinearSparseOnlineLearner learner = new
		// IncrementalBilinearSparseOnlineLearner();
		// learner.getParams().put(BilinearLearnerParameters.BICONVEX_MAXITER,
		// 5);
		// learner.reinitParams();
		// final int dataitems = 300;
		// final int halfDataItems = dataitems / 2;
		// final List<Pair<Matrix>> pairs = new ArrayList<Pair<Matrix>>();
		// double first100 = 0;
		// double second100 = 0;
		// for (int i = 0; i < dataitems; i++) {
		// final IndependentPair<Map<String, Map<String, Double>>, Map<String,
		// Double>> xy = gen.generate();
		// if (xy == null)
		// continue;
		// learner.process(xy.firstObject(), xy.secondObject());
		// pairs.add(learner.asMatrixPair(xy, nfeatures, nusers, ntasks));
		// final BilinearEvaluator eval = new MeanSumLossEvaluator();
		// eval.setLearner(learner.getBilinearLearner(nusers, nfeatures));
		// final double loss = eval.evaluate(pairs);
		//
		// if (i / halfDataItems == 0)
		// first100 += loss;
		// else if (i / halfDataItems == 1)
		// second100 += loss;
		// logger.debug(String.format("Pair %d, Loss = %f", i, loss));
		// }
		// logger.info("First half:" + first100 / halfDataItems);
		// logger.info("Second half:" + second100 / halfDataItems);
		// // assertTrue(first100 > second100);
	}
}
