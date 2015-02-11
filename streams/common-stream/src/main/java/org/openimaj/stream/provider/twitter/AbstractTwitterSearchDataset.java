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
package org.openimaj.stream.provider.twitter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.StreamingDataset;
import org.openimaj.twitter.utils.Twitter4jUtil;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.stream.BlockingDroppingBufferedStream;
import org.openimaj.util.stream.Stream;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * Calls the {@link Twitter#search(Query)} function periodically and offers all
 * discovered {@link Status} instances with the underlying
 * {@link BlockingDroppingBufferedStream}. This stream initially takes all
 * search results available and then attempts to retrieve results which occurred
 * after the latest Tweet seen. Apart form this, this {@link Stream} makes no
 * special efforts to:
 *
 * <ul>
 * <li>stop duplicate items
 * <li>stop when a search returns nothing
 * <li>remove retweets or spam.
 * </ul>
 * These must all be handled externally!
 * <p>
 * This class is abstract in terms of the object the stream consumes. Instances
 * of this class can construct the specific object they require for each twitter
 * status using:
 * <ul>
 * <li>the query that created it
 * <li>the status object
 * <li>the raw json of the status
 * </ul>
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 *            Type of items in the stream
 */
public abstract class AbstractTwitterSearchDataset<T> extends BlockingDroppingBufferedStream<T>
implements
StreamingDataset<T>
{

	final class TwitterAPIRunnable implements Runnable {
		private final Twitter twitter;

		private class QueryMetaInfo {
			private long newestID = -1;
			private long newest = -1;
			private int backoff = 0;
		}

		private Map<Query, QueryMetaInfo> metaInfoMap;

		private TwitterAPIRunnable(Twitter twitter) {
			this.twitter = twitter;
			metaInfoMap = new HashMap<Query, QueryMetaInfo>();
		}

		@Override
		public void run() {
			while (true) {
				try {
					logger.debug("Querying...");
					final Query query = AbstractTwitterSearchDataset.this.getQuery();

					QueryMetaInfo metaInfo = metaInfoMap.get(query);
					if (metaInfo == null)
						metaInfoMap.put(query, metaInfo = new QueryMetaInfo());

					final QueryResult res = twitter.search(query);
					if (metaInfo.newest != -1) {
						query.sinceId(metaInfo.newestID + 1);
					}

					if (res.getCount() == 0) {
						metaInfo.backoff++;
						Thread.sleep(ZERO_RESULT_BACKOFF * metaInfo.backoff);
						logger.error("Backing off");
					} else {
						metaInfo.backoff = 0;
					}

					for (final Status status : res.getTweets()) {
						String rawjson = DataObjectFactory.getRawJSON(status);
						rawjson = DataObjectFactory.getRawJSON(status);

						AbstractTwitterSearchDataset.this.registerStatus(query, status, rawjson);
						final long tweetTime = status.getCreatedAt().getTime();

						if (tweetTime > metaInfo.newest) {
							metaInfo.newest = tweetTime;
							metaInfo.newestID = status.getId();
						}
					}

					Thread.sleep(SLEEP_PER_SEARCH);
				} catch (final InterruptedException e) {
					logger.error("Thread interuppted!", e);
					close();
				} catch (final TwitterException e) {
					final long waitTime = Twitter4jUtil.handleTwitterException(e, DEFAULT_ERROR_BUT_NO_WAIT_TIME);
					try {
						Thread.sleep(waitTime);
					} catch (final InterruptedException e1) {
					}
				}
			}
		}
	}

	private static final long DEFAULT_ERROR_BUT_NO_WAIT_TIME = 5000;
	private static final long SLEEP_PER_SEARCH = (1000 * 60) / 30l;
	private static final int ZERO_RESULT_BACKOFF = 1000;

	protected Query query;
	protected Logger logger = Logger.getLogger(TwitterSearchDataset.class);
	protected Configuration config;
	protected Twitter twitter;

	/**
	 * @param token
	 * @param buffer
	 */
	protected AbstractTwitterSearchDataset(TwitterAPIToken token, BlockingDroppingQueue<T> buffer, Query query) {
		super(buffer);

		this.config = makeConfiguration(token);
		this.twitter = new TwitterFactory(config).getInstance();
		this.query = query;

		startSearch();
	}

	/**
	 * Handle the given incoming status and optionally {@link #register(Object)}
	 * it with the stream.
	 *
	 * @param query
	 *            the query
	 * @param status
	 *            the parsed {@link Status}
	 * @param rawjson
	 *            the raw json string
	 * @throws InterruptedException
	 */
	protected abstract void registerStatus(Query query, Status status, String rawjson) throws InterruptedException;

	private void startSearch() {
		GlobalExecutorPool.getPool().execute(new TwitterAPIRunnable(twitter));
	}

	/**
	 * Get the current query
	 *
	 * @return the query
	 */
	public abstract Query getQuery();

	protected Configuration makeConfiguration(TwitterAPIToken token) {
		final ConfigurationBuilder cb = new ConfigurationBuilder()
		.setOAuthConsumerKey(token.consumerKey)
		.setOAuthConsumerSecret(token.consumerSecret)
		.setOAuthAccessToken(token.accessToken)
		.setOAuthAccessTokenSecret(token.accessSecret);
		cb.setJSONStoreEnabled(true);

		return cb.build();
	}

	@Override
	public T getRandomInstance() {
		return this.next();
	}

	@Override
	public int numInstances() {
		return Integer.MAX_VALUE;
	}
}
