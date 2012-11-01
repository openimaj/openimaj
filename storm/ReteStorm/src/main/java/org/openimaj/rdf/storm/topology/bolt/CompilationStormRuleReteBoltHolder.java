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

public class CompilationStormRuleReteBoltHolder {
	private StormRuleReteBolt bolt;
	private String[] outputFields;

	public CompilationStormRuleReteBoltHolder(StormRuleReteBolt bolt, Rule rule) {
		this.bolt = bolt;
		List<ClauseEntry> outputTemplate = Arrays.asList(rule.getHead());
		this.setVars(extractFields(outputTemplate));
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

	public Rule getRule() {
		return this.bolt.getRule();
	}

}
