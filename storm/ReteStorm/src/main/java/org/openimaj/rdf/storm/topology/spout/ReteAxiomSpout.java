package org.openimaj.rdf.storm.topology.spout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * This spout, given a set of {@link Rule} instances (which are checked to be axioms) emits the rule and its head.
 * This is generally fed directly to a {@link ReteConflictSetBolt} instance.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteAxiomSpout implements IRichSpout{

	List<String> axioms = new ArrayList<String>();
	private SpoutOutputCollector collector;

	private Iterator<String> ruleIterator;


	/**
	 * Does nothing
	 */
	public ReteAxiomSpout() {
	}

	/**
	 * @param rule the axiom to add and fire
	 */
	public void addAxiom(Rule rule){
		this.axioms.add(rule.toString());
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 4620171661143283169L;

	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;

		this.ruleIterator = this.axioms.iterator();
	}

	@Override
	public void close() {
	}

	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void nextTuple() {
		if(!this.ruleIterator.hasNext()) return;

		String axiomStr = this.ruleIterator.next();
		Rule axiom = Rule.parseRule(axiomStr);
		BindingVector env = new BindingVector(new Node[axiom.getNumVars()]);
//		if(!ReteRuleUtil.shouldFire(axiom,env, true,false)) {
//			return;
//		}
		ReteTopologyRuleContext context = new ReteTopologyRuleContext.IgnoreAdd(axiom,env);
		if(!context.shouldFire(true)){
			// The axiom shouldn't fire. Should this be checked again?
			return;
		}

		Object environment = env.getEnvironment();

		Values bindingsRule = new Values(environment);
		bindingsRule.add(axiom.toString());
		this.collector.emit(bindingsRule);
	}

	@Override
	public void ack(Object msgId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fail(Object msgId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("bindings", "rule"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}
