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

import org.openimaj.data.dataset.StreamingDataset;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.stream.BlockingDroppingBufferedStream;

import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * Base class for Live twitter streams based on the live Twitter streaming API.
 * <p>
 * This class is abstract in terms of the object the stream consumes. Instances
 * of this class can construct the specific object they require for each twitter
 * status using:
 * <ul>
 * <li>the status object
 * <li>the raw json of the status
 * </ul>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T>
 *            Type of items in the stream
 */
public abstract class AbstractTwitterStreamDataset<T> extends BlockingDroppingBufferedStream<T>
		implements
		StreamingDataset<T>
{
	/**
	 * Construct with the given Twitter API credentials and buffer
	 * 
	 * @param token
	 *            the Twitter api authentication credentials
	 * @param buffer
	 *            the backing buffer for storing data before consumption from
	 *            the stream
	 */
	public AbstractTwitterStreamDataset(TwitterAPIToken token, BlockingDroppingQueue<T> buffer) {
		super(buffer);

		final TwitterStream twitterStream = new TwitterStreamFactory(makeConfiguration(token)).getInstance();

		twitterStream.addListener(new StatusAdapter() {
			@Override
			public void onStatus(Status status) {
				try {
					registerStatus(status, DataObjectFactory.getRawJSON(status));
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		});

		twitterStream.sample();
	}

	private Configuration makeConfiguration(TwitterAPIToken token) {
		final ConfigurationBuilder cb = new ConfigurationBuilder()
				.setOAuthConsumerKey(token.consumerKey)
				.setOAuthConsumerSecret(token.consumerSecret)
				.setOAuthAccessToken(token.accessToken)
				.setOAuthAccessTokenSecret(token.accessSecret);

		return cb.build();
	}

	/**
	 * Handle the given incoming status and optionally {@link #register(Object)}
	 * it with the stream.
	 * 
	 * @param status
	 *            the parsed {@link Status}
	 * @param json
	 *            the json string
	 * @throws InterruptedException
	 */
	protected abstract void registerStatus(Status status, String json) throws InterruptedException;

	@Override
	public T getRandomInstance() {
		return this.next();
	}

	@Override
	public int numInstances() {
		return Integer.MAX_VALUE;
	}
}
