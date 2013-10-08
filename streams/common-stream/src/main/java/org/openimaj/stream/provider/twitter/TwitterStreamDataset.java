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
