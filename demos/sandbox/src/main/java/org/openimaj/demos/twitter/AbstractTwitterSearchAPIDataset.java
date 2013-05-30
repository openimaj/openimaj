package org.openimaj.demos.twitter;

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
 * Calls the {@link Twitter#search(Query)} function periodically and offers all discovered {@link Status} instances
 * with the underlying {@link BlockingDroppingBufferedStream}. This stream initially takes all search results available
 * and then attempts to retrieve results which occured after the latest tweet seen. Apart form this, this {@link Stream}
 * makes no special efforts to:
 *
 * 		- stop duplicate items,
 * 		- stop when a search returns nothing
 * 		- remove retweets or spam.
 *
 * This must all be handled externally!
 *
 * This class is abstract in terms of the object the stream consumes.
 * Instances of this class can construct the specific object they require for each twitter status using:
 * 	- the query that created it
 * 	- the status object
 * 	- the raw json of the status
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public abstract class AbstractTwitterSearchAPIDataset<T> extends BlockingDroppingBufferedStream<T> implements StreamingDataset<T> {

	final class TwitterAPIRunnable implements Runnable {
		private final Twitter twitter;
		private class QueryMetaInfo{
			private long newestID = -1;
			private long newest = -1;
			private int backoff = 0;
		}

		private Map<Query,QueryMetaInfo> metaInfoMap;

		private TwitterAPIRunnable(Twitter twitter) {
			this.twitter = twitter;
			metaInfoMap = new HashMap<Query, QueryMetaInfo>();
		}

		@Override
		public void run() {
			while(true){
				try {
					Query query = AbstractTwitterSearchAPIDataset.this.getQuery();
					QueryMetaInfo metaInfo = metaInfoMap.get(query);
					if(metaInfo == null) metaInfoMap.put(query, metaInfo = new QueryMetaInfo());
					QueryResult res = twitter.search(query);
					if(metaInfo.newest!=-1){
						query.sinceId(metaInfo.newestID+1);
					}
					if(res.getCount() == 0){
						metaInfo.backoff ++;
						Thread.sleep(ZERO_RESULT_BACKOFF*metaInfo.backoff);
					}
					else{
						metaInfo.backoff = 0;
					}
					for (Status status : res.getTweets()) {
						String rawjson = DataObjectFactory.getRawJSON(status);
						rawjson = DataObjectFactory.getRawJSON(status);
						AbstractTwitterSearchAPIDataset.this.registerStatus(query,status,rawjson);
						long tweetTime = status.getCreatedAt().getTime();
						if(tweetTime > metaInfo.newest){
							metaInfo.newest = tweetTime;
							metaInfo.newestID = status.getId();
						}
					}
					Thread.sleep(SLEEP_PER_SEARCH);
				} catch (InterruptedException e) {
					logger.error("Thread interuppted!",e);
					close();
				} catch (TwitterException e) {
					long waitTime = Twitter4jUtil.handleTwitterException(e,DEFAULT_ERROR_BUT_NO_WAIT_TIME);
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException e1) {
					}
				}
			}
		}
	}
	private static final long DEFAULT_ERROR_BUT_NO_WAIT_TIME = 5000;
	protected static final long SLEEP_PER_SEARCH = (1000 * 60)/10l;
	protected static final int ZERO_RESULT_BACKOFF = 2000;
	protected Query query;
	protected Logger logger = Logger.getLogger(TwitterSearchAPIDataset.class);
	protected Configuration config;
	protected Twitter twitter;

	/**
	 * @param token
	 * @param buffer
	 */
	protected AbstractTwitterSearchAPIDataset(TwitterAPIToken token, BlockingDroppingQueue<T> buffer) {
		super(buffer);
		this.config = makeConfiguration(token);
		this.twitter = new TwitterFactory(config).getInstance();
	}

	/**
	 * @param query
	 * @param status
	 * @param rawjson
	 * @throws InterruptedException
	 */
	public abstract void registerStatus(Query query, Status status, String rawjson) throws InterruptedException;

	protected void startSearch() {
		GlobalExecutorPool.getPool().execute(new TwitterAPIRunnable(twitter));
	}

	/**
	 * @return the query to work on
	 */
	public abstract Query getQuery() ;

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
