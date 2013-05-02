package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.openimaj.demos.twitter.TwitterStreamingDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.Stream;

import twitter4j.Status;

public class FinancialStreamLearningExperiment {
	public static void main(String[] args) throws MalformedURLException, IOException {
		RealTimeWindowFunction<List<Double>> yahooWindow = new RealTimeWindowFunction<List<Double>>(5000);
		yahooWindow.name = "YAHOO";
		Stream<List<List<Double>>> yahooStream = new YahooFinanceStream("AAPL","GOOG").
				transform(
						yahooWindow
		);
		final ArrayBlockingDroppingQueue<Status> buffer = new ArrayBlockingDroppingQueue<Status>(1);
		RealTimeWindowFunction<Status> twitterWindow = new RealTimeWindowFunction<Status>(5000);
		twitterWindow.name = "TWITTER";
		Stream<Status> twitterStream = new TwitterStreamingDataset(
			DefaultTokenFactory.get(TwitterAPIToken.class),buffer
		)
//		.transform(
//			twitterWindow
//		)
		;
		twitterStream.forEach(new Operation<Status>() {

			@Override
			public void perform(Status object) {
//				System.out.println(String.format("I've seen: %d tweets",object.size()));
				System.out.format("Buffer dropped: %d seen %d\n", buffer.dropCount(),buffer.insertCount());
			}
		});
//		StreamCombiner.combine(yahooStream, twitterStream).forEach(new Operation<IndependentPair<List<List<Double>>,List<Status>>>() {
//
//			@Override
//			public void perform(IndependentPair<List<List<Double>>, List<Status>> pair) {
//				List<List<Double>> yahoo = pair.firstObject();
//				List<Status> twitter = pair.secondObject();
//				System.out.println(String.format("I've seen: %d yahoo ticks and %d tweets",yahoo.size(),twitter.size()));
//				System.out.format("Buffer dropped: %d seen %d\n", buffer.dropCount(),buffer.insertCount());
//			}
//		});


	}
}