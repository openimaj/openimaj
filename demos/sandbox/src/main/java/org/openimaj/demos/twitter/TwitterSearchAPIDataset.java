package org.openimaj.demos.twitter;

import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.BlockingDroppingQueue;

import twitter4j.Query;
import twitter4j.Status;

/**
 * A version of the {@link AbstractTwitterSearchAPIDataset} which passes only the status along
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterSearchAPIDataset extends AbstractTwitterSearchAPIDataset<Status> {


	/**
	 * @param query
	 * @param token
	 * @param buffer
	 */
	public TwitterSearchAPIDataset(Query query,final TwitterAPIToken token, BlockingDroppingQueue<Status> buffer) {
		super(token,buffer);
		this.query = query;
		startSearch();
	}

	@Override
	public void registerStatus(Query query, Status status, String rawjson) throws InterruptedException {
		register(status);
	}
	@Override
	public Query getQuery() {
		return this.query;
	}
}
