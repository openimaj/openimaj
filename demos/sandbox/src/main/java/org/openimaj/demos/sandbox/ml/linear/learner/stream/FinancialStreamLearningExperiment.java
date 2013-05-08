package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.openimaj.demos.twitter.TwitterStreamingDataset;
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


		// The combined stream
		StreamCombiner.combine(yahooAveragePriceStream, twitterUserWordCountStream)
//		.map(new IncrementalLearnerWorldSelectingEvaluator())
		;
//		{
//
//			@Override
//			public void perform(IndependentPair<List<List<Double>>, List<USMFStatus>> pair) {
//				List<List<Double>> yahoo = pair.firstObject();
//				List<USMFStatus> twitter = pair.secondObject();
//				System.out.println(String.format("I've seen: %d yahoo ticks and %d tweets",yahoo.size(),twitter.size()));
//				System.out.format("Buffer dropped: %d seen %d\n", buffer.dropCount(),buffer.insertCount());
//				for (USMFStatus tweet : twitter) {
//					try {
//						List<String> nostopwords = TwitterPreprocessingMode.results(tweet, stopwordMode);
//					} catch (Exception e) {
//					}
//				}
//			}
//		});


	}
}