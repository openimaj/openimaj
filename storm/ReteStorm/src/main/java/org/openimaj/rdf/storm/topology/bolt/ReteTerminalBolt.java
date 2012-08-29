package org.openimaj.rdf.storm.topology.bolt;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.topology.ReteRuleUtil;

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

		// Check if the rule should still fire (i.e. check the functors)
		// TODO: ???
		// now pass on the bindings
		Rule rule = Rule.parseRule(ruleString);
		ReteRuleUtil.shouldFire(rule,env, true);
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
