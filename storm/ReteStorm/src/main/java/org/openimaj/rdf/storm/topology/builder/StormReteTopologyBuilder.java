package org.openimaj.rdf.storm.topology.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteTerminalBolt;
import org.openimaj.rdf.storm.utils.Count;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import scala.actors.threadpool.Arrays;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;

/**
 * The simple topology builders make no attempt to optimise the joins. This base
 * interface takes care of recording filters, joins etc. and leaves the job of
 * actually adding the bolts to the topology as well as the construction of the
 * {@link ReteConflictSetBolt} instance down to its children.
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk), David Monks (dm11g08@ecs.soton.ac.uk)
 *
 */
public abstract class StormReteTopologyBuilder extends ReteTopologyBuilder {
	private static Logger logger = Logger
			.getLogger(StormReteTopologyBuilder.class);
	/**
	 * the name of the final bolt
	 */
	public static final String FINAL_TERMINAL = "final_term";

	private BoltDeclarer finalTerminalBuilder;
	private ReteTerminalBolt term;
	private Map<String, Map<String,StormReteBolt>> rules;
	private Map<String,StormReteBolt> rule;
	private Map<String, StormReteBolt> bolts;
	private Map<String, List<String>> priorBolts;
	private String ruleName;
	private String prior;
	
	@Override
	public void initTopology(ReteTopologyBuilderContext context) {
		this.rules = new HashMap<String, Map<String, StormReteBolt>>();
		this.bolts = new HashMap<String, StormReteBolt>();
		this.priorBolts = new HashMap<String, List<String>>();
		ReteConflictSetBolt finalTerm = constructConflictSetBolt(context);
		if (finalTerm != null)
		{
			this.finalTerminalBuilder = context.builder.setBolt(FINAL_TERMINAL, finalTerm,1); // There is explicity 1 and only 1 Conflict set
			this.finalTerminalBuilder.allGrouping(context.axiomSpout);
		}
	}
	
	private static List<ClauseEntry> sortClause(List<ClauseEntry> template) {
		Collections.sort(template,new Comparator<ClauseEntry>(){
			@Override
			public int compare(ClauseEntry o1,
					ClauseEntry o2) {
				clauseEntryToString(o1).compareTo(clauseEntryToString(o2));
				return 0;
			}
		});
		return template;
	}

