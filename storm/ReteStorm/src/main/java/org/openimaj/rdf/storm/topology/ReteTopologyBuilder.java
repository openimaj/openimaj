package org.openimaj.rdf.storm.topology;

import java.util.List;

import backtype.storm.topology.TopologyBuilder;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * {@link ReteTopologyBuilder} instances can accept the filter parts, construct the joins and 
 * add the terminal nodes of a Rete topology.
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class ReteTopologyBuilder {
	
	/**
	 * A {@link ReteTopologyBuilderContext} holds variables needed by the various stages of
	 * a {@link ReteTopologyBuilder}
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class ReteTopologyBuilderContext{
		ReteTopologyBuilderContext() {}
		/**
		 * @param builder the Storm {@link TopologyBuilder}
		 * @param source the Storm tuples 
		 * @param rules
		 */
		public ReteTopologyBuilderContext(TopologyBuilder builder,String source, List<Rule> rules) {
			this.builder = builder;
			this.source = source;
			this.rules = rules;
		}
		/**
		 * the builder
		 */
		public TopologyBuilder builder;
		/**
		 * the initial source of tuples
		 */
		public String source;
		/**
		 * The rules in this topology
		 */
		public List<Rule> rules;
		/**
		 * The specific rule being worked on now, not null after {@link ReteTopologyBuilder#startRule(ReteTopologyBuilderContext)} and before {@link ReteTopologyBuilder#finishRule(ReteTopologyBuilderContext)} 
		 */
		public Rule rule;
		/**
		 * The specific clause in the body of the rule, not null specifically in the {@link ReteTopologyBuilder#addFilter(ReteTopologyBuilderContext)} call
		 */
		public Object filterClause;
		
	}
	private int unnamedRules = 0;
	/**
	 * Given a builder, a source spout and a set of rules, drive the 
	 * construction of the Rete topology
	 * 
	 * @param builder
	 * @param source
	 * @param rules
	 */
	public void compile(TopologyBuilder builder, String source, List<Rule> rules){
		ReteTopologyBuilderContext context = new ReteTopologyBuilderContext(builder,source,rules);
		initTopology(context);
		
		for (Rule rule : rules) {
			context.rule = rule;
			startRule(context);
			
			// Extract all the filter clauses
			for (int i = 0; i < rule.bodyLength(); i++) {
				Object clause = rule.getBodyElement(i);
				if (clause instanceof TriplePattern) {
					context.filterClause = clause;
					addFilter(context);
				}
			}

			// All the filters have been provided, create the joins!
			context.filterClause = null;
			createJoins(context);

			finishRule(context);
		}
	}
	



	/**
	 * Initialise the topology. Might be used to create and hold on to nodes that are required by 
	 * all other parts of the topology (e.g. the final node that actually outputs triples)
	 * 
	 * Context not-null values: {@link ReteTopologyBuilderContext#builder}, {@link ReteTopologyBuilderContext#source} and {@link ReteTopologyBuilderContext#rules} 
	 * 
	 * @param context
	 */
	public abstract void initTopology(ReteTopologyBuilderContext context);
	/**
	 * Start a new rule. The {@link ReteTopologyBuilderContext#rule} becomes not null
	 * @param context
	 */
	public abstract void startRule(ReteTopologyBuilderContext context) ;
	/**
	 * Add a new filter clause. The {@link ReteTopologyBuilderContext#filterClause} becomes not null. 
	 * This stage may result in the construction and addition of {@link ReteFilterBolt} instances
	 * 
	 * So far the {@link ReteTopologyBuilderContext#filterClause} can only be {@link TriplePattern} instance
	 * @param context
	 */
	public abstract void addFilter(ReteTopologyBuilderContext context); 
	/**
	 * All the filters have been provided. Organise the various filters into joins. 
	 * The {@link ReteTopologyBuilderContext#filterClause} becomes null 
	 * This stage may result in the construction and addition of {@link ReteJoinBolt} instances
	 * @param context
	 */
	public abstract void createJoins(ReteTopologyBuilderContext context);
	/**
	 * This particular rule is completed. Finish the rule off, possible with a {@link ReteTerminalBolt} instance.
	 * The various bolts may have already added themselves to the topology, if not this is their last chance to do so
	 * @param context
	 */
	public abstract void finishRule(ReteTopologyBuilderContext context);
	
	protected String nextRuleName() {
		unnamedRules  += 1;
		return String.format("unnamed_rule_%d", unnamedRules);
	}
	
	
	
}
