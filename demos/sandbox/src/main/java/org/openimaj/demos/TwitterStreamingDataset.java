package org.openimaj.demos;

import org.openimaj.data.dataset.StreamingDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.ForgetfulBuffer;
import org.openimaj.util.stream.LiveStream;
import org.openimaj.util.stream.StreamBuffer;

import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterStreamingDataset extends LiveStream<Status> implements StreamingDataset<Status> {
	static class TwitterConnection extends LiveStreamConnection<Status> {
		public TwitterConnection(TwitterAPIToken token) {
			final TwitterStream twitterStream = new TwitterStreamFactory(makeConfiguration(token)).getInstance();

			twitterStream.addListener(new StatusAdapter() {
				@Override
				public void onStatus(Status status) {
					register(status);
				}
			});

			twitterStream.sample();
		}

		private Configuration makeConfiguration(TwitterAPIToken token) {
			final ConfigurationBuilder cb = new ConfigurationBuilder()
					.setOAuthConsumerKey(token.consumerKey)
					.setOAuthConsumerSecret(token.consumerSecret)
					.setOAuthAccessToken(token.accessToken)
					.setOAuthAccessTokenSecret(token.accessSecret);

			return cb.build();
		}
	}

	public TwitterStreamingDataset(TwitterAPIToken token, StreamBuffer<Status> buffer) {
		super(new TwitterConnection(token), buffer);
	}

	@Override
	public Status getRandomInstance() {
		return this.next();
	}

	@Override
	public int numInstances() {
		return Integer.MAX_VALUE;
	}

	public static void main(String[] args) {
		final TwitterAPIToken token = DefaultTokenFactory.getInstance().getToken(TwitterAPIToken.class);

		final ForgetfulBuffer<Status> buffer = new ForgetfulBuffer<Status>();
		final TwitterStreamingDataset dataset = new TwitterStreamingDataset(token, buffer);

		dataset.forEach(new Operation<Status>() {
			@Override
			public void perform(Status object) {
				System.out.println(buffer.getStatistics());

				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
