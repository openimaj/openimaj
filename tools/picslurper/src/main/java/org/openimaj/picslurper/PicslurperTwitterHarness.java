package org.openimaj.picslurper;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.openimaj.text.nlp.TweetTokeniserException;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class PicslurperTwitterHarness {

	static Logger logger = Logger.getLogger(PicslurperTwitterHarness.class);

	private PicslurperTwitterHarness() {
	}

	public static void main(String args[]) throws ClientProtocolException, IOException, TweetTokeniserException, InterruptedException {
		TwitterInputStreamFactory factory = TwitterInputStreamFactory.streamFactory();
		if (factory == null)
			return;
		logger.debug("TwitterInputStreamFactory prepared...");
		while (true) {
			logger.debug("Establishing twitter connection");
			try {
				System.setIn(factory.nextInputStream());
				PicSlurper.main("-o /Users/ss/Development/picslurper/01-09-2012 -j 1".split(" "));
			} catch (Throwable e) {
			} finally {
			}
			logger.debug("Connection down, waiting 30 seconds....");
			Thread.sleep(30000);
		}
	}
}
