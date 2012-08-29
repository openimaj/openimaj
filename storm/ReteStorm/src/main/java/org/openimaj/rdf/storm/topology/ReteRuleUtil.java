package org.openimaj.rdf.storm.topology;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
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
	public static IndependentPair<RETEClauseFilter, ArrayList<Node>> compileRuleExtractClause(String ruleString, int clauseIndex) {
		Rule rule = Rule.parseRule(ruleString);
		int numVars = rule.getNumVars();
		TriplePattern clausePattern = (TriplePattern) rule.getBody()[clauseIndex];
		ArrayList<Node> tempClauseVars = new ArrayList<Node>(numVars);
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

	public static boolean shouldFire(Rule rule, BindingVector vector, boolean allowUnsafe) {
		return allowUnsafe;
//		RETERuleContext context = new RETERuleContext(GraphFactory.sinkGraph(),null);
//		context.setRule(rule);
//		shouldFire()
//	}
//        // Check any non-pattern clauses
//        for (int i = 0; i < rule.bodyLength(); i++) {
//            Object clause = rule.getBodyElement(i);
//            if (clause instanceof Functor) {
//                // Fire a built in
//                if (allowUnsafe) {
//                    if (!((Functor)clause).evalAsBodyClause(this)) {
//                        // Failed guard so just discard and return
//                        return false;
//                    }
//                } else {
//                    // Don't re-run side-effectful clause on a re-run
//                    if (!((Functor)clause).safeEvalAsBodyClause(this)) {
//                        // Failed guard so just discard and return
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
    }
}
