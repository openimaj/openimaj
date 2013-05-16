package org.openimaj.demos.twitter;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.StreamingDataset;
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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterSearchAPIDataset extends BlockingDroppingBufferedStream<Status> implements StreamingDataset<Status> {
	protected static final long SLEEP_PER_SEARCH = (1000 * 60)/10l;
	protected static final int ZERO_RESULT_BACKOFF = 2000;
	final private Query query;
	Logger logger = Logger.getLogger(TwitterSearchAPIDataset.class);
	private Configuration config;
	public TwitterSearchAPIDataset(Query query,final TwitterAPIToken token, BlockingDroppingQueue<Status> buffer) {
		super(buffer);

		this.query = query;
		this.config = makeConfiguration(token);
		final Twitter twitter = new TwitterFactory(config).getInstance();

		GlobalExecutorPool.getPool().execute(new Runnable() {


			private long newestID = -1;
			private long newest = -1;
			private int backoff = 0;
			@Override
			public void run() {
				while(true){
					try {
						QueryResult res = twitter.search(TwitterSearchAPIDataset.this.query);
						if(newest!=-1){
							TwitterSearchAPIDataset.this.query.sinceId(newestID+1);
						}
						if(res.getCount() == 0){
							backoff ++;
							Thread.sleep(ZERO_RESULT_BACKOFF*backoff);
						}
						else{
							backoff = 0;
						}
						for (Status status : res.getTweets()) {
							register(status);
							long tweetTime = status.getCreatedAt().getTime();
							if(tweetTime > newest){
								newest = tweetTime;
								newestID = status.getId();
							}
						}
						Thread.sleep(SLEEP_PER_SEARCH);
					} catch (InterruptedException e) {
						logger.error("Thread interuppted!",e);
						close();
					} catch (TwitterException e) {
						if(e.exceededRateLimitation()){
							long retryAfter = e.getRetryAfter() * 1000;
							logger.debug(String.format("Rate limit exceeded, waiting %dms",retryAfter));
							try {
								Thread.sleep(retryAfter);
							} catch (InterruptedException e1) {
								logger.error("Thread interuppted!",e1);
								close();
							}
						}else{
							logger.error("Twitter Exception!",e);
							close();
						}
					}
				}
			}
		});
	}

	private Configuration makeConfiguration(TwitterAPIToken token) {
		final ConfigurationBuilder cb = new ConfigurationBuilder()
				.setOAuthConsumerKey(token.consumerKey)
				.setOAuthConsumerSecret(token.consumerSecret)
				.setOAuthAccessToken(token.accessToken)
				.setOAuthAccessTokenSecret(token.accessSecret);

		return cb.build();
	}

	@Override
	public Status getRandomInstance() {
		return this.next();
	}

	@Override
	public int numInstances() {
		return Integer.MAX_VALUE;
	}
}
