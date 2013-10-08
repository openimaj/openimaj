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

import org.apache.log4j.Logger;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.openimaj.data.dataset.StreamingDataset;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.stream.BlockingDroppingBufferedStream;

/**
 * Abstract base class for producing a stream of items from an IRC channel.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of items produced
 */
public abstract class AbstractIRCStreamDataset<T> extends BlockingDroppingBufferedStream<T>
		implements
		StreamingDataset<T>
{
	private static Logger logger = Logger.getLogger(AbstractIRCStreamDataset.class);

	private class IRCStreamBot extends PircBot {
		public IRCStreamBot() {
			this.setName("IRCStreamBot");
		}

		@Override
		protected void onMessage(String channel, String sender, String login, String hostname, String message) {
			try {
				final T construct = AbstractIRCStreamDataset.this.construct(channel, sender, login, hostname, message);
				if (construct == null)
					return;

				AbstractIRCStreamDataset.this.register(construct);
			} catch (final Throwable e) {
				logger.warn("INTERRUPTED! " + e);
			}
		}
	}

	protected AbstractIRCStreamDataset(BlockingDroppingQueue<T> buffer, String hostname, String channel)
			throws IOException
	{
		super(buffer);

		final PircBot bot = new IRCStreamBot();
		try {
			bot.connect(hostname);
			bot.joinChannel(channel);
			logger.debug("Connected!");
		} catch (final NickAlreadyInUseException e) {
			throw new IOException(e);
		} catch (final IOException e) {
			throw new IOException(e);
		} catch (final IrcException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void register(T obj) throws InterruptedException {
		super.register(obj);
	}

	/**
	 * Called by {@link PircBot#onMessage}
	 * 
	 * @param channel
	 *            the channel to which the message was sent
	 * @param sender
	 *            the sender of the message (their nick)
	 * @param login
	 *            the login of the message sender
	 * @param hostname
	 *            the hostname of the message sender
	 * @param message
	 *            the message itself
	 * @return the object the stream produces
	 */
	public abstract T construct(String channel, String sender, String login, String hostname, String message);

	@Override
	public T getRandomInstance() {
		return this.next();
	}

	@Override
	public int numInstances() {
		return Integer.MAX_VALUE;
	}
}
