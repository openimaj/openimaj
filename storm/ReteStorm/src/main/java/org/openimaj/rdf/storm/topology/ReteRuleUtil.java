package org.openimaj.rdf.storm.topology;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEClauseFilter;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETENode;

/**
 * Collection of utils for parsing Jena {@link Rule} instances and constructing
 * various {@link RETENode} instances
 *
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
	public static IndependentPair<RETEClauseFilter, ArrayList<Node>> compileRuleExtractClause(String ruleString,
			int clauseIndex)
	{
		Rule rule = Rule.parseRule(ruleString);
		int numVars = rule.getNumVars();
		TriplePattern clausePattern = (TriplePattern) rule.getBody()[clauseIndex];
		ArrayList<Node> tempClauseVars = new ArrayList<Node>(numVars);
		RETEClauseFilter filter = RETEClauseFilter.compile(clausePattern, numVars, tempClauseVars);
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

//	/**
//	 * An incredibly badly named function whose sole purpose it is to evaluate
//	 * (either safely or not) the various {@link Functor}of the Jena rule
//	 * langauge. A description of all the functors can be found here:
//	 * http://jena.apache.org/documentation/inference/#rules
//	 *
//	 * VERY IMPORTANT: Here we set the engine and graph of the context to null.
//	 * This means {@link RETERuleContext#getGraph()} and
//	 * {@link RETERuleContext#getEngine()} will be null
//	 *
//	 * This has few limitations which culminates in a few {@link Builtin}
//	 * instances which back {@link Functor} instances not working
//	 *
//	 * Firstly, {@link RETERuleContext#getGraph()} is used by the following
//	 * builtins: {@link Drop}, {@link Table}, {@link TableAll} and {@link Hide}
//	 *
//	 * Table can only affect {@link FBRuleInfGraph} and
//	 * {@link LPBackwardRuleInfGraph} which {@link RETERuleInfGraph} is not one.
//	 * Then there is Drop which effects everything with a deduction graph, but
//	 * this makes no sense in a streaming setting anyway.
//	 *
//	 * Further {@link RETERuleContext#contains(Node, Node, Node)} calls
//	 * {@link RETERuleContext#find(Node, Node, Node)} which calls the graph and
//	 * will not work. This is ok because, again, in a streaming environment this
//	 * doesn't make very much sense unless (in the future) ReteConflictBolt
//	 * started holding on to all derived triples
//	 *
//	 * @param rule
//	 *            The rule used in {@link RETERuleContext#setRule(Rule)}
//	 * @param vector
//	 *            The {@link BindingEnvironment} used in
//	 *            {@link RETERuleContext#setEnv(BindingEnvironment)}
//	 * @param allowUnsafe
//	 *            handed {@link RETERuleContext#shouldFire(boolean)}
//	 * @param onlyNonMonotonic
//	 *            decides whether {@link RETERuleContext#shouldStillFire()} or
//	 *            {@link RETERuleContext#shouldFire(boolean)}
//	 *
//	 * @return whether all {@link Functor} clauses returned true
//	 */
//	public static boolean shouldFire(Rule rule, BindingVector vector, boolean allowUnsafe, boolean onlyNonMonotonic) {
//
//		RETERuleContext context = new RETERuleContext(null, null);
//		context.setRule(rule);
//		context.setEnv(vector);
//		if (onlyNonMonotonic) {
//			return context.shouldStillFire();
//		}
//		else {
//			return context.shouldFire(allowUnsafe);
//		}
//	}
//
//	/**
//	 * Similarly to {@link #shouldFire(Rule, BindingVector, boolean, boolean)}
//	 * this function constructs a fake context with the rules and environment
//	 * leaving the engine and graph null.
//	 *
//	 * @param context
//	 * @param f
//	 */
//	public static void fireHeadFunctor(RuleContext context, Functor f) {
//		Builtin imp = f.getImplementor();
//
//		if (imp != null) {
//			imp.headAction(f.getBoundArgs(context.getEnv()), f.getArgLength(), context);
//		} else {
//			throw new ReasonerException("Invoking undefined Functor " + f.getName() + " in " + context.getRule().toShortString());
//		}
//	}

}
