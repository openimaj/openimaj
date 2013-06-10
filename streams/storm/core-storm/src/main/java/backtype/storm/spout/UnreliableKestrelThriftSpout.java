/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
 * A version of the {@link KestrelThriftSpout} that is purposefully unreliable
 * and cuts other corners in an effort for improved speeds
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
			String inputQueue)
	{
		this.scheme = scheme;
		this.queue = inputQueue;
		this.port = -1;
		this.hosts = new ArrayList<String>();
		for (final KestrelServerSpec kestrelServerSpec : serverSpecs) {
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
		for (final String specs : this.hosts) {
			clients.add(new KestrelServerSpec(specs, this.port));
		}
		MAX_ITEMS_PER_QUEUE = HOLD_ITEMS / this.clients.size();
		this.tuples = new LinkedList<EmitItem>();
		this.ackIterator = KestrelServerSpec.thriftClientIterator(clients);

	}

	@Override
	public void close() {
		for (final KestrelServerSpec client : this.clients)
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
		final EmitItem poll = this.tuples.poll();
		collector.emit(poll.tuple, poll.sourceId);
		// collector.emit(poll.tuple);
	}

	int readTotal = 0;

	private void getSomeMoreTuples() {
		if (this.tuples.size() > HOLD_ITEMS - MAX_ITEMS_PER_QUEUE / 2)
			return;
		int clientIndex = 0;
		for (final KestrelServerSpec clientSpec : this.clients) {
			try {
				final KestrelThriftClient client = clientSpec.getValidClient();
				final List<Item> ret = client
						.get(this.queue, MAX_ITEMS_PER_QUEUE, TIMEOUT, TIMEOUT * MAX_ITEMS_PER_QUEUE);
				readTotal += ret.size();
				logger.debug("Read total: " + readTotal);
				final Set<Long> ids = new HashSet<Long>();
				for (final Item item : ret) {
					final long kestrelId = item.get_id();
					ids.add(kestrelId);
					final List<Object> deserialize = scheme.deserialize(item.get_data());
					if (deserialize == null)
						continue; // we silently skip null items

					final EmitItem e = new EmitItem(deserialize, new KestrelSourceId(clientIndex, kestrelId));
					this.tuples.add(e);
				}
				// We immediately confirm ALL items. This is the thing that
				// makes it unreliable!
				client.confirm(this.queue, ids);
			} catch (final TException e) {
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
			emitToAckQueue();
		}
	}

	private void emitToAckQueue() {
		logger.debug("Acked: " + acked);
		final float throughput = acked / ((float) ackTimer.duration() / 1000);
		if (this.ackQueue != null) {
			final AckStats stats = new AckStats(throughput);
			final KestrelThriftClient client = getNextValidClient();
			try {
				client.put(this.ackQueue, gson.toJson(stats), 0);
			} catch (final TException e) {
				logger.error("Failed to write acknowledgement");
			}
		}
	}

	private KestrelThriftClient getNextValidClient() {
		return this.ackIterator.next();
	}

	@Override
	public void fail(Object msgId) {
		final KestrelSourceId sourceId = (KestrelSourceId) msgId;
		logger.debug("Failing: " + sourceId);
	}

}
