package org.openimaj.rdf.storm.topology;

import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
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

	protected final static Logger logger = Logger.getLogger(ReteBolt.class);

	private static final Fields FIELDS = new Fields("binding");
	protected OutputCollector collector;
	protected TopologyContext context;
	@SuppressWarnings("rawtypes")
	protected Map stormConf;

	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.context = context;
		this.stormConf = stormConf;
		prepare();
	}

	protected abstract void prepare();

	@Override
	public RETENode clone(@SuppressWarnings("rawtypes") Map netCopy, RETERuleContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(FIELDS);
	}

	protected void emitBinding(Tuple anchor, BindingVector binding) {
		this.collector.emit(anchor, new Values(new SerialisableNodes(binding.getEnvironment())));
	}

	protected BindingVector extractBindings(Tuple input) {
		SerialisableNodes snodes = (SerialisableNodes) input.getValue(0);
		Node[] nodes = snodes.getNodes();
		BindingVector env = new BindingVector(nodes);
		return env;
	}

	@Override
	public void fire(BindingVector env, boolean isAdd) {
	}

}
