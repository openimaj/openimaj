package org.openimaj.rdf.storm.topology;

import java.util.List;

import net.lag.kestrel.thrift.Item;

import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.writing.NTripleWritingScheme;

import backtype.storm.spout.KestrelThriftClient;

import com.google.common.collect.Sets;

/**
 * Print everything from a kestrel queue forever
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelQueuePrinter implements Runnable {

	private KestrelServerSpec spec;
	private String queue;

	/**
	 * @param spec
	 *            the kestrel server to connect to
	 * @param queue
	 *            the queue to read from
	 */
	public KestrelQueuePrinter(KestrelServerSpec spec, String queue) {
		this.spec = spec;
		this.queue = queue;
	}

	@Override
	public void run() {
		KestrelThriftClient client = null;
		NTripleWritingScheme scheme = null;
		try {
			client = new KestrelThriftClient(spec.host, spec.port);
			scheme = new NTripleWritingScheme();
		} catch (TException e) {
		}
		while (true) {
			List<Item> itemList;
			try {
				itemList = client.get(queue, 1, 100, 100);
			} catch (TException e) {
				e.printStackTrace();
				break;
			}
			for (Item item : itemList) {
				try {
					client.confirm(queue, Sets.newHashSet(item.get_id()));
				} catch (TException e) {
					System.out.println("Could not confirm! " + e.getMessage());
					break;
				}
				if (item != null) {
					List<Object> thing = scheme.deserialize(item.get_data());
					for (Object object : thing) {
						System.out.println(object);
					}
				}
			}
		}
	}
}
