package org.openimaj.picslurper.output;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

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

	}

	@Override
	public void newImageDownloaded(WriteableImageOutput written) {
		try {
			StringWriter writer = new StringWriter();
			IOUtils.writeASCII(writer, written);
			publisher.send("IMAGE".getBytes("UTF-8"), ZMQ.SNDMORE);
			boolean sent = publisher.send(writer.toString().getBytes("UTF-8"), 0);
			if(!sent){
				throw new IOException("Send failed");
			}
		} catch (IOException e) {
			logger.error("Unable to send written image: " + written.url);
			logger.error(e.getMessage());
		}
	}

	@Override
	public void failedURL(URL url, String reason) {
		try {
			StringWriter writer = new StringWriter();
			if(url==null)return;
			IOUtils.writeASCII(writer, new WriteableFailedURL(url, reason));
			publisher.send("FAIL".getBytes("UTF-8"), ZMQ.SNDMORE);
			boolean sent = publisher.send(writer.toString().getBytes("UTF-8"), 0);
			if(!sent){
				throw new IOException("Send failed");
			}
		} catch (IOException e) {
			logger.error("Unable to send failure!");
		}
	}

	@Override
	public void finished() {
		publisher.close();
	}

	@Override
	public void prepare() {
		ZMQ.Context context = ZMQ.context(1);
		publisher = context.socket(ZMQ.PUB);

		publisher.bind("tcp://*:5563");
	}

}
