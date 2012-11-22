package org.openimaj.rdf.storm.tool.monitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.openimaj.time.Timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Monitor the input and output queue statistics
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
			if(progress > 0 && timer == null){
				timer = Timer.timer();
			}
			else if (progress > 0){
				float currentThroughput = inputProcessed / (timer.duration() / 1000);
				reportTime(inputTotal, outputProcessed, progress, currentThroughput);
				if(progress == 1.0){
					break;
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
		}
		logger.info("Closing kestrel queue monitor");
		this.monitorWriter.flush();
		this.monitorWriter.close();
		this.client.shutdown();
	}
	private final static Gson gson = new GsonBuilder().create();
	private void reportTime(float inputTotal, float outputProcessed, float progress, float currentThroughput) {
//		String status = String.format(
//				"\n{" +
//					"\n\tinputTotal: %s," +
//					"\n\toutputGenerated: %s," +
//					"\n\tprogress: %s," +
//					"\n\tthroughput: %s" +
//				"\n}",inputTotal,outputProcessed,progress,currentThroughput);
		HashMap<String, String> statusMap = new HashMap<String,String>();
		statusMap.put("inputTotal", "" + inputTotal);
		statusMap.put("outputGenerated", "" + outputProcessed);
		statusMap.put("progress", "" + progress);
		statusMap.put("throughput", "" + currentThroughput);
		String status = gson.toJson(statusMap);

		this.monitorWriter.println(status);
		this.monitorWriter.flush();
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

		String addresses = KestrelServerSpec.kestrelAddressListAsString(opts.kestrelSpecList,KestrelServerSpec.DEFAULT_KESTREL_MEMCACHED_PORT);
		this.client = new MemcachedClient(AddrUtil.getAddresses(addresses));
		this.monitorWriter = new PrintWriter(new FileOutputStream(monitorOutput));
	}

}
