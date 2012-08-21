package org.openimaj.rdf.storm.topology;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

/**
 * Rather than encapsulating a RETETerminal, this terminal bolt encapsulates a
 * {@link RETERuleContext} instance created using a rule. If the rule should
 * fire when events are recieved, the head of the rule is fired. All terminal
 * bolts should be sources for all {@link ReteFilterBolt} instances.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteTerminalBolt extends ReteBolt{


	/**
	 *
	 */
	private static final long serialVersionUID = 3122511470480506524L;
	private String ruleString;

	/**
	 * A terminal bolt with a rule
	 * @param rule
	 */
	public ReteTerminalBolt(Rule rule) {
		this.ruleString = rule.toString();
	}

	/**
	 * A rule-less terminal bolt,probably the final terminal
	 */
	public ReteTerminalBolt() {
	}

	@Override
	public void execute(Tuple input) {
		System.out.println(ruleString);
	}

	@Override
	public void fire(BindingVector env, boolean isAdd) {
		// TODO Auto-generated method stub

	}



}
