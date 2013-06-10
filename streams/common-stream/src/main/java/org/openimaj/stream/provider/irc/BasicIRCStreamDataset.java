package org.openimaj.stream.provider.irc;

import java.io.IOException;

import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.data.Context;

/**
 * Basic streaming dataset from IRC messages. Messages are provided as
 * {@link Context} objects.
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
