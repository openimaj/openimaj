package org.openimaj.stream.provider.twitter;

import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.concurrent.BlockingDroppingQueue;

import twitter4j.Query;
import twitter4j.Status;

/**
 * A concrete version of the {@link AbstractTwitterSearchDataset} which pushes
 * the {@link Status}s into the stream.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TwitterSearchDataset extends AbstractTwitterSearchDataset<Status> {
	/**
	 * Construct the dataset from the given API token and query. The stream is
	 * backed by an {@link ArrayBlockingDroppingQueue} with a single item
	 * capacity.
	 * 
	 * @param query
	 *            the Twitter search query
	 * @param token
	 *            the Twitter api authentication credentials
	 */
	public TwitterSearchDataset(Query query, final TwitterAPIToken token) {
		this(query, token, new ArrayBlockingDroppingQueue<Status>(1));
	}

	/**
	 * Construct the dataset from the given API token, query and buffer.
	 * 
	 * @param query
	 *            the Twitter search query
	 * @param token
	 *            the Twitter api authentication credentials
	 * @param buffer
	 *            the buffer to hold {@link Status}s before they are consumed.
	 */
	public TwitterSearchDataset(Query query, final TwitterAPIToken token, BlockingDroppingQueue<Status> buffer) {
		super(token, buffer, query);
	}

	@Override
	protected void registerStatus(Query query, Status status, String rawjson) throws InterruptedException {
		register(status);
	}

	@Override
	public Query getQuery() {
		return this.query;
	}
}
