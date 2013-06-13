package org.openimaj.stream.provider.twitter;

import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.concurrent.BlockingDroppingQueue;

import twitter4j.Status;

/**
 * A concrete version of the {@link AbstractTwitterStreamDataset} which pushes
 * the {@link Status}s into the stream.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TwitterStreamDataset extends AbstractTwitterStreamDataset<Status> {
	/**
	 * Construct the dataset from the given API token. The stream is backed by
	 * an {@link ArrayBlockingDroppingQueue} with a single item capacity.
	 * 
	 * @param token
	 *            the Twitter api authentication credentials
	 */
	public TwitterStreamDataset(final TwitterAPIToken token) {
		this(token, new ArrayBlockingDroppingQueue<Status>(1));
	}

	/**
	 * Construct the dataset from the given API token and buffer.
	 * 
	 * @param token
	 *            the Twitter api authentication credentials
	 * @param buffer
	 *            the buffer to hold {@link Status}s before they are consumed.
	 */
	public TwitterStreamDataset(final TwitterAPIToken token, BlockingDroppingQueue<Status> buffer) {
		super(token, buffer);
	}

	@Override
	protected void registerStatus(Status status, String json) throws InterruptedException {
		register(status);
	}
}
