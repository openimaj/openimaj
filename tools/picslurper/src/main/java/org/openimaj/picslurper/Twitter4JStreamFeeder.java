package org.openimaj.picslurper;

import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.GlobalExecutorPool.DaemonThreadFactory;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.FixedSizeBlockingChunkPartitioner;
import org.openimaj.util.queue.BoundedPriorityQueue;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

/**
 * Uses the {@link TwitterStreamFactory} of twitter4j and oAuth using
 * 
 * @author Jonathan Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
 *         (ss@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 * 
 */
public class Twitter4JStreamFeeder implements StatusFeeder {
	Logger logger = Logger.getLogger(Twitter4JStreamFeeder.class);

	private final class StatusTimeComparator implements Comparator<Status> {

		@Override
		public int compare(Status o1, Status o2) {
			if (o2 == null && o1 == null) {
				return 0;
			}
			if (o2 == null) {
				return -1;
			}
			if (o1 == null) {
				return 1;
			}

			final long id1 = o1.getId();
			final long id2 = o2.getId();
			if (id1 < id2) {
				return -1;
			} else if (id1 > id2) {
				return 1;
			}

			return 0;
		}

	}

	private final class PriorityQueueStatusListener implements StatusListener {
		private BoundedPriorityQueue<Status> queue;

		public PriorityQueueStatusListener(final PicSlurper slurper) {
			this.queue = new BoundedPriorityQueue<Status>(1000, new StatusTimeComparator());
			// Start a thread which feeds the slurper from the queue
			// final ThreadPoolExecutor pool = GlobalExecutorPool.getPool();
			final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(slurper.nThreads,
					new DaemonThreadFactory());
			final FixedSizeBlockingChunkPartitioner<Status> partitioner = new FixedSizeBlockingChunkPartitioner<Status>(
					this.queue);
			final Operation<Status> r = new Operation<Status>() {

				@Override
				public void perform(Status s) {
					slurper.handleStatus(s);
				}

			};
			new Thread(new Runnable() {

				@Override
				public void run() {
					Parallel.forEach(partitioner, r, pool);
				}
			}).start();

		}

		@Override
		public void onStatus(Status status) {
			if (status.getURLEntities() != null && status.getURLEntities().length != 0) {
				synchronized (queue) {
					queue.add(status);
				}
				logger.debug("Adding status to queue, current queue size: " + queue.size());
				// try {
				// Thread.sleep(100);
				// } catch (InterruptedException e) {
				// }
			}

		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		}

		@Override
		public void onException(Exception ex) {
			ex.printStackTrace();
		}

		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStallWarning(StallWarning warning) {
			// TODO Auto-generated method stub

		}
	}

	private AccessToken accessToken;

	/**
	 * @throws TwitterException
	 * 
	 */
	public Twitter4JStreamFeeder() throws TwitterException {
		// Twitter twitter = new TwitterFactory().getInstance();
		// twitter.setOAuthConsumer(
		// System.getProperty("twitter4j.oauth.consumerKey"),
		// System.getProperty("twitter4j.oauth.consumerSecret")
		// );
		// RequestToken requestToken = twitter.getOAuthRequestToken();
		this.accessToken = new AccessToken(
				System.getProperty("twitter4j.oauth.accessKey"),
				System.getProperty("twitter4j.oauth.accessSecret")
				);
	}

	@Override
	public void feedStatus(final PicSlurper slurper) {
		final StatusListener listener = new PriorityQueueStatusListener(slurper);
		final TwitterStream twitterStream = new TwitterStreamFactory().getInstance(accessToken);
		twitterStream.addListener(listener);
		twitterStream.sample();
	}
}
