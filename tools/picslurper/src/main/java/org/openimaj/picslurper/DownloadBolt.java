package org.openimaj.picslurper;

import java.io.File;
import java.util.Map;

import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

public class DownloadBolt implements IRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6459004768597784620L;
	private boolean stats;
	private File globalStats;
	private File outputLocation;
	private OutputCollector collector;

	public DownloadBolt(boolean stats, File globalStats, File outputLocation) {
		this.stats = stats;
		this.globalStats = globalStats;
		this.outputLocation = outputLocation;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,OutputCollector collector) {
		this.collector = collector;

	}

	@Override
	public void execute(Tuple input) {
		ReadableWritableJSON json = (ReadableWritableJSON) input.getValue(0);
		StatusConsumer consumer = new StatusConsumer(json, stats,globalStats,outputLocation);
		try {
			consumer.call();
			collector.ack(input);
		} catch (Exception e) {
			collector.fail(input);
		}
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
