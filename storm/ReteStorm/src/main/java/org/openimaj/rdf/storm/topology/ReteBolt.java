package org.openimaj.rdf.storm.topology;

import java.util.Map;

import org.openimaj.rdf.storm.spout.NTriplesSpout;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;

import com.hp.hpl.jena.reasoner.rulesys.impl.RETENode;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETESinkNode;

/**
 * A ReteBolt wraps a {@link RETENode} of some kind and provides the clauses of
 * the provided triple
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class ReteBolt extends BaseRichBolt implements RETESinkNode{

	/**
	 *
	 */
	private static final long serialVersionUID = 4118928454986874401L;
	protected OutputCollector collector;
	protected TopologyContext context;
	@SuppressWarnings("rawtypes")
	protected Map stormConf;

	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.context = context;
		this.stormConf = stormConf;
	}

	@Override
	public RETENode clone(@SuppressWarnings("rawtypes") Map netCopy, RETERuleContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(NTriplesSpout.FIELDS);
	}

}
