package org.openimaj.rdf.storm.topology.bolt;

import java.util.List;
import java.util.Map;

import scala.actors.threadpool.Arrays;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;

import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * A {@link StormReteBolt} which has some specific support for rules
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class StormRuleReteBolt extends StormReteBolt{

	/**
	 *
	 */
	private static final long serialVersionUID = 3977605874827044044L;
	private String ruleString;
	private int variableCount;
	private List<ClauseEntry> outputTemplate;

	/**
	 * The rule backing this bolt
	 * @param rule
	 */
	@SuppressWarnings("unchecked")
	public StormRuleReteBolt(Rule rule) {
		this.ruleString = rule.toString();
		this.variableCount = countVariables(Arrays.asList(rule.getHead()));
	}

	private int countVariables(List<ClauseEntry> fieldEntry) {

		return CompilationStormRuleReteBoltHolder.extractFields(fieldEntry).length;
	}

	@Override
	public int getVariableCount() {
		return this.variableCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
		this.outputTemplate = Arrays.asList(Rule.parseRule(this.ruleString).getHead());
		super.prepare(stormConf, context, collector);
	}

	/**
	 * Get the rule on which this FlexibleReteBolt is built.
	 *
	 * @return Rule
	 */
	public Rule getRule() {
		return Rule.parseRule(this.ruleString);
	}

}
