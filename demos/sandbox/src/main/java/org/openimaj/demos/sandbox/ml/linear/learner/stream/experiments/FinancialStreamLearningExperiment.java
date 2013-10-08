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
package org.openimaj.demos.sandbox.ml.linear.learner.stream.experiments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.openimaj.demos.sandbox.ml.linear.learner.stream.IncrementalLearnerFunction;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.IncrementalLearnerWorldSelectingEvaluator;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.ModelStats;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.YahooFinanceStream;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.TwitterPredicateFunction;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.TwitterPreprocessingFunction;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.TwitterStatusAsUSMFStatus;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.USMFStatusBagOfWords;
import org.openimaj.ml.linear.evaluation.SumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.init.HardCodedInitStrat;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.stream.provider.twitter.TwitterStreamDataset;
import org.openimaj.tools.twitter.modes.filter.LanguageFilter;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.context.ContextFunctionAdaptor;
import org.openimaj.util.function.context.ContextListFilter;
import org.openimaj.util.function.context.ContextListFunction;
import org.openimaj.util.pair.Pair;
import org.openimaj.util.stream.Stream;
import org.openimaj.util.stream.combine.ContextStreamCombiner;
import org.openimaj.util.stream.window.ContextRealTimeWindowFunction;
import org.openimaj.util.stream.window.WindowAverage;

import twitter4j.Status;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class FinancialStreamLearningExperiment {
	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {

		// The financial stream
		final ContextRealTimeWindowFunction<Map<String, Double>> yahooWindow = new ContextRealTimeWindowFunction<Map<String, Double>>(
				5000);
		final Stream<Context> yahooAveragePriceStream = new YahooFinanceStream("AAPL", "GOOG")
				.transform(yahooWindow)
				.map(
						new ContextFunctionAdaptor<List<Map<String, Double>>, Map<String, Double>>(
								new WindowAverage(), "item",
								"averageticks"
						)
				);

		// The Twitter Stream
		final ArrayBlockingDroppingQueue<Status> buffer = new ArrayBlockingDroppingQueue<Status>(1);
		final LanguageDetectionMode languageDetectionMode = new LanguageDetectionMode();
		final StopwordMode stopwordMode = new StopwordMode();
		final TokeniseMode tokeniseMode = new TokeniseMode();

		final Stream<Context> twitterUserWordCountStream = new TwitterStreamDataset(
				DefaultTokenFactory.get(TwitterAPIToken.class), buffer
				)
						.transform(new ContextRealTimeWindowFunction<Status>(5000))
						.map(
								new ContextListFunction<Status, USMFStatus>(new TwitterStatusAsUSMFStatus(), "item",
										"usmfstatuses"
								)
						)
						.map(
								new ContextListFunction<USMFStatus, USMFStatus>(new TwitterPreprocessingFunction(languageDetectionMode, tokeniseMode,
										stopwordMode),
										"usmfstatuses"
								)
						)
						.map(new ContextListFilter<USMFStatus>(new TwitterPredicateFunction(new LanguageFilter("en")),
								"usmfstatuses"
								)
						)
						.map(
								new ContextFunctionAdaptor<List<USMFStatus>, Map<String, Map<String, Double>>>(new USMFStatusBagOfWords(new StopwordMode()),
										"usmfstatuses",
										"bagofwords"
								)
						);

		final BilinearLearnerParameters params = new BilinearLearnerParameters();
		params.put(BilinearLearnerParameters.ETA0_U, 0.02);
		params.put(BilinearLearnerParameters.ETA0_W, 0.02);
		params.put(BilinearLearnerParameters.LAMBDA, 0.001);
		params.put(BilinearLearnerParameters.BICONVEX_TOL, 0.01);
		params.put(BilinearLearnerParameters.BICONVEX_MAXITER, 10);
		params.put(BilinearLearnerParameters.BIAS, true);
		params.put(BilinearLearnerParameters.ETA0_BIAS, 0.5);
		params.put(BilinearLearnerParameters.WINITSTRAT, new SingleValueInitStrat(0.1));
		params.put(BilinearLearnerParameters.UINITSTRAT, new SparseZerosInitStrategy());
		final HardCodedInitStrat biasInitStrat = new HardCodedInitStrat();
		params.put(BilinearLearnerParameters.BIASINITSTRAT, biasInitStrat);
		// The combined stream
		ContextStreamCombiner
				.combine(twitterUserWordCountStream, yahooAveragePriceStream)
				.map(
						new IncrementalLearnerWorldSelectingEvaluator(new SumLossEvaluator(),
								new IncrementalLearnerFunction(params)))
				.forEach(new Operation<Context>() {

					@Override
					public void perform(Context c) {
						final ModelStats object = c.getTyped("modelstats");
						System.out.println("Loss: " + object.score);
						System.out.println("Important words: ");
						for (final String task : object.importantWords.keySet()) {
							final Pair<Double> minmax = object.taskWordMinMax.get(task);
							System.out.printf("... %s (%1.4f->%1.4f) %s\n",
									task,
									minmax.firstObject(),
									minmax.secondObject(),
									object.importantWords.get(task)
									);
						}
					}
				});

	}
}
