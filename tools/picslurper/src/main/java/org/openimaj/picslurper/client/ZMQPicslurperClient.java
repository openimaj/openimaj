package org.openimaj.picslurper.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.openimaj.io.IOUtils;
import org.openimaj.picslurper.output.WriteableImageOutput;
import org.zeromq.ZMQ;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ZMQPicslurperClient {
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		// Prepare our context and subscriber
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket subscriber = context.socket(ZMQ.SUB);

		subscriber.connect("tcp://localhost:5563");
		subscriber.subscribe(new byte[0]);
		while (true) {
			ByteArrayInputStream stream = new ByteArrayInputStream(subscriber.recv(0));
			WriteableImageOutput instance;
			try {
				instance = IOUtils.read(stream, WriteableImageOutput.class, "UTF-8");
				System.out.println("Got URL: " + instance.url + " ( " + instance.stats.imageURLs + " ) ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
