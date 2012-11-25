package org.openimaj.rdf.storm.tool.monitor;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.lag.kestrel.thrift.Item;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.sparql.topology.builder.group.KestrelStaticDataSPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.openimaj.time.Timer;

import backtype.storm.Config;
import backtype.storm.spout.AckStats;
import backtype.storm.spout.KestrelThriftClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Monitor the input and output queue statistics
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelQueueStatsMonitorMode extends MonitorMode {

	private static final Logger logger = Logger.getLogger(KestrelQueueStatsMonitorMode.class);
	private String inputQueue;
	private String outputQueue;
	private MemcachedClient client;

	enum MemcachedStats {
		age,
		bytes,
		canceled_transactions,
		discarded,
		expired_items,
		items,
		logsize,
		mem_bytes,
		mem_items,
		open_transactions,
		total_flushes,
		total_items,
		transactions,
		waiters
	}

	String queueFormatString = "queue_%s_%s";
	private Timer timer;
	private PrintWriter monitorWriter;
	private String ackQueue;
	private Iterator<KestrelThriftClient> thriftClientIterator;
	private AckStats recentAckStats = null;
	private boolean forceShutDown;

	@Override
	public void run() {
		while (true) {
			if (this.forceShutDown)
				break;
			Map<SocketAddress, Map<String, String>> stats = client.getStats();
			Map<SocketAddress, Map<MemcachedStats, String>> inputStats = extractQueueStats(inputQueue, stats);
			Map<SocketAddress, Map<MemcachedStats, String>> outputStats = extractQueueStats(outputQueue, stats);
			List<AckStats> extractAckStats = extractAckStats(this.ackQueue, stats);
			boolean emptyAck = extractAckStats.size() == 0;
			AckStats newAckStats = mostRecentAckStats(extractAckStats);

			float inputRemaining = 0;
			float inputTotal = 0;
			float outputProcessed = 0;
			for (SocketAddress socket : stats.keySet()) {
				Map<MemcachedStats, String> inputSocketStats = inputStats.get(socket);
				inputRemaining += Integer.parseInt(inputSocketStats.get(MemcachedStats.items));
				inputTotal += Integer.parseInt(inputSocketStats.get(MemcachedStats.total_items));
				Map<MemcachedStats, String> outputSocketStats = outputStats.get(socket);
				outputProcessed += Integer.parseInt(outputSocketStats.get(MemcachedStats.total_items));
			}

			float inputProcessed = inputTotal - inputRemaining;
			float progress = inputProcessed / inputTotal;
			if (progress > 0 && timer == null) {
				timer = Timer.timer();
			}
			else if (progress > 0) {
				float currentThroughput = inputProcessed / (timer.duration() / 1000);
				reportTime(inputTotal, outputProcessed, progress, currentThroughput, newAckStats);
				if (progress == 1.0 && emptyAck) {
					break;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		logger.info("Closing kestrel queue monitor");
		this.monitorWriter.flush();
		this.monitorWriter.close();
		this.client.shutdown();
	}

	private final static Gson gson = new GsonBuilder().create();

	private void reportTime(float inputTotal, float outputProcessed, float progress, float currentThroughput, AckStats newAckStats) {
		//		String status = String.format(
		//				"\n{" +
		//					"\n\tinputTotal: %s," +
		//					"\n\toutputGenerated: %s," +
		//					"\n\tprogress: %s," +
		//					"\n\tthroughput: %s" +
		//				"\n}",inputTotal,outputProcessed,progress,currentThroughput);
		HashMap<String, String> statusMap = new HashMap<String, String>();
		statusMap.put("inputTotal", "" + inputTotal);
		statusMap.put("outputGenerated", "" + outputProcessed);
		statusMap.put("progress", "" + progress);
		statusMap.put("spout_throughput", "" + currentThroughput);
		if (newAckStats != null) {
			statusMap.put("ack_throughput", "" + newAckStats.throughput);
		}
		String status = gson.toJson(statusMap);

		this.monitorWriter.println(status);
		this.monitorWriter.flush();
		logger.debug(status);
	}

	private List<AckStats> extractAckStats(String queue, Map<SocketAddress, Map<String, String>> stats) {
		List<AckStats> ret = new ArrayList<AckStats>();
		if (queue == null) {
			return ret;
		}

		KestrelThriftClient cli = thriftClientIterator.next();
		try {
			List<Item> replies = cli.get(queue, 100, 100, 0);
			for (Item item : replies) {
				AckStats ackStats = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(item.get_data())), AckStats.class);
				ret.add(ackStats);
			}
		} catch (TException e) {
			logger.error("Failed to get ackknowledgement queue");
		}
		return ret;

	}

	private AckStats mostRecentAckStats(List<AckStats> newAckStats) {
		if (newAckStats.size() == 0)
			return recentAckStats;
		for (AckStats newAckStat : newAckStats) {
			if (this.recentAckStats == null) {
				this.recentAckStats = newAckStat;
			}
			else {
				if (this.recentAckStats.timestamp < newAckStat.timestamp) {
					this.recentAckStats = newAckStat;
				}
			}
		}
		return this.recentAckStats;
	}

	private Map<SocketAddress, Map<MemcachedStats, String>> extractQueueStats(String queue, Map<SocketAddress, Map<String, String>> stats)
	{
		Map<SocketAddress, Map<MemcachedStats, String>> ret = new HashMap<SocketAddress, Map<MemcachedStats, String>>();
		for (Entry<SocketAddress, Map<String, String>> inetStats : stats.entrySet()) {
			Map<MemcachedStats, String> memcachedStats = new HashMap<MemcachedStats, String>();
			ret.put(inetStats.getKey(), memcachedStats);
			Map<String, String> inetStat = inetStats.getValue();
			for (MemcachedStats statsKey : MemcachedStats.values()) {
				String statsItemName = String.format(queueFormatString, queue, statsKey.toString());
				String value = inetStat.get(statsItemName);
				if (value == null)
					memcachedStats.put(statsKey, "0");
				else
					memcachedStats.put(statsKey, value);
			}
		}
		return ret;
	}

	@Override
	public void init(ReteStormOptions opts, Config config) throws IOException {
		this.inputQueue = opts.inputQueue;
		this.outputQueue = opts.outputQueue;
		this.ackQueue = (String) config.get(KestrelStaticDataSPARQLReteTopologyBuilder.RETE_TOPOLOGY_KESTREL_ACK_QUEUE);
		this.thriftClientIterator = KestrelServerSpec.thriftClientIterator(opts.kestrelSpecList);
		String addresses = KestrelServerSpec.kestrelAddressListAsString(opts.kestrelSpecList, KestrelServerSpec.DEFAULT_KESTREL_MEMCACHED_PORT);
		this.client = new MemcachedClient(AddrUtil.getAddresses(addresses));
		this.monitorWriter = new PrintWriter(new FileOutputStream(monitorOutput));
	}

	@Override
	public void close() {
		this.forceShutDown = true;
	}

}
