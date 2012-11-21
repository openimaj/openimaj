package org.openimaj.rdf.storm.tool.monitor;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.openimaj.time.Timer;

/**
 * Monitor the input and output queue statistics
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelQueueStatsMonitorMode implements MonitorMode {

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

	@Override
	public void run() {
		while(true){
			Map<SocketAddress, Map<String, String>> stats = client.getStats();
			Map<SocketAddress, Map<MemcachedStats, String>> inputStats = extractQueueStats(inputQueue,stats);
			Map<SocketAddress, Map<MemcachedStats, String>> outputStats = extractQueueStats(outputQueue,stats);
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
			float currentThroughput = inputProcessed / (timer.duration() / 1000);
			reportTime(inputTotal, outputProcessed, progress, currentThroughput);
			if(progress == 1.0){
				break;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}

		}
	}

	private void reportTime(float inputTotal, float outputProcessed, float progress, float currentThroughput) {
		String status = String.format(
				"\n{" +
					"\n\tinputTotal: %s," +
					"\n\toutputGenerated: %s," +
					"\n\tprogress: %s," +
					"\n\tthroughput: %s" +
				"\n}",inputTotal,outputProcessed,progress,currentThroughput);
		logger.debug(status);
	}

	private Map<SocketAddress, Map<MemcachedStats, String>> extractQueueStats(String queue, Map<SocketAddress, Map<String, String>> stats)
	{
		Map<SocketAddress, Map<MemcachedStats, String>> ret = new HashMap<SocketAddress, Map<MemcachedStats,String>>();
		for (Entry<SocketAddress, Map<String, String>> inetStats: stats.entrySet()) {
			Map<MemcachedStats, String> memcachedStats = new HashMap<MemcachedStats, String>();
			ret.put(inetStats.getKey(), memcachedStats  );
			Map<String, String> inetStat = inetStats.getValue();
			for (MemcachedStats statsKey : MemcachedStats.values()) {
				String statsItemName = String.format(queueFormatString,queue,statsKey.toString());
				String value = inetStat.get(statsItemName);
				if(value==null)
					memcachedStats.put(statsKey, "0");
				else
					memcachedStats.put(statsKey, value);
			}
		}
		return ret;
	}

	@Override
	public void init(ReteStormOptions opts) throws IOException {
		this.inputQueue = opts.inputQueue;
		this.outputQueue = opts.outputQueue;
		this.timer = Timer.timer();

		this.client = new MemcachedClient(AddrUtil.getAddresses(String.format("%s:%s",opts.kestrelHost,22133)));
	}

}
