package org.openimaj.stream.provider.irc;

import java.io.IOException;

import org.openimaj.stream.provider.irc.BasicIRCStreamDataset.IRCMessage;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.function.Function;

/**
 * Basic streaming dataset from IRC messages. Message objects can have single
 * fields extracted by applying the {@link IRCMessageExtractionFunction}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BasicIRCStreamDataset extends AbstractIRCStreamDataset<IRCMessage> {
	/**
	 * Representation of an IRC Message
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class IRCMessage {
		/**
		 * The IRC channel name
		 */
		public String channel;
		/**
		 * The sender
		 */
		public String sender;
		/**
		 * The login
		 */
		public String login;
		/**
		 * The hostname
		 */
		public String hostname;
		/**
		 * The message text
		 */
		public String message;

		IRCMessage(String channel, String sender, String login, String hostname, String message) {
			this.channel = channel;
			this.sender = sender;
			this.login = login;
			this.hostname = hostname;
			this.message = message;
		}
	}

	/**
	 * Simple function to extract a single field from an {@link IRCMessage} as a
	 * {@link String}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static class IRCMessageExtractionFunction implements Function<IRCMessage, String> {
		/**
		 * The data to output from the function
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 * 
		 */
		public enum OutputMode {
			/**
			 * The IRC channel name
			 */
			Channel,
			/**
			 * The sender
			 */
			Sender,
			/**
			 * The login
			 */
			Login,
			/**
			 * The hostname
			 */
			Hostname,
			/**
			 * The message text
			 */
			Message
		}

		OutputMode output;

		/**
		 * 
		 * @param output
		 */
		public IRCMessageExtractionFunction(OutputMode output) {
			this.output = output;
		}

		@Override
		public String apply(IRCMessage in) {
			switch (output) {
			case Channel:
				return in.channel;
			case Sender:
				return in.sender;
			case Login:
				return in.login;
			case Hostname:
				return in.hostname;
			case Message:
				return in.message;
			}
			return null;
		}
	}

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
		this(new ArrayBlockingDroppingQueue<IRCMessage>(1), hostname, channel);
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
	public BasicIRCStreamDataset(BlockingDroppingQueue<IRCMessage> buffer, String hostname, String channel)
			throws IOException
	{
		super(buffer, hostname, channel);
	}

	@Override
	public IRCMessage construct(String channel, String sender, String login, String hostname, String message) {
		return new IRCMessage(channel, sender, login, hostname, message);
	}

}
