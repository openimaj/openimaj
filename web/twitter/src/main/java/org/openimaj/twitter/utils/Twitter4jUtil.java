package org.openimaj.twitter.utils;

import org.apache.log4j.Logger;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Create a Twitter4j instance
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Twitter4jUtil {

	private static Logger logger = Logger.getLogger(Twitter4jUtil.class);

	/**
	 * @return create a {@link Twitter} instance using the tokens from
	 *         {@link DefaultTokenFactory}
	 */
	public static Twitter create() {
		Twitter INSTANCE = null;
		if (INSTANCE == null) {
			final Configuration config = makeConfiguration(DefaultTokenFactory.get(TwitterAPIToken.class));
			INSTANCE = new TwitterFactory(config).getInstance();
		}
		return INSTANCE;
	}

	private static Configuration makeConfiguration(TwitterAPIToken token) {
		final ConfigurationBuilder cb = new ConfigurationBuilder()
				.setOAuthConsumerKey(token.consumerKey)
				.setOAuthConsumerSecret(token.consumerSecret)
				.setOAuthAccessToken(token.accessToken)
				.setOAuthAccessTokenSecret(token.accessSecret);
		cb.setJSONStoreEnabled(true);

		return cb.build();
	}

	/**
	 * Handle a twitter exception, waiting the appropriate amount of time if the
	 * API requests this behaviour
	 * 
	 * @param e
	 * @param errorWaitTime
	 * @return the errorWaitTime
	 */
	public static long handleTwitterException(TwitterException e, long errorWaitTime) {
		if (e.exceededRateLimitation()) {
			long retryAfter = e.getRetryAfter() * 1000;
			logger.debug(String.format("Rate limit exceeded, waiting %dms", retryAfter));
			if (retryAfter < 0) {
				retryAfter = errorWaitTime * 5;
			}
			return retryAfter;

		} else {
			logger.error("Twitter Exception!", e);
			logger.error("Waiting a short period of time");
			return errorWaitTime;
		}
	}
}
