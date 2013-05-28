package org.openimaj.demos.irc;

import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.openimaj.data.dataset.StreamingDataset;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.stream.BlockingDroppingBufferedStream;

public abstract class IRCStreamingDataset<T> extends BlockingDroppingBufferedStream<T> implements StreamingDataset<T>{

	private class IRCStreamBot extends PircBot{
		public IRCStreamBot() {
			this.setName("IRCStreamBot");
		}
		@Override
		protected void onMessage(String channel, String sender, String login, String hostname, String message) {
			try {
				T construct = IRCStreamingDataset.this.construct(channel,sender,login,hostname,message);
				if(construct == null) return;
				IRCStreamingDataset.this.register(
					construct
				);
			} catch (Throwable e) {
				System.out.println("INTERRUPTED!");
				e.printStackTrace();
			}
		}
	}

	protected IRCStreamingDataset(BlockingDroppingQueue<T> buffer, String hostname, String channel) throws IOException {
		super(buffer);
		PircBot bot = new IRCStreamBot();
		try {
			bot.connect(hostname);
			bot.joinChannel(channel);
			System.out.println("Connected!");
		} catch (NickAlreadyInUseException e) {
			throw new IOException(e);
		} catch (IOException e) {
			throw new IOException(e);
		} catch (IrcException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void register(T obj) throws InterruptedException {
		super.register(obj);
	}

	/**
	 * Called by {@link PircBot#onMessage}
	 * @param channel the channel to which the message was sent
	 * @param sender the sender of the message (their nick)
	 * @param login the login of the message sender
	 * @param hostname the hostname of the message sender
	 * @param message the message itself
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