	@SuppressWarnings("unchecked")
	protected static String clauseEntryToString(ClauseEntry ce, List<String> varNames, int[] matchIndices, Count count){
		if (ce instanceof TriplePattern) {
			TriplePattern filter = (TriplePattern) ce;
			String subject = filter.getSubject().isVariable()
					? varNames.contains(filter.getSubject().getName())
						? matchIndices[varNames.indexOf(filter.getSubject().getName())] == -1
							? "?"+(matchIndices[varNames.indexOf(filter.getSubject().getName())] = count.inc())
							: "?"+matchIndices[varNames.indexOf(filter.getSubject().getName())]
						: "'VAR'"
					: filter.getSubject().isURI()
						? filter.getSubject().getURI()
						: filter.getSubject().isLiteral()
							? filter.getSubject().getLiteralValue().toString()
							: filter.getSubject().getBlankNodeLabel();
			String predicate = filter.getPredicate().isVariable()
					? varNames.contains(filter.getPredicate().getName())
						? matchIndices[varNames.indexOf(filter.getPredicate().getName())] == -1
							? "?"+(matchIndices[varNames.indexOf(filter.getPredicate().getName())] = count.inc())
							: "?"+matchIndices[varNames.indexOf(filter.getPredicate().getName())]
						: "'VAR'"
					: filter.getPredicate().isURI()
						? filter.getPredicate().getURI()
						: filter.getPredicate().isLiteral()
							? filter.getPredicate().getLiteralValue().toString()
							: filter.getPredicate().getBlankNodeLabel();
			String object;
			if (filter.getObject().isLiteral() && filter.getObject().getLiteralValue() instanceof Functor) {
				object = clauseEntryToString((Functor)filter.getObject().getLiteralValue(),
											 varNames, matchIndices, count);
			} else {
				object = filter.getObject().isVariable()
						? varNames.contains(filter.getObject().getName())
							? matchIndices[varNames.indexOf(filter.getObject().getName())] == -1
								? "?"+(matchIndices[varNames.indexOf(filter.getObject().getName())] = count.inc())
								: "?"+matchIndices[varNames.indexOf(filter.getObject().getName())]
							: "'VAR'"
						: filter.getObject().isURI()
							? filter.getObject().getURI()
							: filter.getObject().isLiteral()
								? filter.getObject().getLiteralValue().toString()
								: filter.getObject().getBlankNodeLabel();
			}
			return String.format("(%s %s %s)", subject, predicate, object);
		} else if (ce instanceof Functor) {
			Functor f = (Functor) ce;
			String functor = f.getName()+"(";
			for (Node n : f.getArgs())
				functor += (n.isVariable()
						? varNames.contains(n.getName())
							? matchIndices[varNames.indexOf(n.getName())] == -1
								? "?"+(matchIndices[varNames.indexOf(n.getName())] = count.inc())
								: "?"+matchIndices[varNames.indexOf(n.getName())]
							: "'VAR'"
						: n.isURI()
							? n.getURI()
							: n.isLiteral()
								? n.getLiteralValue().toString()
								: n.getBlankNodeLabel())
						+ " ";
			return functor.substring(0, functor.length() - 1) + ")";
		} else if (ce instanceof Rule) {
			Rule r = (Rule) ce;
			
			List<String> v = Arrays.asList(StormReteBolt.extractFields(Arrays.asList(r.getBody())));
			int[] m = new int[v.size()];
			for (int i = 0; i < m.length; i++ )
				m[i] = -1;
			Count c = new Count(0);
			
			String rule = "[ ";
			if (r.getName() != null)
				rule += r.getName() + " : ";
			return rule
					+ clauseToStringAllVars(Arrays.asList(r.getBody()),v,m,c) + "-> "
					+ clauseToStringAllVars(Arrays.asList(r.getHead()),v,m,c) + "]";
		}
		throw new ClassCastException("The proffered ClauseEntry is not one of the standard implementations supplied by Jena (TriplePattern, Functor or Rule)");
	}
	
	protected static String clauseEntryToString(ClauseEntry ce){
		return clauseEntryToString(ce, new ArrayList<String>(), new int[0], new Count(0));
	}
	
	protected static String clauseToString(List<ClauseEntry> template, List<String> varNames, int[] matchIndices, Count count) {
		template = sortClause(template);
		
		StringBuilder clause = new StringBuilder();
		for (ClauseEntry ce : template)
			clause.append(clauseEntryToString(ce,varNames,matchIndices,count)+" ");
		
		return clause.toString();
	}
	
	protected static String clauseToString(List<ClauseEntry> template){
		@SuppressWarnings("unchecked")
		List<String> varNames = Arrays.asList(StormReteBolt.extractJoinFields(template));
		int[] matchIndices = new int[varNames.size()];
		for (int i = 0; i < matchIndices.length; i++ )
			matchIndices[i] = -1;
		Count count = new Count(0);
		
		return clauseToString(template, varNames, matchIndices, count);
	}
	
	protected static String clauseToStringAllVars(List<ClauseEntry> template, List<String> varNames, int[] matchIndices, Count count) {
		template = sortClause(template);
		
		StringBuilder clause = new StringBuilder();
		for (ClauseEntry ce : template)
			clause.append(clauseEntryToString(ce,varNames,matchIndices,count)+" ");
		
		return clause.toString();
	}
	
	protected static String clauseToStringAllVars(List<ClauseEntry> template){
		@SuppressWarnings("unchecked")
		List<String> varNames = Arrays.asList(StormReteBolt.extractFields(template));
		int[] matchIndices = new int[varNames.size()];
		for (int i = 0; i < matchIndices.length; i++ )
			matchIndices[i] = -1;
		Count count = new Count(0);
		
		return clauseToStringAllVars(template, varNames, matchIndices, count);
	}	

