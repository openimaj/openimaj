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
package org.openimaj.stream.provider.irc;

import java.io.IOException;

import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.data.Context;

/**
 * Basic streaming dataset from IRC messages. Messages are provided as
 * {@link Context} objects. The contexts contain keys for channel, sender,
 * login, hostname and message. All values are {@link String}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BasicIRCStreamDataset extends AbstractIRCStreamDataset<Context> {
	/**
	 * The IRC channel name key
	 */
	public String CHANNEL_KEY = "channel";
	/**
	 * The sender key
	 */
	public String SENDER_KEY = "sender";
	/**
	 * The login key
	 */
	public String LOGIN_KEY = "login";
	/**
	 * The host name key
	 */
	public String HOSTNAME_KEY = "hostname";
	/**
	 * The message text key
	 */
	public String MESSAGE_KEY = "message";

	/**
	 * Connect to the given host and channel. Internally a
	 * {@link ArrayBlockingDroppingQueue} with size of 1 is created to
	 * buffer/drop messages.
	 * 
	 * @param hostname
	 *            the host
	 * @param channel
	 *            the channel
	 * @throws IOException
	 */
	public BasicIRCStreamDataset(String hostname, String channel)
			throws IOException
	{
		this(new ArrayBlockingDroppingQueue<Context>(1), hostname, channel);
	}

	/**
	 * Construct with the given buffer, connecting to the given host and
	 * channel.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param hostname
	 *            the host
	 * @param channel
	 *            the channel
	 * @throws IOException
	 */
	public BasicIRCStreamDataset(BlockingDroppingQueue<Context> buffer, String hostname, String channel)
			throws IOException
	{
		super(buffer, hostname, channel);
	}

	@Override
	public Context construct(String channel, String sender, String login, String hostname, String message) {
		final Context ctx = new Context();

		ctx.put(CHANNEL_KEY, channel);
		ctx.put(SENDER_KEY, sender);
		ctx.put(LOGIN_KEY, login);
		ctx.put(HOSTNAME_KEY, hostname);
		ctx.put(MESSAGE_KEY, message);

		return ctx;
	}
}
