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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BilinearSparseOnlineLearnerTest {
	Logger logger = Logger.getLogger(BilinearSparseOnlineLearnerTest.class);
	/**
	 * the output folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// private File matfile;
	// private File winitfile;
	// private File uinitfile;

	@BeforeClass
	public static void before() throws IOException {
		final ConsoleAppender console = new ConsoleAppender(); // create
		// appender
		// configure the appender
		final String PATTERN = "[%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
	}

	@Test
	public void testBilinear() throws IOException {
		// final BiconvexDataGenerator gen = new BiconvexDataGenerator(
		// 5, 10, 3, 0.3, 0, // users, words, tasks, sparcity
		// true, true, 2, 0); // indw, indu, seed, noise
		// // MatlabFileDataGenerator gen = new
		// MatlabFileDataGenerator(matfile);
		// final BilinearSparseOnlineLearner learner = new
		// BilinearSparseOnlineLearner();
		// // learner.getParams().put("winitstrat", new
		// // MatlabFileInitStrat(winitfile));
		// // learner.getParams().put("uinitstrat", new
		// // MatlabFileInitStrat(uinitfile));
		// final int dataitems = 400;
		// final int halfdataitems = dataitems / 2;
		// final List<Pair<Matrix>> pairs = new ArrayList<Pair<Matrix>>();
		// double first = 0;
		// double second = 0;
		// for (int i = 0; i < dataitems; i++) {
		// final Pair<Matrix> xy = gen.generate();
		// if (xy == null)
		// continue;
		// pairs.add(xy);
		// learner.process(xy.firstObject(), xy.secondObject());
		// final BilinearEvaluator eval = new SumLossEvaluator();
		// eval.setLearner(learner);
		// final double loss = eval.evaluate(pairs);
		// if (i / halfdataitems == 0)
		// first += loss / (i + 1);
		// else if (i / halfdataitems == 1)
		// second += loss / (i + 1);
		// logger.debug(String.format("Pair %d, Loss = %f", i, loss));
		// }
		// logger.info("First half:" + first / dataitems);
		// logger.info("Second half:" + second / dataitems);
		// logger.info("W sparcity:" +
		// CFMatrixUtils.rowSparsity(learner.getW()));
		// logger.info("U sparcity:" +
		// CFMatrixUtils.rowSparsity(learner.getU()));
		// assertTrue(first > second);
	}

	// @Test
	// public void testBilinearUnmixed() throws IOException {
	// BiconvexDataGenerator gen = new BiconvexDataGenerator(
	// 5, 10, 3, 0.3,0, // users, words, tasks, sparcity
	// true, true, 2, 0); // indw, indu, seed, noise
	// // MatlabFileDataGenerator gen = new MatlabFileDataGenerator(matfile);
	// BilinearUnmixedSparseOnlineLearner learner = new
	// BilinearUnmixedSparseOnlineLearner();
	// BilinearLearnerParameters params = learner.getParams();
	// params.put(BilinearLearnerParameters.LAMBDA, 0.001);
	// learner.reinitParams();
	// // learner.getParams().put("winitstrat", new
	// MatlabFileInitStrat(winitfile));
	// // learner.getParams().put("uinitstrat", new
	// MatlabFileInitStrat(uinitfile));
	// int dataitems = 400;
	// int halfdataitems = dataitems/2;
	// List<Pair<Matrix>> pairs = new ArrayList<Pair<Matrix>>();
	// double first = 0;
	// double second = 0;
	// for (int i = 0; i < dataitems; i++) {
	// Pair<Matrix> xy = gen.generate();
	// if(xy == null) continue;
	// pairs.add(xy);
	// learner.process(xy.firstObject(), xy.secondObject());
	// BilinearEvaluator eval = new SumLossEvaluator();
	// eval.setLearner(learner);
	// double loss = eval.evaluate(pairs);
	// if(i / halfdataitems == 0) first += loss/(i+1);
	// else if(i / halfdataitems == 1) second += loss/(i+1);
	// logger.debug(String.format("Pair %d, Loss = %f", i, loss));
	// }
	// logger.info("First half:" + first/halfdataitems);
	// logger.info("Second half:" + second/halfdataitems);
	// logger.info("W sparcity:" +
	// SandiaMatrixUtils.rowSparcity(learner.getW()));
	// logger.info("U sparcity:" +
	// SandiaMatrixUtils.rowSparcity(learner.getU()));
	// assertTrue(first > second);
	// }
}
