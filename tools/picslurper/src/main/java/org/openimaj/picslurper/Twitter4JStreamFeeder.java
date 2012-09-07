package org.openimaj.picslurper;

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
		StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	        	slurper.handleStatus(status);
	        }
	        
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	        public void onException(Exception ex) {
	            ex.printStackTrace();
	        }
			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// TODO Auto-generated method stub
				
			}
	    };
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance(accessToken);
		twitterStream.addListener(listener);
		twitterStream.sample();
	}
}
