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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.lag.kestrel.thrift.Item;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;

import backtype.storm.Config;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

/**
 * This spout can be used to consume messages in a reliable way from a cluster
 * of Kestrel servers. It is recommended that you set the parallelism hint to a
 * multiple of the number of Kestrel servers, otherwise the read load will be
 * higher on some Kestrel servers than others.
 */
@SuppressWarnings("serial")
public class KestrelThriftSpout extends BaseRichSpout {
	public static Logger LOG = Logger.getLogger(KestrelThriftSpout.class);

	public static final long BLACKLIST_TIME_MS = 1000 * 60;
	public static final int BATCH_SIZE = 4000;

	private List<String> _hosts = null;
	private int _port = -1;
	private String _queueName = null;
	private SpoutOutputCollector _collector;
	private Scheme _scheme;

	private List<KestrelClientInfo> _kestrels;
	private int _emitIndex;

	private Queue<EmitItem> _emitBuffer = new LinkedList<EmitItem>();

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
	}

	private static class KestrelClientInfo {
		public Long blacklistTillTimeMs;
		public String host;
		public int port;

		private KestrelThriftClient client;

		public KestrelClientInfo(String host, int port) {
			this.host = host;
			this.port = port;
			this.blacklistTillTimeMs = 0L;
			this.client = null;
		}

		public KestrelThriftClient getValidClient() throws TException {
			if (this.client == null) { // If client was blacklisted, remake it.
				LOG.info("Attempting reconnect to kestrel " + this.host + ":" + this.port);
				this.client = new KestrelThriftClient(this.host, this.port);
			}
			return this.client;
		}

		public void closeClient() {
			if (this.client != null) {
				this.client.close();
				this.client = null;
			}
		}
	}

	public KestrelThriftSpout(List<String> hosts, int port, String queueName, Scheme scheme) {
		if (hosts.isEmpty()) {
			throw new IllegalArgumentException("Must configure at least one host");
		}
		_port = port;
		_hosts = hosts;
		_queueName = queueName;
		_scheme = scheme;
	}

	public KestrelThriftSpout(String hostname, int port, String queueName, Scheme scheme) {
		this(Arrays.asList(hostname), port, queueName, scheme);
	}

	public KestrelThriftSpout(String hostname, int port, String queueName) {
		this(hostname, port, queueName, new RawScheme());
	}

	public KestrelThriftSpout(List<String> hosts, int port, String queueName) {
		this(hosts, port, queueName, new RawScheme());
	}

	public Fields getOutputFields() {
		return _scheme.getOutputFields();
	}

	int _messageTimeoutMillis;

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		// TODO: should switch this to maxTopologyMessageTimeout
		final Number timeout = (Number) conf.get(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS);
		_messageTimeoutMillis = 1000 * timeout.intValue();
		_collector = collector;
		_emitIndex = 0;
		_kestrels = new ArrayList<KestrelClientInfo>();
		final int numTasks = context.getComponentTasks(context.getThisComponentId()).size();
		final int myIndex = context.getThisTaskIndex();
		final int numHosts = _hosts.size();
		if (numTasks < numHosts) {
			for (final String host : _hosts) {
				_kestrels.add(new KestrelClientInfo(host, _port));
			}
		} else {
			final String host = _hosts.get(myIndex % numHosts);
			_kestrels.add(new KestrelClientInfo(host, _port));
		}
	}

	@Override
	public void close() {
		for (final KestrelClientInfo info : _kestrels)
			info.closeClient();

		// Closing the client connection causes all the open reliable reads to
		// be aborted.
		// Thus, clear our local buffer of these reliable reads.
		_emitBuffer.clear();

		_kestrels.clear();
	}

	public boolean bufferKestrelGet(int index) {
		assert _emitBuffer.size() == 0; // JTODO

		final KestrelClientInfo info = _kestrels.get(index);

		final long now = System.currentTimeMillis();
		if (now > info.blacklistTillTimeMs) {
			List<Item> items = null;
			try {
				items = info.getValidClient().get(_queueName, BATCH_SIZE, 0, _messageTimeoutMillis);
				// if(items.size()!=0)LOG.debug("Got this many items: " +
				// items.size());
			} catch (final TException e) {
				LOG.error("Error reading from client: " + e.getMessage());
				blacklist(info, e);
				return false;
			}

			assert items.size() <= BATCH_SIZE;
			// LOG.info("Kestrel batch get fetched " + items.size() +
			// " items. (batchSize= " + BATCH_SIZE +
			// " queueName=" + _queueName + ", index=" + index + ", host=" +
			// info.host + ")");

			final HashSet<Long> toAck = new HashSet<Long>();

			for (final Item item : items) {
				final List<Object> retItems = _scheme.deserialize(item.get_data());

				if (retItems != null) {
					final EmitItem emitItem = new EmitItem(retItems, new KestrelSourceId(index, item.get_id()));

					if (!_emitBuffer.offer(emitItem)) {
						throw new RuntimeException("KestrelThriftSpout's Internal Buffer Enqeueue Failed.");
					}
				} else {
					toAck.add(item.get_id());
				}
			}

			if (toAck.size() > 0) {
				try {
					info.client.confirm(_queueName, toAck);
				} catch (final TException e) {
					blacklist(info, e);
				}
			}

			if (items.size() > 0)
				return true;
		}
		return false;
	}

	public void tryEachKestrelUntilBufferFilled() {
		for (int i = 0; i < _kestrels.size(); i++) {
			final int index = (_emitIndex + i) % _kestrels.size();
			if (bufferKestrelGet(index)) {
				_emitIndex = index;
				break;
			}
		}
		_emitIndex = (_emitIndex + 1) % _kestrels.size();
	}

	int countTriples = 1;
	int lastEmit = countTriples;
	int emptyIterations = 0;

	@Override
	public void nextTuple() {
		if (_emitBuffer.isEmpty())
			tryEachKestrelUntilBufferFilled();
		if (countTriples % 1000 == 0 && countTriples != lastEmit) {
			LOG.debug("Number of triples emitted: " + countTriples);
			LOG.debug("Number of empty iterations: " + emptyIterations);
			emptyIterations = 0;
			lastEmit = countTriples;
		}
		final EmitItem item = _emitBuffer.poll();
		if (item != null) {
			countTriples += 1;
			_collector.emit(item.tuple, item.sourceId);
		} else { // If buffer is still empty here, then every kestrel Q is also
					// empty.
			emptyIterations++;
			Utils.sleep(10);
		}
	}

	private void blacklist(KestrelClientInfo info, Throwable t) {

		// this case can happen when it fails to connect to Kestrel (and so
		// never stores the connection)
		info.closeClient();
		info.blacklistTillTimeMs = System.currentTimeMillis() + BLACKLIST_TIME_MS;

		final int index = _kestrels.indexOf(info);

		// we just closed the connection, so all open reliable reads will be
		// aborted. empty buffers.
		for (final Iterator<EmitItem> i = _emitBuffer.iterator(); i.hasNext();) {
			final EmitItem item = i.next();
			if (item.sourceId.index == index)
				i.remove();
		}
	}

	@Override
	public void ack(Object msgId) {
		final KestrelSourceId sourceId = (KestrelSourceId) msgId;
		final KestrelClientInfo info = _kestrels.get(sourceId.index);

		// if the transaction didn't exist, it just returns false. so this code
		// works
		// even if client gets blacklisted, disconnects, and kestrel puts the
		// item
		// back on the queue
		try {
			if (info.client != null) {
				final HashSet<Long> xids = new HashSet<Long>();
				xids.add(sourceId.id);
				info.client.confirm(_queueName, xids);
			}
		} catch (final TException e) {
			blacklist(info, e);
		}
	}

	@Override
	public void fail(Object msgId) {
		final KestrelSourceId sourceId = (KestrelSourceId) msgId;
		final KestrelClientInfo info = _kestrels.get(sourceId.index);

		// see not above about why this works with blacklisting strategy
		try {
			if (info.client != null) {
				final HashSet<Long> xids = new HashSet<Long>();
				xids.add(sourceId.id);
				info.client.abort(_queueName, xids);
			}
		} catch (final TException e) {
			blacklist(info, e);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(getOutputFields());
	}
}
