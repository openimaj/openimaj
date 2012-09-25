package org.openimaj.rdf.storm.topology.rules;

import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.LPBackwardRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.RETERuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.builtins.AddOne;
import com.hp.hpl.jena.reasoner.rulesys.builtins.AssertDisjointPairs;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Bound;
import com.hp.hpl.jena.reasoner.rulesys.builtins.CountLiteralValues;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Difference;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Drop;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Equal;
import com.hp.hpl.jena.reasoner.rulesys.builtins.GE;
import com.hp.hpl.jena.reasoner.rulesys.builtins.GreaterThan;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Hide;
import com.hp.hpl.jena.reasoner.rulesys.builtins.IsBNode;
import com.hp.hpl.jena.reasoner.rulesys.builtins.IsDType;
import com.hp.hpl.jena.reasoner.rulesys.builtins.IsFunctor;
import com.hp.hpl.jena.reasoner.rulesys.builtins.LE;
import com.hp.hpl.jena.reasoner.rulesys.builtins.LessThan;
import com.hp.hpl.jena.reasoner.rulesys.builtins.ListContains;
import com.hp.hpl.jena.reasoner.rulesys.builtins.ListEntry;
import com.hp.hpl.jena.reasoner.rulesys.builtins.ListLength;
import com.hp.hpl.jena.reasoner.rulesys.builtins.ListMapAsObject;
import com.hp.hpl.jena.reasoner.rulesys.builtins.ListMapAsSubject;
import com.hp.hpl.jena.reasoner.rulesys.builtins.MakeInstance;
import com.hp.hpl.jena.reasoner.rulesys.builtins.MakeSkolem;
import com.hp.hpl.jena.reasoner.rulesys.builtins.MakeTemp;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Max;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Min;
import com.hp.hpl.jena.reasoner.rulesys.builtins.NotBNode;
import com.hp.hpl.jena.reasoner.rulesys.builtins.NotDType;
import com.hp.hpl.jena.reasoner.rulesys.builtins.NotEqual;
import com.hp.hpl.jena.reasoner.rulesys.builtins.NotFunctor;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Now;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Print;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Product;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Quotient;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Regex;
import com.hp.hpl.jena.reasoner.rulesys.builtins.StrConcat;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Sum;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Table;
import com.hp.hpl.jena.reasoner.rulesys.builtins.TableAll;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Unbound;
import com.hp.hpl.jena.reasoner.rulesys.builtins.UriConcat;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEEngine;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * A {@link RETERuleContext} works well with distributed streaming reasoners and
 * allows the resolution of {@link Functor} instances.
 * 
 * VERY IMPORTANT: Here we set the engine and graph of the context to null. This
 * means {@link #getGraph()} and {@link #getEngine()} will be null
 * 
 * This has few limitations which culminates in a few {@link Builtin} instances
 * which back {@link Functor} instances not working
 * 
 * Firstly, {@link RETERuleContext#getGraph()} is used by the following
 * builtins: {@link Drop}, {@link Table}, {@link TableAll} and {@link Hide}
 * 
 * Table can only affect {@link FBRuleInfGraph} and
 * {@link LPBackwardRuleInfGraph} which {@link RETERuleInfGraph} is not one.
 * Then there is Drop which effects everything with a deduction graph, but this
 * makes no sense in a streaming setting anyway.
 * 
 * Further {@link RETERuleContext#contains(Node, Node, Node)} calls
 * {@link RETERuleContext#find(Node, Node, Node)} which calls the graph and will
 * always return a {@link ClosableIterator} which is empty. This is again
 * becuase, in a streaming environment, this doesn't make very much sense unless
 * (in the future) {@link ReteConflictSetBolt} starts holding on to all derived
 * triples. The knock on effect of this is that a few other {@link Builtin}
 * instances and therefore {@link Functor} instnaces will not work. These
 * include: {@link CountLiteralValues}, {@link ListEntry}, {@link ListLength},
 * {@link ListContains}, {@link AssertDisjointPairs}, {@link ListMapAsObject}
 * {@link ListMapAsSubject}. These are basically all the list {@link Builtin}
 * instances
 * 
 * In Summary, the list of supported {@link Builtin} instances is as follows:
 * <ul>
 * <li>{@link AddOne} - addOne(?a, ?b) bind ?b to ?a + 1</li>
 * <li>{@link Bound} - bound(?a) true if ?a is bound</li>
 * <li>{@link Equal} - equal(?a,?b) return ?a == ?b (see also {@link NotEqual})</li>
 * <li>{@link GE} - ge(?a,?b) return ?a >= ?b (see also {@link LE})</li>
 * <li>{@link GreaterThan} - greaterThan(?a,?b) return ?a > ?b (see also {@link LessThan}</li>
 * <li>{@link IsBNode} - isBNode(?a) return if ?a is bound to a {@link Node_Blank} (see also {@link NotBNode})</li>
 * <li>{@link IsDType} - isDType(?a,?b) return true if ?a is bound to a datatype ?b (see also {@link NotDType}</li>
 * <li>{@link IsFunctor} - isFunctor(?a) whether ?a is a functor (see also {@link NotFunctor})</li>
 * <li>{@link MakeInstance} - Make a blank instance of a subject and property</li>
 * <li>{@link MakeSkolem} - binds the first argument to an anonymous node made from the MD5 hash of the rest of the nodes</li>
 * <li>{@link MakeTemp} - create a new {@link AnonId} {@link Node} and bind it to each variable passed in</li>
 * <li>{@link Max} - max(a?,b?,c?) bind the maximum of a? and b? to c? (See also {@link Min})</li>
 * <li>{@link Now} - the first argument is set to the current time</li>
 * <li>{@link Print} - print the value of the bound variable</li>
 * <li>{@link Product} - product(a?,b?,c?) bind the product of a? and b? to c? (See also {@link Sum}, {@link Quotient} and {@link Difference})</li>
 * <li>{@link Regex} - regex(?a,?b,...) match ?a to pattern ?b and bind matching groups in other variables</li>
 * <li>{@link StrConcat} - concat strings (see also {@link UriConcat})</li>
 * <li>{@link Unbound} - true if variables are unbound</li>
 * </ul>
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class ReteTopologyRuleContext extends RETERuleContext {
	private final static Logger logger = Logger.getLogger(RETERuleContext.class);

	/**
	 * An implmenetation of the {@link ReteTopologyRuleContext} such that
	 * {@link Triple} adds are silently ignored. This is useful mainly for
	 * allowing body {@link Functor} instances to fire
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class IgnoreAdd extends ReteTopologyRuleContext {

		/**
		 * @param rule
		 *            the context rule
		 * @param vector
		 *            the context bound variables
		 */
		public IgnoreAdd(Rule rule, BindingVector vector) {
			super(rule, vector);
		}

		@Override
		public void add(Triple t) {
			// TODO Auto-generated method stub

		}

	}

	private static Set<String> unsupported = Sets.newHashSet(
			"drop", "table", "remove", "hide", "tableAll"
			);

	/**
	 * Initialise a {@link RETERuleContext} with a null {@link Graph} and a null
	 * {@link RETEEngine} instance
	 * 
	 * @param rule
	 *            The rule set using {@link #setRule(Rule)}
	 * @param env
	 *            The {@link BindingEnvironment} set using
	 *            {@link #setEnv(BindingEnvironment)}
	 */
	public ReteTopologyRuleContext(Rule rule, BindingEnvironment env) {
		super(null, null);
		this.setEnv(env);
		this.setRule(rule);

	}

	@Override
	public abstract void add(Triple t);

	@Override
	public ClosableIterator<Triple> find(Node s, Node p, Node o) {
		return new ClosableIterator<Triple>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public Triple next() {
				return null;
			}

			@Override
			public void remove() {
			}

			@Override
			public void close() {
			}
		};
	}

	@Override
	public void remove(Triple t) {
		throw new UnsupportedOperationException("Can't remove from streams");
	}

	/**
	 * @param builtin
	 * @return whether the {@link Builtin} is supported
	 */
	public static boolean isUnsupported(Builtin builtin) {
		if (builtin == null)
		{
			return false;
		}
		boolean unsupported = ReteTopologyRuleContext.unsupported.contains(builtin.getName());
		if (unsupported)
			logger.warn("Warning, unsupported clause detected, builtin: " + builtin.getName());
		return unsupported;
	}

	/**
	 * @param functor
	 * @return whether the {@link Functor} is
	 */
	public static boolean isUnsupported(Functor functor) {
		return isUnsupported(functor.getImplementor());
	}

	/**
	 * Check whether the rule should fire in this context. This over-ridden
	 * method makes sure a {@link Functor} is supported using
	 * {@link #isUnsupported(Functor)} before calling it. Functors which are not
	 * supported return false (i.e. rules do not fire)
	 */
	@Override
	public boolean shouldFire(boolean allowUnsafe) {
		// Check any non-pattern clauses
		for (int i = 0; i < rule.bodyLength(); i++) {
			Object clause = rule.getBodyElement(i);
			if (clause instanceof Functor) {
				if (isUnsupported((Functor) clause)) {
					return false;
				}
				// Fire a built in
				if (allowUnsafe) {
					if (!((Functor) clause).evalAsBodyClause(this)) {
						// Failed guard so just discard and return
						return false;
					}
				} else {
					// Don't re-run side-effectful clause on a re-run
					if (!((Functor) clause).safeEvalAsBodyClause(this)) {
						// Failed guard so just discard and return
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Check if a rule from the conflict set is still OK to fire. Just checks
	 * the non-monotonic guards such as noValue.
	 */
	@Override
	public boolean shouldStillFire() {
		// Check any non-pattern clauses
		for (int i = 0; i < rule.bodyLength(); i++) {
			Object clause = rule.getBodyElement(i);
			if (clause instanceof Functor) {
				Builtin builtin = ((Functor) clause).getImplementor();
				if (builtin != null && !builtin.isMonotonic()) {
					if (isUnsupported(builtin)) {
						return false;
					}
					if (!((Functor) clause).evalAsBodyClause(this)) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
