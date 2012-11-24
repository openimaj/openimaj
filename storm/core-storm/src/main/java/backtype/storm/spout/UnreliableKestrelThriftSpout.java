package backtype.storm.spout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.lag.kestrel.thrift.Item;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.time.Timer;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A version of the {@link KestrelThriftSpout} that is purposefully
 * unreliable and cuts other corners in an effort for improved
 * speeds
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class UnreliableKestrelThriftSpout extends BaseRichSpout {

	private class EmitItem {
		public KestrelSourceId sourceId;
		public List<Object> tuple;

		public EmitItem(List<Object> tuple, KestrelSourceId sourceId) {
			this.tuple = tuple;
			this.sourceId = sourceId;
		}
	}

	private static class KestrelSourceId {
		public KestrelSourceId(int index, long id) {
			this.index = index;
			this.id = id;
		}

		int index;
		long id;

		@Override
		public String toString() {
			return String.format("{client:%s,id:%s}", index, id);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3531693744499668571L;
	private static final int HOLD_ITEMS = 1000;
	private static final int TIMEOUT = 100;
	private static final Logger logger = Logger.getLogger(UnreliableKestrelThriftSpout.class);
	private List<KestrelServerSpec> clients;
	private Scheme scheme;
	private Queue<EmitItem> tuples;
	private String queue;
	private int MAX_ITEMS_PER_QUEUE;
	private SpoutOutputCollector collector;
	private List<String> hosts;
	private int port;
	private String ackQueue;
	private Iterator<KestrelThriftClient> ackIterator;

	/**
	 * @param serverSpecs
	 *            servers to connect to in a round robin fasion
	 * @param scheme
	 *            how items should be read
	 * @param inputQueue
	 *            queue from which to read
	 */
	public UnreliableKestrelThriftSpout(
			List<KestrelServerSpec> serverSpecs,
			Scheme scheme,
			String inputQueue) {
		this.scheme = scheme;
		this.queue = inputQueue;
		this.port = -1;
		this.hosts = new ArrayList<String>();
		for (KestrelServerSpec kestrelServerSpec : serverSpecs) {
			this.hosts.add(kestrelServerSpec.host);
			this.port = kestrelServerSpec.port;
		}
		this.ackQueue = null;
	}

	/**
	 * The ackQueue holds statistics about acknowledgement time useful for
	 * measuring topology throughput
	 * 
	 * @param ackQueue
	 */
	public void setAckQueue(String ackQueue) {
		this.ackQueue = ackQueue;
	}

	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		this.clients = new ArrayList<KestrelServerSpec>();
		for (String specs : this.hosts) {
			clients.add(new KestrelServerSpec(specs, this.port));
		}
		MAX_ITEMS_PER_QUEUE = HOLD_ITEMS / this.clients.size();
		this.tuples = new LinkedList<EmitItem>();
		this.ackIterator = KestrelServerSpec.thriftClientIterator(clients);

	}

	@Override
	public void close() {
		for (KestrelServerSpec client : this.clients)
			client.close();
	}

	@Override
	public void nextTuple() {
		getSomeMoreTuples();
		if (this.tuples.size() == 0)
		{
			Utils.sleep(10);
			return;
		}
		EmitItem poll = this.tuples.poll();
		collector.emit(poll.tuple, poll.sourceId);
		//		collector.emit(poll.tuple);
	}

	int readTotal = 0;

	private void getSomeMoreTuples() {
		if (this.tuples.size() > HOLD_ITEMS - MAX_ITEMS_PER_QUEUE / 2)
			return;
		int clientIndex = 0;
		for (KestrelServerSpec clientSpec : this.clients) {
			try {
				KestrelThriftClient client = clientSpec.getValidClient();
				List<Item> ret = client.get(this.queue, MAX_ITEMS_PER_QUEUE, TIMEOUT, TIMEOUT * MAX_ITEMS_PER_QUEUE);
				readTotal += ret.size();
				logger.debug("Read total: " + readTotal);
				Set<Long> ids = new HashSet<Long>();
				for (Item item : ret) {
					long kestrelId = item.get_id();
					ids.add(kestrelId);
					List<Object> deserialize = scheme.deserialize(item.get_data());
					if (deserialize == null)
						continue; // we silently skip null items

					EmitItem e = new EmitItem(deserialize, new KestrelSourceId(clientIndex, kestrelId));
					this.tuples.add(e);
				}
				// We immediately confirm ALL items. This is the thing that makes it unreliable!
				client.confirm(this.queue, ids);
			} catch (TException e) {
			}
			clientIndex++;
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(scheme.getOutputFields());
	}

	private final static Gson gson = new GsonBuilder().create();
	int acked = 0;
	private Timer ackTimer;

	@Override
	public void ack(Object msgId) {
		if (acked == 0) {
			ackTimer = Timer.timer();
		}
		acked++;
		if (acked % 1000 == 0) {
			logger.debug("Acked: " + acked);
			float throughput = acked / ((float) ackTimer.duration() / 1000);
			if (this.ackQueue != null) {
				AckStats stats = new AckStats(throughput);
				KestrelThriftClient client = getNextValidClient();
				try {
					client.put(this.ackQueue, gson.toJson(stats), 0);
				} catch (TException e) {
					logger.error("Failed to write acknowledgement");
				}
			}
		}
	}

	private KestrelThriftClient getNextValidClient() {
		return this.ackIterator.next();
	}

	@Override
	public void fail(Object msgId) {
		KestrelSourceId sourceId = (KestrelSourceId) msgId;
		logger.debug("Failing: " + sourceId);
	}

}
