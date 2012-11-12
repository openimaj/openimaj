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
package org.openimaj.rdf.storm.sparql.topology.builder.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLFilterBolt;
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
import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.util.pair.IndependentPair;

import scala.actors.threadpool.Arrays;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.TopologyBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;

import eu.larkc.csparql.parser.StreamInfo;

/**
 * This topology builder attempts to support groups of paths and subqueries in
 * SPARQL queries
 * 
 * 
 * This base interface takes care of recording filters, joins etc. and leaves
 * the job of actually adding the bolts to the topology as well as the
 * construction of the {@link ReteConflictSetBolt} instance down to its
 * subclasses.
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class BaseSPARQLReteTopologyBuilder extends SPARQLReteTopologyBuilder {
	private static Logger logger = Logger.getLogger(BaseSPARQLReteTopologyBuilder.class);
	/**
	 * the name of the final bolt
	 */
	public static final String FINAL_TERMINAL = "final_term";

	private BoltDeclarer finalTerminalBuilder;
	private Map<String, StormReteBolt> bolts;
	private List<List<NamedCompilation>> secondToLast;
	private SPARQLReteTopologyBuilderContext context;
	private StormSPARQLReteConflictSetBolt finalTerm;

	private static class NamedCompilation extends
			IndependentPair<String, CompilationStormRuleReteBoltHolder> {

		public NamedCompilation(String name, CompilationStormRuleReteBoltHolder compilation) {
			super(name, compilation);
		}

	}

	@Override
	public abstract String prepareSourceSpout(TopologyBuilder builder, Set<StreamInfo> streams);

	@Override
	public void initTopology(SPARQLReteTopologyBuilderContext context) {
		this.context = context;
		this.bolts = new HashMap<String, StormReteBolt>();
		this.finalTerm = constructConflictSetBolt();
		if (finalTerm != null)
		{
			// There is explicity 1 and only 1 Conflict set
			this.finalTerminalBuilder = context.builder.setBolt(FINAL_TERMINAL, finalTerm, 1);
		}
	}

	@Override
	public void compile(Element el) {
		this.secondToLast = visit(el);
	}

	private List<List<NamedCompilation>> visit(Element el) {
		if (el instanceof ElementGroup) {
			return visit((ElementGroup) el);
		}
		else if (el instanceof ElementPathBlock) {
			return visit((ElementPathBlock) el);
		}
		else if (el instanceof ElementUnion) {
			return visit((ElementUnion) el);
		}
		else if (el instanceof ElementOptional) {
			return visit((ElementOptional) el);
		}
		else if (el instanceof ElementSubQuery) {
			return visit((ElementSubQuery) el);
		}
		else if (el instanceof ElementFilter) {
			return visit((ElementFilter) el);
		}
		throw new RuntimeException("Unable to handle query statement element: " + el.getClass());
	}

	private List<List<NamedCompilation>> visit(ElementFilter el) {
		List<List<NamedCompilation>> retList = new ArrayList<List<NamedCompilation>>();
		List<NamedCompilation> filterList = new ArrayList<NamedCompilation>();
		retList.add(filterList);
		filterList.add(new NamedCompilation(constructFilterName(el), new CompilationStormRuleReteBoltHolder(constructFilterCompilation(el))));
		return retList;
	}

	private StormSPARQLFilterBolt constructFilterCompilation(ElementFilter el) {
		StormSPARQLFilterBolt bolt = new StormSPARQLFilterBolt(el);
		return bolt;
	}

	int countFilters = 0;

	private String constructFilterName(ElementFilter el) {
		final String filterName = String.format("SPARQLfilter_%d", countFilters++);
		return filterName;
	}

	private List<List<NamedCompilation>> visit(ElementSubQuery el) {
		Query q = el.getQuery(); // Deal with this shit!
		return null;
	}

	private List<List<NamedCompilation>> visit(ElementGroup el) {

		List<List<NamedCompilation>> boltNamesLists = new ArrayList<List<NamedCompilation>>();
		List<ElementFilter> filters = new ArrayList<ElementFilter>();
		for (Element sel : el.getElements()) {
			if (sel instanceof ElementFilter) {
				filters.add((ElementFilter) sel);
				continue; // Note filters are NOT added to the main boltNamesLists so they can be dealt with together, last
			}
			List<List<NamedCompilation>> newBoltNamesLists = visit(sel);

			if (boltNamesLists.size() == 0) {
				boltNamesLists = newBoltNamesLists;
			}
			else {
				List<List<NamedCompilation>> combinedBoltNamesLists = new ArrayList<List<NamedCompilation>>();
				for (List<NamedCompilation> other : newBoltNamesLists) {
					for (List<NamedCompilation> current : boltNamesLists) {
						// Merge the two lists and add to the combined
						List<NamedCompilation> merged = new ArrayList<NamedCompilation>();
						merged.addAll(current);
						merged.addAll(other);
						combinedBoltNamesLists.add(merged);
					}
				}
				boltNamesLists = combinedBoltNamesLists;
			}
			// now merge the bolt name lists with the ones we currently have and add them again
		}
		// for each set of bolts
		for (List<NamedCompilation> boltNames : boltNamesLists) {
			// Now do the join!
			join: while (boltNames.size() > 1) {
				int innerSelect = 1;
				NamedCompilation currentNameCompBoltPair = boltNames.get(0);
				if (currentNameCompBoltPair == null) {
					logger.error("Uninitiated child group found, can't compile tree!");
					throw new RuntimeException("An empty group!");
				}
				CompilationStormRuleReteBoltHolder currentBolt = currentNameCompBoltPair.getSecondObject();
				String[] currentVars = currentBolt.getVars();
				while (innerSelect < boltNames.size()) {
					NamedCompilation otherNameCompBoltPair = boltNames.get(innerSelect);
					CompilationStormRuleReteBoltHolder otherBolt = otherNameCompBoltPair.secondObject();
					String[] otherVars = otherBolt.getVars();
					for (String v : currentVars) {
						if (Arrays.asList(otherVars).contains(v)) {
							createJoin(0, innerSelect, boltNames);
							continue join;
						}
					}
					innerSelect++;
					// if we ever get here, inner select failed to
					// find a joining bolt, just pick the 1st one.
					if (innerSelect == boltNames.size()) {
						createJoin(0, 1, boltNames);
					}
				}
			}
		}

		compileFilters(boltNamesLists, filters);

		return boltNamesLists;
	}

	private void compileFilters(List<List<NamedCompilation>> boltNamesLists, List<ElementFilter> filters) {
		// for each grouping, add a new filter compilation to the end
		for (List<NamedCompilation> list : boltNamesLists) {
			NamedCompilation last = list.remove(0);// There must only be 1 in each list
			for (ElementFilter f : filters) {
				NamedCompilation filter = visit(f).get(0).get(0); // There must only be 1 filter bolt
				StormSPARQLFilterBolt fbolt = (StormSPARQLFilterBolt) filter.getSecondObject().getBolt();
				fbolt.registerSourceVariables(last.firstObject(), last.secondObject().getVars());
				filter.secondObject().setVars(last.secondObject().getVars()); // If the filter passes it throws along ALL variables of the previous (possibly more?)
				bolts.put(filter.firstObject(), fbolt); // so it is actually added
				last = filter; // connect all filters in a chain
			}
			list.add(last); // re-attach the last node, which might now be a filter
		}
	}

	private List<List<NamedCompilation>> visit(ElementUnion el) {
		List<List<NamedCompilation>> unionList = new ArrayList<List<NamedCompilation>>();
		for (Element elm : el.getElements()) {
			unionList.addAll(visit(elm));
		}
		return unionList;
	}

	private List<List<NamedCompilation>> visit(ElementOptional el) {
		List<List<NamedCompilation>> unionList = new ArrayList<List<NamedCompilation>>();
		unionList.add(new ArrayList<NamedCompilation>()); // Add an EMPTY list to the proceedings. i.e. allow option to not happen
		unionList.addAll(visit(el.getOptionalElement()));
		return unionList;
	}

	private List<List<NamedCompilation>> visit(ElementPathBlock el) {
		List<NamedCompilation> ret = new ArrayList<NamedCompilation>();
		for (TriplePath triple : el.getPattern().getList()) {
			if (triple.isTriple()) {
				TriplePattern tp = constructTriplePattern(triple.asTriple());
				String boltName = constructNewBoltName(tp);
				Rule rule = constructRule(tp);

				StormReteBolt filterBolt;
				if (bolts.containsKey(boltName)) {
					logger.debug(String.format("Filter bolt %s used from existing rule", boltName));
					filterBolt = bolts.get(boltName);
				} else {
					filterBolt = this.constructReteFilterBolt(rule);
					if (filterBolt == null) {
						String error = String.format("Filter bolt %s was null, not adding", boltName);
						logger.debug(error);
						throw new RuntimeException(error);
					}
					logger.debug(String.format("Filter bolt %s created from clause %s", boltName, el.toString()));
					bolts.put(boltName, filterBolt);
				}
				NamedCompilation toCompile = new NamedCompilation(boltName, new CompilationStormRuleReteBoltHolder((StormRuleReteBolt) filterBolt, rule));
				ret.add(toCompile);
			}
		}
		ArrayList<List<NamedCompilation>> retArr = new ArrayList<List<NamedCompilation>>();
		retArr.add(ret);
		return retArr;
	}

	@SuppressWarnings("unchecked")
	private void createJoin(
			int currentIndex, int otherIndex,
			List<NamedCompilation> boltNames
			)
	{
		NamedCompilation otherNameCompBoltPair = boltNames.remove(otherIndex);
		NamedCompilation currentNameCompBoltPair = boltNames.remove(currentIndex);
		List<ClauseEntry> template = new ArrayList<ClauseEntry>();
		CompilationStormRuleReteBoltHolder currentBolt = currentNameCompBoltPair.secondObject();
		CompilationStormRuleReteBoltHolder otherBolt = otherNameCompBoltPair.secondObject();
		template.addAll(Arrays.asList(currentBolt.getRule().getHead()));
		template.addAll(Arrays.asList(otherBolt.getRule().getHead()));

		// Create the string representing the variable-name-independently
		// ordered
		// output graph (this makes it repeatable irrespective of component
		// bolts).
		// This involves sorting the template, again independently of variable
		// names, which means the fields will be output in the same order
		// irrespective
		// of their names, thanks to being ordered by location in the template.
		String newJoinName = VariableIndependentReteRuleToStringUtils.clauseToString(template);
		StormReteBolt newJoin;
		if (bolts.containsKey(newJoinName)) {
			newJoin = bolts.get(newJoinName);
		} else {
			newJoin = constructReteJoinBolt(currentNameCompBoltPair, otherNameCompBoltPair, template);
		}

		bolts.put(newJoinName, newJoin);
		boltNames.add(new NamedCompilation(newJoinName, new CompilationStormRuleReteBoltHolder((StormRuleReteBolt) newJoin, new Rule(template, template))));
	}

	private Rule constructRule(TriplePattern tp) {
		List<ClauseEntry> template = new ArrayList<ClauseEntry>();
		template.add(tp);
		return new Rule(template, template);

	}

	private TriplePattern constructTriplePattern(Triple asTriple) {
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
	public void finishQuery(SPARQLReteTopologyBuilderContext context) {
		logger.debug("Connecting last node to the conflict set");
		// Now construct the terminal
		if (secondToLast == null) {
			throw new RuntimeException("Couldn't compile without prior!");
		}
		// We have a prior, we have a terminal bolt, we can go ahead and

		logger.debug("Connecting the final terminal to " + secondToLast);

		for (List<NamedCompilation> nodes : this.secondToLast) {
			for (NamedCompilation namedCompilation : nodes) {
				logger.debug("Connecting final terminal to: " + namedCompilation.secondObject());
				finalTerminalBuilder.shuffleGrouping(namedCompilation.firstObject());
				this.finalTerm.registerSourceVariables(namedCompilation.firstObject(), namedCompilation.secondObject().getVars());
			}
		}

		logger.debug("Connecting the filter and join instances to the source/final terminal instances");
		// Now add the nodes to the actual topology
		for (Entry<String, StormReteBolt> nameFilter : bolts.entrySet()) {
			String name = nameFilter.getKey();
			IRichBolt bolt = nameFilter.getValue();
			if (bolt instanceof StormReteFilterBolt)
				connectFilterBolt(context, name, (StormReteFilterBolt) bolt);
			else if (bolt instanceof StormReteJoinBolt)
				connectJoinBolt(context, name, (StormReteJoinBolt) bolt);
			else if (bolt instanceof StormSPARQLFilterBolt) {
				connectFilterBolt(context, name, (StormSPARQLFilterBolt) bolt);
			}
		}
	}

	private void connectFilterBolt(SPARQLReteTopologyBuilderContext context, String name, StormSPARQLFilterBolt bolt) {
		context.builder.setBolt(name, bolt).shuffleGrouping(bolt.getPrevious());
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
	public void connectFilterBolt(SPARQLReteTopologyBuilderContext context, String name, StormReteFilterBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt);
		// All the filter bolts are given triples from the source spout
		// and the final terminal
		midBuild.shuffleGrouping(context.source);
		//		if (this.finalTerminalBuilder != null)
		//			midBuild.shuffleGrouping(FINAL_TERMINAL);
	}

	// Bolt Construction

	/**
	 * @return the conflict set bolt usually describing what is done with
	 *         triples in the stream
	 */
	public StormSPARQLReteConflictSetBolt constructConflictSetBolt() {
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
	public StormReteBolt
			constructReteJoinBolt(NamedCompilation currentNameCompBoltPair,
					NamedCompilation otherNameCompBoltPair,
					List<ClauseEntry> template)
	{
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
