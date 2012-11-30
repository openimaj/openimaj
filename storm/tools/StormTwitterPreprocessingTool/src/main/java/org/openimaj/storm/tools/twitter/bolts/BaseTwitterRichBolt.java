package org.openimaj.storm.tools.twitter.bolts;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public abstract class BaseTwitterRichBolt extends BaseRichBolt {
	private OutputCollector collector;
	Logger logger = LoggerFactory.getLogger(BaseRichBolt.class);

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.prepare();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		new Fields("tweet");
	}

	@Override
	public void execute(Tuple input) {
		try {
			String t = (String) input.getValueByField("tweet");
			processTweet(t);
		} catch (Exception e) {
			logger.error("Failed to read tweet from tuple: ", e);
		}
		collector.ack(input);
	}

	public abstract void processTweet(String string) throws Exception;

	public abstract void prepare();
}
