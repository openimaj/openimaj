/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
