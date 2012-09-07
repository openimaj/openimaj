package org.openimaj.picslurper.output;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.openimaj.io.IOUtils;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 * Using ZeroMQ (because it is awesome and I wanted to learn it so fuck off) we set up a
 * subscription based server which tells anyone listening about any new images.
 *
 * The name of the queue can be specified
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ZMQOutputListener implements OutputListener {

	private static final Logger logger = Logger.getLogger(ZMQOutputListener.class);

	private Socket publisher;

	/**
	 * Construct the publishing connector
	 */
	public ZMQOutputListener() {
		ZMQ.Context context = ZMQ.context(1);
		publisher = context.socket(ZMQ.PUB);

		publisher.bind("tcp://*:5563");
	}

	@Override
	public void newImageDownloaded(WriteableImageOutput written) {
		try {
			StringWriter writer = new StringWriter();
			IOUtils.writeASCII(writer, written);
			boolean sent = publisher.send(writer.toString().getBytes("UTF-8"), 0);
			if(!sent){
				throw new IOException("Send failed");
			}
		} catch (IOException e) {
			logger.error("Unable to send written image: " + written.url);
			logger.error(e.getMessage());
		}
	}

}
