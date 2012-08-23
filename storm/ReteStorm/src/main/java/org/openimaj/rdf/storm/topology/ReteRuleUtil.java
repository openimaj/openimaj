package org.openimaj.rdf.storm.topology;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEClauseFilter;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETENode;

/**
 * Collection of utils for parsing Jena {@link Rule} instances and constructing various {@link RETENode} instances
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteRuleUtil {
	protected final static Logger logger = Logger.getLogger(ReteRuleUtil.class);
	/**
	 * Given an individual rule, extract the specific clause from the body and
	 * the variable assignments of the clause
	 *
	 * @param ruleString
	 * @param clauseIndex
	 * @return the clause and the list of variables in the clause
	 */
	public static IndependentPair<RETEClauseFilter, ArrayList<Node_RuleVariable>> compileRuleExtractClause(String ruleString, int clauseIndex) {
		Rule rule = Rule.parseRule(ruleString);
		int numVars = rule.getNumVars();
		TriplePattern clausePattern = (TriplePattern) rule.getBody()[clauseIndex];
		ArrayList<Node_RuleVariable> tempClauseVars = new ArrayList<Node_RuleVariable>(numVars);
		RETEClauseFilter filter = RETEClauseFilter.compile(clausePattern, numVars,tempClauseVars);
		return IndependentPair.pair(filter, tempClauseVars);
	}

	/**
	 * Given an individual rule, extract the specific clause from the body and
	 * return the pattern
	 *
	 * @param ruleString
	 * @param clauseIndex
	 * @return the clause entry
	 */
	public static ClauseEntry extractRuleBodyIndex(String ruleString, int clauseIndex) {
		Rule rule = Rule.parseRule(ruleString);
		ClauseEntry clausePattern = rule.getBody()[clauseIndex];
		return clausePattern;
	}
}
