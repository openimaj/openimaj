/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.sparql.topology.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteFilterBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteJoinBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteTerminalBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteTerminalBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteJoinBolt;
import org.openimaj.rdf.storm.topology.builder.BaseStormReteTopologyBuilder;
import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;

import scala.actors.threadpool.Arrays;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.TopologyBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.sparql.core.TriplePath;

import eu.larkc.csparql.parser.StreamInfo;

/**
 * The simple topology builders make no attempt to optimise the joins. This base
 * interface takes care of recording filters, joins etc. and leaves the job of
 * actually adding the bolts to the topology as well as the construction of the
 * {@link ReteConflictSetBolt} instance down to its children.
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class BaseSPARQLReteTopologyBuilder extends SPARQLReteTopologyBuilder {

	private static Logger logger = Logger.getLogger(BaseStormReteTopologyBuilder.class);
	/**
	 * the name of the final bolt
	 */
	public static final String FINAL_TERMINAL = "final_term";

	private BoltDeclarer finalTerminalBuilder;
	private StormSPARQLReteTerminalBolt term;
	private Map<String, StormSPARQLReteBolt> rule;
	private Map<String, StormSPARQLReteBolt> bolts;
	private Map<String, List<String>> priorBolts;
	private String prior;

	@Override
	public String prepareSourceSpout(TopologyBuilder builder, Set<StreamInfo> streams) {
		return null;
	}

	@Override
	public void initTopology(SPARQLReteTopologyBuilderContext context) {
		this.bolts = new HashMap<String, StormSPARQLReteBolt>();
		this.priorBolts = new HashMap<String, List<String>>();
		ReteConflictSetBolt finalTerm = constructConflictSetBolt(context);
		if (finalTerm != null)
		{
			this.finalTerminalBuilder = context.builder.setBolt(FINAL_TERMINAL, finalTerm, 1); // There is explicity 1 and only 1 Conflict set
		}
	}

	@Override
	public void startGroup(SPARQLReteTopologyBuilderContext context) {
		// Groups represent a join. One doesn't exist, construct the root group.
		// if the root group exists, add a new group as a component to merge
	}

	@Override
	public void endGroup(SPARQLReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFilter(SPARQLReteTopologyBuilderContext context) {

		for (TriplePath triple : context.filterClause.getPattern().getList()) {
			if (triple.isTriple()) {
				TriplePattern tp = constructTriplePattern(triple.asTriple(), context);
				String boltName = constructNewBoltName(tp);

				StormSPARQLReteBolt filterBolt;
				if (bolts.containsKey(boltName)) {
					logger.debug(String.format("Filter bolt %s used from existing rule", boltName));
					filterBolt = bolts.get(boltName);
				} else {
					filterBolt = this.constructReteFilterBolt(tp);
					if (filterBolt == null) {
						logger.debug(String.format("Filter bolt %s was null, not adding", boltName));
						return;
					}
					logger.debug(String.format("Filter bolt %s created from clause %s", boltName, context.filterClause.toString()));
					bolts.put(boltName, filterBolt);
					priorBolts.put(boltName, new ArrayList<String>());
				}
				rule.put(boltName, filterBolt);
			}
		}
	}

	private TriplePattern constructTriplePattern(Triple asTriple, SPARQLReteTopologyBuilderContext context) {
		Node o = asTriple.getObject();
		Node p = asTriple.getPredicate();
		Node s = asTriple.getSubject();

		if (s.isVariable())
			s = updateNode((Node_Variable) s, context.bindingVector);
		if (p.isVariable())
			p = updateNode((Node_Variable) p, context.bindingVector);
		if (o.isVariable())
			o = updateNode((Node_Variable) o, context.bindingVector);
		else if (o.isLiteral() && o.getLiteralValue() instanceof Functor)
		{
			Node[] fargs = ((Functor) o.getLiteralValue()).getArgs();
			for (int i = 0; i < fargs.length; i++) {
				Node node = fargs[i];
				if (node.isVariable())
					fargs[i] = updateNode((Node_Variable) node, context.bindingVector);
			}

		}

		return new TriplePattern(s, p, o);
	}

	private Node_RuleVariable updateNode(Node_Variable o, HashMap<String, Integer> bindingVector) {
		return new Node_RuleVariable(o.getName(), bindingVector.get(o.getName()));
	}

	private StormSPARQLReteBolt constructReteFilterBolt(TriplePattern filter) {
		List<ClauseEntry> template = new ArrayList<ClauseEntry>();
		template.add(filter);
		return new StormSPARQLReteFilterBolt(new Rule(template, template));
	}

	private String constructNewBoltName(TriplePattern tp) {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(tp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void createJoins(SPARQLReteTopologyBuilderContext context) {
		// Now construct the beta networks (this could be optimised)
		Set<String> boltNames = new HashSet<String>();
		boltNames.addAll(rule.keySet());
		while (boltNames.size() > 1) {
			Iterator<String> names = boltNames.iterator();
			join: while (names.hasNext()) {
				String currentBoltName = names.next();
				StormSPARQLReteBolt currentBolt = rule.get(currentBoltName);
				String[] currentVars = currentBolt.getVars();
				currentBolt.getRule();
				// Remove the bolt from the rule, so that it does not attempt to match against itself.
				// If no joins are made then it will be added to the nextLevel map.
				names.remove();

				while (boltNames.size() > 0) {
					names = boltNames.iterator();
					while (names.hasNext()) {
						String otherBoltName = names.next();
						StormSPARQLReteBolt otherBolt = rule.get(otherBoltName);
						String[] otherVars = otherBolt.getVars();
						for (String v : currentVars) {
							if (Arrays.asList(otherVars).contains(v)) {
								names.remove();
								List<ClauseEntry> template = new ArrayList<ClauseEntry>();
								template.addAll(Arrays.asList(currentBolt.getRule().getHead()));
								template.addAll(Arrays.asList(otherBolt.getRule().getHead()));

								// Create the string representing the variable-name-independently ordered
								// output graph (this makes it repeatable irrespective of component bolts).
								// This involves sorting the template, again independently of variable
								// names, which means the fields will be output in the same order irrespective
								// of their names, thanks to being ordered by location in the template.
								String newJoinName = VariableIndependentReteRuleToStringUtils.clauseToString(template);
								StormSPARQLReteBolt newJoin;
								if (bolts.containsKey(newJoinName)) {
									newJoin = bolts.get(newJoinName);
								} else {
									newJoin = constructReteJoinBolt(currentBoltName, otherBoltName, template);
									bolts.put(newJoinName, newJoin);
								}

								rule.put(newJoinName, newJoin);
								boltNames.add(newJoinName);

								continue join;
							}
						}
					}
				}
			}
		}
		prior = boltNames.iterator().next();
	}

	@Override
	public void finishQuery(SPARQLReteTopologyBuilderContext context) {
		logger.debug("Compiling the terminal node instance");
		// Now construct the terminal
		if (prior != null) {
			term = constructTerminalBolt(context);
		} else {
			return; // This should never really happen, it implies an empty
					// rule
		}

		if (term == null) {
			logger.debug("Terminal was null, not connecting the terminal");
		}
		else {
			logger.debug("Connecting the terminal instance to " + prior);
			// We have a prior, we have a terminal bolt, we can go ahead and
			// make addition to the topology
			// FIXME: This is wrong, the name of the terminal bolt should reflect what it does more accurately
			String terminalName = String.format("%s", context.query.simpleQuery.isSelectType());
			context.builder.setBolt(terminalName, term).shuffleGrouping(prior);

			logger.debug("Connecting the final terminal to " + terminalName);
			finalTerminalBuilder.shuffleGrouping(terminalName);
		}

		logger.debug("Connecting the filter and join instances to the source/final terminal instances");
		// Now add the nodes to the actual topology
		for (Entry<String, StormSPARQLReteBolt> nameFilter : rule.entrySet()) {
			String name = nameFilter.getKey();
			IRichBolt bolt = nameFilter.getValue();
			if (bolt instanceof StormSPARQLReteFilterBolt)
				connectFilterBolt(context, name, bolt);
			else if (bolt instanceof StormSPARQLReteJoinBolt)
				connectJoinBolt(context, name, (StormSPARQLReteJoinBolt) bolt);
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
	public void connectJoinBolt(SPARQLReteTopologyBuilderContext context, String name, StormSPARQLReteJoinBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt, 1);
		midBuild.fieldsGrouping(bolt.getLeftBolt(), bolt.getJoinFields());
		midBuild.fieldsGrouping(bolt.getRightBolt(), bolt.getJoinFields());
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
	public void connectFilterBolt(SPARQLReteTopologyBuilderContext context, String name, IRichBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt);
		// All the filter bolts are given triples from the source spout
		// and the final terminal
		midBuild.shuffleGrouping(context.source);
		if (this.finalTerminalBuilder != null)
			midBuild.shuffleGrouping(FINAL_TERMINAL);
	}

	// Bolt Construction

	/**
	 * @param context
	 * @return the conflict set bolt usually describing what is done with
	 *         triples in the stream
	 */
	public ReteConflictSetBolt constructConflictSetBolt(SPARQLReteTopologyBuilderContext context) {
		return new ReteConflictSetBolt();
	}

	/**
	 * @param context
	 * @return the {@link ReteTerminalBolt} usually the buffer between the
	 *         network proper and the {@link ReteConflictSetBolt}
	 */
	public StormSPARQLReteTerminalBolt constructTerminalBolt(SPARQLReteTopologyBuilderContext context) {
		return new StormSPARQLReteTerminalBolt(null);
	}

	/**
	 * @param left
	 *            the left source of the join
	 * @param right
	 *            the right source of the join
	 * @param template
	 * @return the {@link StormReteJoinBolt} usually combining two
	 *         {@link StormReteFilterBolt} instances, {@link StormReteJoinBolt}
	 *         instances, or a combination of the two
	 */
	public StormSPARQLReteBolt constructReteJoinBolt(String left, String right, List<ClauseEntry> template) {
		String[] currentVars = rule.get(left).getVars();
		String[] otherVars = rule.get(right).getVars();
		String[] newVars = StormReteBolt.extractFields(template);
		int[] templateLeft = new int[newVars.length];
		int[] templateRight = new int[newVars.length];
		int[] matchLeft = new int[currentVars.length];
		int[] matchRight = new int[otherVars.length];

		for (int l = 0; l < currentVars.length; l++)
			matchLeft[l] = Arrays.asList(otherVars).indexOf(currentVars[l]);
		for (int r = 0; r < otherVars.length; r++)
			matchRight[r] = Arrays.asList(currentVars).indexOf(otherVars[r]);
		for (int n = 0; n < newVars.length; n++) {
			templateLeft[n] = Arrays.asList(currentVars).indexOf(newVars[n]);
			templateRight[n] = Arrays.asList(otherVars).indexOf(newVars[n]);
		}
		return new StormSPARQLReteJoinBolt(left, matchLeft, templateLeft, right, matchRight, templateRight, new Rule(template, template));
	}

}
