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

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETETerminal;

/**
 * Rather than encapsulating the functionality of (though not an instance of)
 * {@link RETETerminal}. If the rule should fire when events are recieved, the
 * head of the rule is fired.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteTerminalBolt extends ReteBolt {

	protected final static Logger logger = Logger.getLogger(ReteTerminalBolt.class);
	/**
	 *
	 */
	private static final long serialVersionUID = 3122511470480506524L;
	private String ruleString;

	/**
	 * A terminal bolt with a rule
	 *
	 * @param rule
	 */
	public ReteTerminalBolt(Rule rule) {
		this.ruleString = rule.toString();
	}

	@Override
	public void execute(Tuple input) {
		BindingVector env = extractBindings(input);
		Rule rule = Rule.parseRule(ruleString);
		ReteTopologyRuleContext context = new ReteTopologyRuleContext.IgnoreAdd(rule, env);
		logger.debug("Checking rule functors");
		if(!context.shouldFire(true)) {
			collector.ack(input);
			return;
		}
		Object environment = env.getEnvironment();

		Values bindingsRule = new Values(environment);
		bindingsRule.add(ruleString);
		this.collector.emit(input, bindingsRule);
		collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("bindings", "rule"));
	}

	@Override
	protected void prepare() {
	}

}
