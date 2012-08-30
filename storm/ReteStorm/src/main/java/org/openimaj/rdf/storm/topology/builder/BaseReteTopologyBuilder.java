package org.openimaj.rdf.storm.topology.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteTerminalBolt;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * The simple topology builders make no attempt to optimise the joins. This base
 * interface takes care of recording filters, joins etc. and leaves the job of
 * actually adding the bolts to the topology as well as the construction of the
 * {@link ReteConflictSetBolt} instance down to its children.
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class BaseReteTopologyBuilder extends ReteTopologyBuilder {
	private static Logger logger = Logger
			.getLogger(BaseReteTopologyBuilder.class);
	/**
	 * the name of the final bolt
	 */
	public static final String FINAL_TERMINAL = "final_term";

	private BoltDeclarer finalTerminalBuilder;
	private ReteTerminalBolt term;
	private HashMap<String, ReteJoinBolt> joins;
	private HashMap<String, ReteFilterBolt> filters;
	private String ruleName;
	private int filterCount = 0;
	private int numVars;
	private String prior;

	@Override
	public void initTopology(ReteTopologyBuilderContext context) {
		ReteConflictSetBolt finalTerm = constructConflictSetBolt(context);
		if (finalTerm != null)
		{
			this.finalTerminalBuilder = context.builder.setBolt(FINAL_TERMINAL, finalTerm,1); // There is explicity 1 and only 1 Conflict set
			this.finalTerminalBuilder.allGrouping(context.axiomSpout);
		}
	}

	@Override
	public void startRule(ReteTopologyBuilderContext context) {
		// Reset the index of the filter
		this.filterCount = 0;
		// These are the "alpha" nodes of the rule (the filters)
		filters = new HashMap<String, ReteFilterBolt>();
		// This are the "beta" nodes of the rule (the joins)
		joins = new HashMap<String, ReteJoinBolt>();
		// This is the terminal bolt (where the head is fired)
		this.term = null;
		this.numVars = context.rule.getNumVars();

		// The rule name, bolts take the form of ruleName_(BODY|HEAD)_count
		this.ruleName = context.rule.getName();
		if (ruleName == null)
			ruleName = nextRuleName();

		logger.debug(String.format("Compiling rule: %s", ruleName));
	}

	@Override
	public void addFilter(ReteTopologyBuilderContext context) {
		String boltName = String.format("%s_filter_%d", ruleName, filterCount);
		// ReteFilterBolt filterBolt = new ReteFilterBolt((TriplePattern)
		// clause, numVars);
		ReteFilterBolt filterBolt = this.constructReteFilterBolt(context, filterCount);
		this.filterCount += 1;
		if (filterBolt == null) {
			logger.debug(String.format("Filter bolt %s was null, not adding", boltName));
			return;
		}
		logger.debug(String.format("Filter bolt %s created from clause %s", boltName, context.filterClause));
		filters.put(boltName, filterBolt);
	}

	@Override
	public void createJoins(ReteTopologyBuilderContext context) {
		// Now construct the beta networks (this could be optimised)
		this.prior = null;
		boolean[] seenVar = new boolean[numVars];
		int joinNumber = 0;
		for (Entry<String, ReteFilterBolt> reteFilterBolt : filters.entrySet()) {
			String currentName = reteFilterBolt.getKey();
			ReteFilterBolt currentBolt = reteFilterBolt.getValue();
			List<Node> clauseVars = currentBolt.getClauseVars();

			// Get all the variable indecies which this join should worry
			// about
			ArrayList<Byte> matchIndices = new ArrayList<Byte>(numVars);
			for (Iterator<Node> iv = clauseVars.iterator(); iv.hasNext();) {
				int varIndex = ((Node_RuleVariable) iv.next()).getIndex();
				if (seenVar[varIndex])
					matchIndices.add(new Byte((byte) varIndex));
				seenVar[varIndex] = true;
			}

			if (prior == null) {
				logger.debug(String
						.format("Found the first filter node: %s, NO JOIN",
								currentName));
				prior = currentName;
			} else {

				// Construct a ReteJoinBolt which knows which variables it
				// should map on
				String boltName = String.format("%s_join_%d", ruleName, joinNumber++);
				ReteJoinBolt reteJoinBolt = constructReteJoinBolt(prior, currentName, matchIndices);
				if (reteJoinBolt == null) {
					logger.debug(String.format("Join %s was null, not adding", boltName));
					return;
				}
				logger.debug(String.format(
						"Constructing join, left=%s, right=%s", prior,
						currentName));
				joins.put(boltName, reteJoinBolt);
				prior = boltName;
			}
		}
	}

	@Override
	public void finishRule(ReteTopologyBuilderContext context) {
		logger.debug("Compiling the terminal node instance");
		// Now construct the terminal
		if (prior != null) {
			// term = new ReteTerminalBolt(context.rule);
			term = constructTerminalBolt(context);
		} else {
			return; // This should never really happen, it insinuates an empty
					// rule
		}

		if (term == null) {
			logger.debug("Not connecting the temrinal");
		}
		else {
			logger.debug("Connecting the terminal instance to " + prior);
			// We have a prior, we have a terminal bolt, we can go ahead and
			// make addition to the topology
			String terminalName = String.format("%s_terminal", ruleName);
			context.builder.setBolt(terminalName, term).shuffleGrouping(prior);

			logger.debug("Connecting the final terminal to " + terminalName);
			finalTerminalBuilder.shuffleGrouping(terminalName);
		}

		logger.debug("Connecting the filter instances to the source/final terminal instances");
		// Now add the nodes to the actual topology
		for (Entry<String, ReteFilterBolt> nameFilter : filters.entrySet()) {
			String name = nameFilter.getKey();
			IRichBolt bolt = nameFilter.getValue();
			connectFilterBolt(context, name, bolt);
		}

		logger.debug("Connecting the join instances to their parents");
		for (Entry<String, ReteJoinBolt> join : joins.entrySet()) {
			String name = join.getKey();
			ReteJoinBolt bolt = join.getValue();
			connectJoinBolt(context, name, bolt);
		}
	}

	/**
	 * Connect a {@link ReteJoinBolt} to its left and right sources. The default
	 * behaviour is to add the bolt as
	 * {@link BoltDeclarer#globalGrouping(String)} with both sources (this might
	 * be optimisabled)
	 *
	 * @param context
	 * @param name
	 * @param bolt
	 */
	public void connectJoinBolt(ReteTopologyBuilderContext context, String name, ReteJoinBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt, 1);
		midBuild.globalGrouping(bolt.getLeftBolt());
		midBuild.globalGrouping(bolt.getRightBolt());
	}

	/**
	 * Connect a {@link ReteFilterBolt} instance to the network. The behavior is
	 * to connect the bolt to the source with a
	 * {@link BoltDeclarer#shuffleGrouping(String)} to the
	 * {@link org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder.ReteTopologyBuilderContext#source}
	 * and the {@link ReteConflictSetBolt} instance
	 *
	 * @param context
	 * @param name
	 * @param bolt
	 */
	public void connectFilterBolt(ReteTopologyBuilderContext context, String name, IRichBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt);
		// All the filter bolts are given triples from the source spout
		// and the final terminal
		midBuild.shuffleGrouping(context.source);
		if (this.finalTerminalBuilder != null)
			midBuild.shuffleGrouping(FINAL_TERMINAL);
	}

	/**
	 * @param context
	 * @return the conflict set bolt usually describing what is done with
	 *         triples in the stream
	 */
	public ReteConflictSetBolt constructConflictSetBolt(ReteTopologyBuilderContext context){
		return new ReteConflictSetBolt();
	}

	/**
	 * @param context
	 * @return the {@link ReteTerminalBolt} usually the buffer between the
	 *         network proper and the {@link ReteConflictSetBolt}
	 */
	public ReteTerminalBolt constructTerminalBolt(ReteTopologyBuilderContext context) {
		return new ReteTerminalBolt(context.rule);
	}

	/**
	 * @param context
	 *            the context in which the filter is constructed
	 * @param filterCount
	 *            the index of the filter in the rule
	 * @return the {@link ReteFilterBolt} usually the filter between the source
	 *         and a join or a terminal. If null the filter isn't added
	 */
	public ReteFilterBolt constructReteFilterBolt(ReteTopologyBuilderContext context,
			int filterCount) {
		return new ReteFilterBolt(context.rule, filterCount);
	}

	/**
	 * @param left
	 *            the left source of the join
	 * @param right
	 *            the right source of the join
	 * @param matchIndices
	 *            the variables to bind
	 *
	 * @return the {@link ReteJoinBolt} usually combining two
	 *         {@link ReteFilterBolt} or {@link ReteJoinBolt} instances
	 */
	public ReteJoinBolt constructReteJoinBolt(String left, String right, ArrayList<Byte> matchIndices) {
		return new ReteJoinBolt(left, right, matchIndices);
	}

}