	@Override
	public void startRule(ReteTopologyBuilderContext context) {
		// The rule name, bolts take the form of ruleName_(BODY|HEAD)_count
		this.ruleName = context.rule.getName();
		if (this.ruleName == null)
			this.ruleName = nextRuleName();
		// Sort rule clauses and standardise names
		context.rule = Rule.parseRule(clauseEntryToString(context.rule));
		// prepare the map of bolt names to bolts for the rule being started.
		rule = new HashMap<String, StormReteBolt>();
		rules.put(ruleName, rule);
		// This is the terminal bolt (where the head is fired)
		this.term = null;

		logger.debug(String.format("Compiling rule: %s", ruleName));
	}

	@Override
	public void addFilter(ReteTopologyBuilderContext context) {
		String boltName = clauseEntryToString(context.filterClause);
		
		StormReteBolt filterBolt;
		if (bolts.containsKey(boltName)){
			logger.debug(String.format("Filter bolt %s used from existing rule", boltName));
			filterBolt = bolts.get(boltName);
		} else {
			filterBolt = this.constructReteFilterBolt((TriplePattern) context.filterClause);
			if (filterBolt == null) {
				logger.debug(String.format("Filter bolt %s was null, not adding", boltName));
				return;
			}
			logger.debug(String.format("Filter bolt %s created from clause %s", boltName, ((TriplePattern)context.filterClause).toString()));
			bolts.put(boltName, filterBolt);
			priorBolts.put(boltName, new ArrayList<String>());
		}
		rule.put(boltName, filterBolt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void createJoins(ReteTopologyBuilderContext context) {
		// Now construct the beta networks (this could be optimised)
		Set<String> boltNames = new HashSet<String>();
		boltNames.addAll(rule.keySet());
		while (boltNames.size() > 1){
			Iterator<String> names = boltNames.iterator();
			join:
				while (names.hasNext()){
					String currentBoltName = names.next();
					StormReteBolt currentBolt = rule.get(currentBoltName);
					String[] currentVars = currentBolt.getVars();
					currentBolt.getRule();
					// Remove the bolt from the rule, so that it does not attempt to match against itself.
					// If no joins are made then it will be added to the nextLevel map.
					names.remove();
					
					while (boltNames.size() > 0){
						names = boltNames.iterator();
						while (names.hasNext()){
							String otherBoltName = names.next();
							StormReteBolt otherBolt = rule.get(otherBoltName);
							String[] otherVars = otherBolt.getVars();
							for (String v : currentVars){
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
									String newJoinName = clauseToString(template);
									StormReteBolt newJoin;
									if (bolts.containsKey(newJoinName)){
										newJoin = bolts.get(newJoinName);
									}else{
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
	public void finishRule(ReteTopologyBuilderContext context) {
		logger.debug("Compiling the terminal node instance");
		// Now construct the terminal
		if (prior != null) {
			// term = new ReteTerminalBolt(context.rule);
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
			String terminalName = String.format("%s", context.rule.getHead().toString());
			context.builder.setBolt(terminalName, term).shuffleGrouping(prior);

			logger.debug("Connecting the final terminal to " + terminalName);
			finalTerminalBuilder.shuffleGrouping(terminalName);
		}

		logger.debug("Connecting the filter and join instances to the source/final terminal instances");
		// Now add the nodes to the actual topology
		for (Entry<String, StormReteBolt> nameFilter : rule.entrySet()) {
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
	public void connectJoinBolt(ReteTopologyBuilderContext context, String name, StormReteJoinBolt bolt) {
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
	 * @param filter 
	 * @return the {@link StormReteFilterBolt} usually the filter between the source
	 *         and a join or a terminal. If null the filter isn't added
	 */
	public StormReteFilterBolt constructReteFilterBolt(TriplePattern filter) {
		List<ClauseEntry> template = new ArrayList<ClauseEntry>();
		template.add(filter);
		return new StormReteFilterBolt(new Rule(template,template));
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
	public StormReteJoinBolt constructReteJoinBolt(String left, String right, List<ClauseEntry> template) {
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
		for (int n = 0; n < newVars.length; n++){
			templateLeft[n] = Arrays.asList(currentVars).indexOf(newVars[n]);
			templateRight[n] = Arrays.asList(otherVars).indexOf(newVars[n]);
		}
		return new StormReteJoinBolt(left, matchLeft, templateLeft, right, matchRight, templateRight, new Rule(template, template));
	}

}
