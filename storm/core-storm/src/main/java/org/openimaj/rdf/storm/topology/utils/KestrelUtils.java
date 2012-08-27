package org.openimaj.rdf.storm.topology.utils;

import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;

import backtype.storm.spout.KestrelThriftClient;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelUtils {

	/**
	 * @param spec
	 *            the server to connect to
	 * @param queues
	 *            the queues to expunge
	 * @throws TException
	 */
	public static void deleteQueues(KestrelServerSpec spec, String... queues) throws TException {
		KestrelThriftClient client = new KestrelThriftClient(spec.host, spec.port);
		for (String queue : queues) {
			client.delete_queue(queue);
		}
	}

}
