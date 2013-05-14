package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.openimaj.demos.twitter.TwitterStreamingDataset;
import org.openimaj.ml.linear.evaluation.SumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.tools.twitter.modes.filter.LanguageFilter;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.function.ListFilter;
import org.openimaj.util.function.ListFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.Stream;

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
		Stream<Map<String, Double>> yahooAveragePriceStream = new YahooFinanceStream("AAPL","GOOG")
		.transform(yahooWindow)
		.map(new WindowAverage());

		// The Twitter Stream
		final ArrayBlockingDroppingQueue<Status> buffer = new ArrayBlockingDroppingQueue<Status>(1);
		final LanguageDetectionMode languageDetectionMode = new LanguageDetectionMode();
		final StopwordMode stopwordMode = new StopwordMode();
		final TokeniseMode tokeniseMode = new TokeniseMode();

		Stream<Map<String, Map<String, Double>>> twitterUserWordCountStream = new TwitterStreamingDataset(
			DefaultTokenFactory.get(TwitterAPIToken.class),buffer
		)
		.transform(new RealTimeWindowFunction<Status>(5000))
		.map(new ListFunction<Status,USMFStatus>(new TwitterStatusAsUSMFStatus()))
		.map(new ListFunction<USMFStatus,USMFStatus>(new TwitterPreprocessingFunction(languageDetectionMode,tokeniseMode,stopwordMode)))
		.map(new ListFilter<USMFStatus>(new TwitterPredicateFunction(new LanguageFilter("en"))))
		.map(new USMFStatusUserWordScore(stopwordMode))
		;


		BilinearLearnerParameters params = new BilinearLearnerParameters();
		// The combined stream
		StreamCombiner.combine(twitterUserWordCountStream,yahooAveragePriceStream)
		.map(new IncrementalLearnerWorldSelectingEvaluator(new SumLossEvaluator(), new IncrementalLearnerFunction(params)))
		.forEach(new Operation<IndependentPair<List<String>,Double>>() {
			
			@Override
			public void perform(IndependentPair<List<String>, Double> object) {
				System.out.println("Loss: " + object.secondObject());
				System.out.println("Important words: " + object.firstObject());
			}
		});


	}
}