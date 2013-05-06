package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.demos.twitter.TwitterStreamingDataset;
import org.openimaj.tools.twitter.modes.filter.LanguageFilter;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingModeOption;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

import twitter4j.Status;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FinancialStreamLearningExperiment {
	public static void main(String[] args) throws MalformedURLException, IOException {

		// The financial stream
		RealTimeWindowFunction<List<Double>> yahooWindow = new RealTimeWindowFunction<List<Double>>(5000);
		Stream<List<List<Double>>> yahooStream = new YahooFinanceStream("AAPL","GOOG").transform(yahooWindow);
		
		// The Twitter Stream
		final ArrayBlockingDroppingQueue<Status> buffer = new ArrayBlockingDroppingQueue<Status>(1);
		final LanguageDetectionMode languageDetectionMode = new LanguageDetectionMode();
		Stream<List<USMFStatus>> twitterStream = new TwitterStreamingDataset(
			DefaultTokenFactory.get(TwitterAPIToken.class),buffer
		)
		.transform(new RealTimeWindowFunction<Status>(5000))
		.map(new Function<List<Status>, List<USMFStatus>>() {
			@Override
			public List<USMFStatus> apply(List<Status> in) {
				ArrayList<USMFStatus> ret = new ArrayList<USMFStatus>();
				for (Status sstatus : in) {
					USMFStatus status = new USMFStatus();
					new GeneralJSONTweet4jStatus(sstatus).fillUSMF(status);
					ret.add(status);
				}
				
				return ret;
			}
			
		})
		.map(new TwitterPreprocessingFunction(languageDetectionMode,new TokeniseMode(),new StopwordMode()))
		.map(new TwitterPredicateFunction(new LanguageFilter("en")))
		;
		
		// The combined stream
		StreamCombiner.combine(yahooStream, twitterStream).forEach(new Operation<IndependentPair<List<List<Double>>,List<USMFStatus>>>() {

			@Override
			public void perform(IndependentPair<List<List<Double>>, List<USMFStatus>> pair) {
				List<List<Double>> yahoo = pair.firstObject();
				List<USMFStatus> twitter = pair.secondObject();
				System.out.println(String.format("I've seen: %d yahoo ticks and %d tweets",yahoo.size(),twitter.size()));
				System.out.format("Buffer dropped: %d seen %d\n", buffer.dropCount(),buffer.insertCount());
			}
		});


	}
}