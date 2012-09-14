package org.openimaj.picslurper.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import org.openimaj.io.IOUtils;
import org.openimaj.picslurper.output.WriteableFailedURL;
import org.zeromq.ZMQ;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ZMQFailClient {


	/**
	 * @param args
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String args[]) throws UnsupportedEncodingException {
		// Prepare our context and subscriber
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket subscriber = context.socket(ZMQ.SUB);

		subscriber.connect("tcp://152.78.64.99:5563");
		subscriber.subscribe("FAIL".getBytes("UTF-8"));

		HashSet<String> seenHosts = new HashSet<String>();
		while (true) {
			// Consume the header
			subscriber.recv(0);
			ByteArrayInputStream stream = new ByteArrayInputStream(subscriber.recv(0));
			WriteableFailedURL instance;
			try {
				instance = IOUtils.read(stream, WriteableFailedURL.class, "UTF-8");
				if(!seenHosts.contains(instance.url.getHost()))
				{
					System.out.println("Failed to deal with: " + instance.url.getHost() );
					System.out.println("Full URL: " + instance.url );
					System.out.println("Reason: " + instance.reason);
					seenHosts.add(instance.url.getHost());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
