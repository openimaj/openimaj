package org.openimaj.picslurper;

import java.util.Comparator;

import org.openimaj.util.queue.BoundedPriorityQueue;

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
 * @author Jonathan Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class Twitter4JStreamFeeder implements StatusFeeder {
	private final class StatusTimeComparator implements Comparator<Status>{

		@Override
		public int compare(Status o1, Status o2) {
			if(o2 == null && o1 == null){
				return 0;
			}
			if(o2 == null){
				return -1;
			}
			if(o1 == null){
				return 1;
			}

			long id1 = o1.getId() ;
			long id2 = o2.getId() ;
			if(id1 < id2){
				return -1;
			}
			else if(id1 > id2){
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
			new Thread(new Runnable(){
				@Override
				public void run() {
					while(true){
						Status s = nextStatus();
						if(s!=null){
							slurper.handleStatus(s);
						}
					}
				}

			}).start();

		}
		@Override
		public synchronized void onStatus(Status status) {
			queue.add(status);
		}

		public synchronized Status nextStatus(){
			return queue.poll();
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

		@Override
		public void onException(Exception ex) {
		    ex.printStackTrace();
		}

		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
			// TODO Auto-generated method stub

		}
	}
	private AccessToken accessToken;
	/**
	 * @throws TwitterException
	 *
	 */
	public Twitter4JStreamFeeder() throws TwitterException {
//		Twitter twitter = new TwitterFactory().getInstance();
//		twitter.setOAuthConsumer(
//			System.getProperty("twitter4j.oauth.consumerKey"),
//			System.getProperty("twitter4j.oauth.consumerSecret")
//		);
//		RequestToken requestToken = twitter.getOAuthRequestToken();
		this.accessToken = new AccessToken(
			System.getProperty("twitter4j.oauth.accessKey"),
			System.getProperty("twitter4j.oauth.accessSecret")
		);
	}
	@Override
	public void feedStatus(final PicSlurper slurper) {
		StatusListener listener = new PriorityQueueStatusListener(slurper);
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance(accessToken);
		twitterStream.addListener(listener);
		twitterStream.sample();
	}
}
