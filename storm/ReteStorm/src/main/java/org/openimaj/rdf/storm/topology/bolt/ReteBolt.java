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
package org.openimaj.rdf.storm.topology.bolt;

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
		Object env = binding.getEnvironment();
		this.collector.emit(anchor, new Values(env));
	}

	protected BindingVector extractBindings(Tuple input) {
//		SerialisableNodes snodes = (SerialisableNodes) input.getValue(0);
//		Node[] nodes = snodes.getNodes();
		Node[] nodes = (Node[]) input.getValue(0);
		BindingVector env = new BindingVector(nodes);
		return env;
	}

	@Override
	public void fire(BindingVector env, boolean isAdd) {
	}

}
