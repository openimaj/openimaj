package org.openimaj.storm.bolt;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

/**
 * A simple bolt that prints what it sees
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PrintingBolt implements IRichBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = 6446447769256755247L;
	private static final String fieldValueFormat = "(%s) %s";
	private TopologyContext context;
	private OutputCollector collector;

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
//		System.out.println("declarer called!");
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,OutputCollector collector) {
		this.collector = collector;
		
	}

	@Override
	public void execute(Tuple input) {
		for (String  field : input.getFields()) {
			Object value = input.getValueByField(field);
			System.out.println(String.format(fieldValueFormat,field,value));
		}
		collector.ack(input);

	}

	@Override
	public void cleanup() {
	}
}
