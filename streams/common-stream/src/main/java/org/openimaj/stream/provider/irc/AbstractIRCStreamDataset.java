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
