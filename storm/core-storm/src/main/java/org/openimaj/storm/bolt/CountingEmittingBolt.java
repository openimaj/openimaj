package org.openimaj.storm.bolt;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * A simple Storm {@link IBasicBolt} whose purpose is to
 * increment a count on the reciept of a tuple
 * followed by an emit of the same tuple.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CountingEmittingBolt extends BaseRichBolt{

	/**
	 *
	 */
	private static final long serialVersionUID = -2567619894656997375L;
	private static final String fieldValueFormat = "(%s) %s";
	private Fields fields;
	private int count;
	private OutputCollector collector;

	/**
	 * @param fields the fields to expect and emit
	 */
	public CountingEmittingBolt(Fields fields) {
		this.fields = new Fields(fields.toList());
		count = 0;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(fields);
	}
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		Object[] values = new Object[this.fields.size()];
		count ++;
		for (int i = 0; i < values.length; i++) {
			values[i] = input.getValueByField(this.fields.get(i)).toString();
		}
		collector.emit(input, new Values(values));
		collector.ack(input);
		System.out.println(String.format("Seen: %s",count));
	}

}
