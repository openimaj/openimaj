/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
