package org.openimaj.picslurper;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openimaj.text.nlp.TweetTokeniserException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class PicslurperTwitterHarness {

	static Logger logger = Logger.getLogger(PicslurperTwitterHarness.class);

	private PicslurperTwitterHarness() {
	}

	/**
	 * Instantiates a {@link PicSlurper} tool which reads from a stream which
	 * this class constructs. The {@link TwitterInputStreamFactory} is used
	 * which is called again if the connection is dropped for whatever reason
	 * 
	 * @param args
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws TweetTokeniserException
	 * @throws InterruptedException
	 */
	public static void main(String args[]) throws ClientProtocolException, IOException, TweetTokeniserException,
			InterruptedException
	{
		final TwitterInputStreamFactory factory = TwitterInputStreamFactory.streamFactory();
		if (factory == null)
			return;
		logger.debug("TwitterInputStreamFactory prepared...");
		while (true) {
			try {
				logger.debug("Establishing twitter connection at: " + new DateTime());
				System.setIn(factory.nextInputStream());
				PicSlurper.main(args);
			} catch (final Throwable e) {
			} finally {
			}
			logger.debug("Connection down, waiting 30 seconds....");
			Thread.sleep(30000);
		}
	}
}
