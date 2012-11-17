package org.openimaj.rdf.storm.topology.bolt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scala.actors.threadpool.Arrays;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Holds the variables, rule and bolt for a given compilation. This wrapper is
 * necessary as
 * bolts may be shared (i.e. have the same pattern) but may have different
 * bindings and variables.
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class CompilationStormRuleReteBoltHolder {
	private StormReteBolt bolt;
	private String[] outputFields;
	private Rule rule;

	/**
	 * @param bolt
	 *            the bolt instance
	 * @param rule
	 *            the rule using this bolt (bindings extracted from this rule
	 *            rather than the bolt's rule)
	 */
	public CompilationStormRuleReteBoltHolder(StormReteBolt bolt, Rule rule) {
		this.bolt = bolt;
		setRule(rule);
	}

	/**
	 * The variables of this compilation are null
	 * 
	 * @param bolt
	 */
	public CompilationStormRuleReteBoltHolder(StormReteBolt bolt) {
		this.bolt = bolt;
	}

	/**
	 * @param fieldsTemplate
	 * @return String[]
	 */
	public static String[] extractFields(List<ClauseEntry> fieldsTemplate) {
		Map<String, Integer> fields = new HashMap<String, Integer>();
		int current = 0;
		for (ClauseEntry ce : fieldsTemplate)
		{
			if (ce instanceof TriplePattern) {
				Node s = ((TriplePattern) ce).getSubject();
				Node p = ((TriplePattern) ce).getPredicate();
				Node o = ((TriplePattern) ce).getObject();

				current = checkAddNode(s, fields, current);
				current = checkAddNode(p, fields, current);
				current = checkAddNode(o, fields, current);
				if (o.isLiteral() && o.getLiteralValue() instanceof Functor) {
					Functor functor = (Functor) o.getLiteralValue();
					for (Node n : functor.getArgs())
					{
						current = checkAddNode(n, fields, current);
					}
				}
			}
		}

		String[] orderedNames = new String[current];
		for (Entry<String, Integer> name : fields.entrySet()) {
			orderedNames[name.getValue()] = name.getKey();
		}
		return orderedNames;
	}

	private static int checkAddNode(Node s, Map<String, Integer> fields, int current) {
		if (s.isVariable() && !fields.containsKey(s.getName())) {
			fields.put(s.getName(), current++);
		}
		return current;
	}

	/**
	 * Get the names of the variable fields output from this Bolt.
	 * 
	 * @return String[]
	 */
	public String[] getVars() {
		return this.outputFields;
	}

	/**
	 * Set the names of the variable fields output from this Bolt.
	 * 
	 * @param newVars
	 */
	public void setVars(String[] newVars) {
		this.outputFields = newVars;
	}

	/**
	 * 
	 * @return get the rule of this {@link CompilationStormRuleReteBoltHolder}
	 *         rather than the underlying bolt
	 */
	public Rule getRule() {
		return this.rule;
	}

	@Override
	public String toString() {
		if (rule != null)
			return this.rule.toShortString();
		else
			return bolt.toString();
	}

	/**
	 * @return the bolt being compiled
	 */
	public StormReteBolt getBolt() {
		return bolt;
	}

	/**
	 * @param rule
	 *            set the rule and therefore the vars of this compilation
	 */
	public void setRule(Rule rule) {
		this.rule = rule;
		@SuppressWarnings("unchecked")
		List<ClauseEntry> outputTemplate = Arrays.asList(rule.getHead());
		this.setVars(extractFields(outputTemplate));
	}
}
