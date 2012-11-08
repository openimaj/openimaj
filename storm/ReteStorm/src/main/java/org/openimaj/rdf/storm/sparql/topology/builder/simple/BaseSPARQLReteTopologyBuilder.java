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
package org.openimaj.rdf.storm.sparql.topology.builder.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt;
import org.openimaj.rdf.storm.sparql.topology.builder.SPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.sparql.topology.builder.SPARQLReteTopologyBuilderContext;
import org.openimaj.rdf.storm.topology.bolt.CompilationStormRuleReteBoltHolder;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.StormRuleReteBolt;
import org.openimaj.rdf.storm.topology.builder.BaseStormReteTopologyBuilder;
import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.util.pair.IndependentPair;

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
 * The simple topology builders make no attempt to optimise the joins. The most
 * basic SPARQL features are supported, namely (SELECT|CONSTRUCT|ASK|DESCRIBE),
 * path matches, global filtera and global joins.
 *
 * Groups are completely ignored.
 *
 *
 * This base
 * interface takes care of recording filters, joins etc. and leaves the job of
 * actually adding the bolts to the topology as well as the construction of the
 * {@link ReteConflictSetBolt} instance down to its subclasses.
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
	private Map<String, StormReteBolt> bolts;
	private List<IndependentPair<String, CompilationStormRuleReteBoltHolder>> boltNames;
	private String prior;

	@Override
	public String prepareSourceSpout(TopologyBuilder builder, Set<StreamInfo> streams) {
		return null;
	}

	@Override
	public void initTopology(SPARQLReteTopologyBuilderContext context) {
		boltNames = new ArrayList<IndependentPair<String, CompilationStormRuleReteBoltHolder>>();
		this.bolts = new HashMap<String, StormReteBolt>();
		StormSPARQLReteConflictSetBolt finalTerm = constructConflictSetBolt(context);
		if (finalTerm != null)
		{
			this.finalTerminalBuilder = context.builder.setBolt(FINAL_TERMINAL, finalTerm, 1); // There is explicity 1 and only 1 Conflict set
		}
	}

	@Override
	public void addFilter(SPARQLReteTopologyBuilderContext context) {

		for (TriplePath triple : context.filterClause.getPattern().getList()) {
			if (triple.isTriple()) {
				TriplePattern tp = constructTriplePattern(triple.asTriple(), context);
				String boltName = constructNewBoltName(tp);
				Rule rule = constructRule(tp);

				StormReteBolt filterBolt;
				if (bolts.containsKey(boltName)) {
					logger.debug(String.format("Filter bolt %s used from existing rule", boltName));
					filterBolt = bolts.get(boltName);
				} else {
					filterBolt = this.constructReteFilterBolt(rule);
					if (filterBolt == null) {
						logger.debug(String.format("Filter bolt %s was null, not adding", boltName));
						return;
					}
					logger.debug(String.format("Filter bolt %s created from clause %s", boltName, context.filterClause.toString()));
					bolts.put(boltName, filterBolt);
				}
				boltNames.add(IndependentPair.pair(boltName, new CompilationStormRuleReteBoltHolder((StormRuleReteBolt) filterBolt, rule)));
			}
		}
	}

	private Rule constructRule(TriplePattern tp) {
		List<ClauseEntry> template = new ArrayList<ClauseEntry>();
		template.add(tp);
		return new Rule(template, template);

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

	private StormReteBolt constructReteFilterBolt(Rule rule) {
		return new StormReteFilterBolt(rule);
	}

	private String constructNewBoltName(TriplePattern tp) {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(tp);
	}

	@Override
	public void createJoins(SPARQLReteTopologyBuilderContext context) {
		join: while (boltNames.size() > 1) {
			int innerSelect = 1;
			IndependentPair<String, CompilationStormRuleReteBoltHolder> currentNameCompBoltPair = boltNames.get(0);
			CompilationStormRuleReteBoltHolder currentBolt = currentNameCompBoltPair.getSecondObject();
			String[] currentVars = currentBolt.getVars();
			while (innerSelect < boltNames.size()) {
				IndependentPair<String, CompilationStormRuleReteBoltHolder> otherNameCompBoltPair = boltNames.get(innerSelect);
				CompilationStormRuleReteBoltHolder otherBolt = otherNameCompBoltPair.secondObject();
				String[] otherVars = otherBolt.getVars();
				for (String v : currentVars) {
					if (Arrays.asList(otherVars).contains(v)) {
						createJoin(0, innerSelect, currentNameCompBoltPair, currentBolt, otherNameCompBoltPair, otherBolt);
						continue join;
					}
				}
				innerSelect++;
				// if we ever get here, inner select failed to find a joining bolt, just pick the 1st one.
				if (innerSelect == boltNames.size()) {
					createJoin(0, 1, currentNameCompBoltPair, currentBolt, otherNameCompBoltPair, otherBolt);
				}
			}

		}
		prior = boltNames.iterator().next().firstObject();
	}

	@SuppressWarnings("unchecked")
	private void createJoin(int outerSelect, int innerSelect, IndependentPair<String, CompilationStormRuleReteBoltHolder> currentNameCompBoltPair, CompilationStormRuleReteBoltHolder currentBolt, IndependentPair<String, CompilationStormRuleReteBoltHolder> otherNameCompBoltPair, CompilationStormRuleReteBoltHolder otherBolt) {
		boltNames.remove(innerSelect);
		boltNames.remove(outerSelect);
		List<ClauseEntry> template = new ArrayList<ClauseEntry>();
		template.addAll(Arrays.asList(currentBolt.getRule().getHead()));
		template.addAll(Arrays.asList(otherBolt.getRule().getHead()));

		// Create the string representing the variable-name-independently ordered
		// output graph (this makes it repeatable irrespective of component bolts).
		// This involves sorting the template, again independently of variable
		// names, which means the fields will be output in the same order irrespective
		// of their names, thanks to being ordered by location in the template.
		String newJoinName = VariableIndependentReteRuleToStringUtils.clauseToString(template);
		StormReteBolt newJoin;
		if (bolts.containsKey(newJoinName)) {
			newJoin = bolts.get(newJoinName);
		} else {
			newJoin = constructReteJoinBolt(currentNameCompBoltPair, otherNameCompBoltPair, template);
		}

		bolts.put(newJoinName, newJoin);
		boltNames.add(IndependentPair.pair(newJoinName, new CompilationStormRuleReteBoltHolder((StormRuleReteBolt) newJoin, new Rule(template, template))));
	}

	@Override
	public void finishQuery(SPARQLReteTopologyBuilderContext context) {
		logger.debug("Connecting last node to the conflict set");
		// Now construct the terminal
		if (prior == null) {
			throw new RuntimeException("Couldn't compile without prior!");
		}
		// We have a prior, we have a terminal bolt, we can go ahead and

		logger.debug("Connecting the final terminal to " + prior);
		finalTerminalBuilder.shuffleGrouping(prior);

		logger.debug("Connecting the filter and join instances to the source/final terminal instances");
		// Now add the nodes to the actual topology
		for (Entry<String, StormReteBolt> nameFilter : bolts.entrySet()) {
			String name = nameFilter.getKey();
			IRichBolt bolt = nameFilter.getValue();
			if (bolt instanceof StormReteFilterBolt)
				connectFilterBolt(context, name, bolt);
			else if (bolt instanceof StormReteJoinBolt)
				connectJoinBolt(context, name, (StormReteJoinBolt) bolt);
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
	public void connectJoinBolt(SPARQLReteTopologyBuilderContext context, String name, StormReteJoinBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt, 1);
		midBuild.fieldsGrouping(bolt.getLeftBolt(), bolt.getLeftJoinFields());
		midBuild.fieldsGrouping(bolt.getRightBolt(), bolt.getRightJoinFields());
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
	public StormSPARQLReteConflictSetBolt constructConflictSetBolt(SPARQLReteTopologyBuilderContext context) {
		return StormSPARQLReteConflictSetBolt.construct(context.query.simpleQuery);
	}

	/**
	 * @param currentNameCompBoltPair
	 *            the left source of the join
	 * @param otherNameCompBoltPair
	 *            the right source of the join
	 * @param template
	 * @return the {@link StormReteJoinBolt} usually combining two
	 *         {@link StormReteFilterBolt} instances, {@link StormReteJoinBolt}
	 *         instances, or a combination of the two
	 */
	public StormReteBolt constructReteJoinBolt(IndependentPair<String, CompilationStormRuleReteBoltHolder> currentNameCompBoltPair, IndependentPair<String, CompilationStormRuleReteBoltHolder> otherNameCompBoltPair, List<ClauseEntry> template) {
		String[] currentVars = currentNameCompBoltPair.secondObject().getVars();
		String[] otherVars = otherNameCompBoltPair.secondObject().getVars();
		String[] newVars = CompilationStormRuleReteBoltHolder.extractFields(template);
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
		return new StormReteJoinBolt(currentNameCompBoltPair.firstObject(), matchLeft, templateLeft, otherNameCompBoltPair.firstObject(), matchRight, templateRight, new Rule(template, template));
	}

}
