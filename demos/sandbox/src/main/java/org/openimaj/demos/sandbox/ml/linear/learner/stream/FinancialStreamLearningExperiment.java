package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.openimaj.demos.twitter.TwitterStreamingDataset;
import org.openimaj.ml.linear.evaluation.SumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.init.HardCodedInitStrat;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.tools.twitter.modes.filter.LanguageFilter;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.Pair;
import org.openimaj.util.stream.Stream;
import org.openimaj.util.stream.window.Aggregation;
import org.openimaj.util.stream.window.AggregationStreamCombiner;
import org.openimaj.util.stream.window.RealTimeWindowFunction;
import org.openimaj.util.stream.window.Window;
import org.openimaj.util.stream.window.WindowAverage;
import org.openimaj.util.stream.window.WindowFilter;
import org.openimaj.util.stream.window.WindowFunction;

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
		RealTimeWindowFunction<Map<String,Double>> yahooWindow = new RealTimeWindowFunction<Map<String,Double>>(5000);
		Stream<Aggregation<Map<String, Double>,Long>> yahooAveragePriceStream = new YahooFinanceStream("AAPL","GOOG")
		.transform(yahooWindow)
		.map(new WindowAverage<Window<Map<String,Double>,Long>,Long>());

		// The Twitter Stream
		final ArrayBlockingDroppingQueue<Status> buffer = new ArrayBlockingDroppingQueue<Status>(1);
		final LanguageDetectionMode languageDetectionMode = new LanguageDetectionMode();
		final StopwordMode stopwordMode = new StopwordMode();
		final TokeniseMode tokeniseMode = new TokeniseMode();

		Stream<Aggregation<Map<String, Map<String, Double>>, Long>> twitterUserWordCountStream = new TwitterStreamingDataset(
			DefaultTokenFactory.get(TwitterAPIToken.class),buffer
		)
		.transform(new RealTimeWindowFunction<Status>(5000))
		.map(new WindowFunction<Status,USMFStatus,Long>(new TwitterStatusAsUSMFStatus()))
		.map(new WindowFunction<USMFStatus,USMFStatus,Long>(new TwitterPreprocessingFunction(languageDetectionMode,tokeniseMode,stopwordMode)))
		.map(new WindowFilter<USMFStatus,Long>(new TwitterPredicateFunction(new LanguageFilter("en"))))
		.map(new USMFStatusUserWordScore<Window<USMFStatus,Long>>(stopwordMode))
		;


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
		HardCodedInitStrat biasInitStrat = new HardCodedInitStrat();
		params.put(BilinearLearnerParameters.BIASINITSTRAT, biasInitStrat);
		// The combined stream
		AggregationStreamCombiner.combine(twitterUserWordCountStream,yahooAveragePriceStream)
		.map(new IncrementalLearnerWorldSelectingEvaluator(new SumLossEvaluator(), new IncrementalLearnerFunction(params)))
		.forEach(new Operation<ModelStats>() {

			@Override
			public void perform(ModelStats object) {
				System.out.println("Loss: " + object.score);
				System.out.println("Important words: " );
				for (String task: object.importantWords.keySet()) {
					Pair<Double> minmax = object.taskMinMax.get(task);
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