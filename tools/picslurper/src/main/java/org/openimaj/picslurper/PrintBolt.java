package org.openimaj.picslurper;

import java.util.Map;

import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

public class PrintBolt implements IBasicBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6446447769256755247L;

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		ReadableWritableJSON json = (ReadableWritableJSON) input.getValue(0);
		System.out.println(json.get("text"));
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

}
