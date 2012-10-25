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
package org.openimaj.rdf.storm.topology.builder;

import java.util.List;

import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteTerminalBolt;
import org.openimaj.rdf.storm.topology.spout.ReteAxiomSpout;

import backtype.storm.topology.TopologyBuilder;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * {@link ReteTopologyBuilder} instances can accept the filter parts, construct
 * the joins and add the terminal nodes of a Rete topology.
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class ReteTopologyBuilder {

	/**
	 * The default name for the axiom spout
	 */
	public static final String AXIOM_SPOUT = "axiom_spout";

	/**
	 * A {@link ReteTopologyBuilderContext} holds variables needed by the
	 * various stages of a {@link ReteTopologyBuilder}
	 *
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class ReteTopologyBuilderContext {
		ReteTopologyBuilderContext() {
		}

		/**
		 * @param builder
		 *            the Storm {@link TopologyBuilder}
		 * @param source
		 *            the Storm tuples
		 * @param rules
		 */
		public ReteTopologyBuilderContext(TopologyBuilder builder, String source, List<Rule> rules) {
			this.builder = builder;
			this.source = source;
			this.rules = rules;
			this.axiomSpout = AXIOM_SPOUT;
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
		 * The specific rule being worked on now, not null after
		 * {@link ReteTopologyBuilder#startRule(ReteTopologyBuilderContext)} and
		 * before
		 * {@link ReteTopologyBuilder#finishRule(ReteTopologyBuilderContext)}
		 */
		public Rule rule;
		/**
		 * The specific clause in the body of the rule, not null specifically in
		 * the {@link ReteTopologyBuilder#addFilter(ReteTopologyBuilderContext)}
		 * call
		 */
		public ClauseEntry filterClause;
		/**
		 * The name of the axiom spout
		 */
		public String axiomSpout;

	}

	private int unnamedRules = 0;

	/**
	 * Given a builder and a set of rules, drive the construction of the Rete
	 * topology
	 *
	 * @param builder
	 * @param rules
	 */
	public void compile(TopologyBuilder builder, List<Rule> rules) {
		String source = prepareSourceSpout(builder);
		ReteTopologyBuilderContext context = new ReteTopologyBuilderContext(builder, source, rules);
		ReteAxiomSpout axiomSpout = new ReteAxiomSpout();
		context.builder.setSpout(context.axiomSpout, axiomSpout, 1);
		initTopology(context);

		for (Rule rule : rules) {
			if (rule.isAxiom()) {
				axiomSpout.addAxiom(rule);
			}
			else
			{
				context.rule = rule;
				startRule(context);

				// Extract all the filter clauses
				for (int i = 0; i < rule.bodyLength(); i++) {
					ClauseEntry clause = rule.getBodyElement(i);
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
	}

	/**
	 * Given a builder, add the source spout to the builder and return the name
	 * of the source spout
	 *
	 * @param builder
	 * @return the name of the source spout
	 */
	public abstract String prepareSourceSpout(TopologyBuilder builder);

	/**
	 * Initialise the topology. Might be used to create and hold on to nodes
	 * that are required by all other parts of the topology (e.g. the final node
	 * that actually outputs triples)
	 *
	 * Context not-null values: {@link ReteTopologyBuilderContext#builder},
	 * {@link ReteTopologyBuilderContext#source} and
	 * {@link ReteTopologyBuilderContext#rules}
	 *
	 * @param context
	 */
	public abstract void initTopology(ReteTopologyBuilderContext context);

	/**
	 * Start a new rule. The {@link ReteTopologyBuilderContext#rule} becomes not
	 * null
	 *
	 * @param context
	 */
	public abstract void startRule(ReteTopologyBuilderContext context);

	/**
	 * Add a new filter clause. The
	 * {@link ReteTopologyBuilderContext#filterClause} becomes not null. This
	 * stage may result in the construction and addition of
	 * {@link ReteFilterBolt} instances
	 *
	 * So far the {@link ReteTopologyBuilderContext#filterClause} can only be
	 * {@link TriplePattern} instance
	 *
	 * @param context
	 */
	public abstract void addFilter(ReteTopologyBuilderContext context);

	/**
	 * All the filters have been provided. Organise the various filters into
	 * joins. The {@link ReteTopologyBuilderContext#filterClause} becomes null
	 * This stage may result in the construction and addition of
	 * {@link ReteJoinBolt} instances
	 *
	 * @param context
	 */
	public abstract void createJoins(ReteTopologyBuilderContext context);

	/**
	 * This particular rule is completed. Finish the rule off, possible with a
	 * {@link ReteTerminalBolt} instance. The various bolts may have already
	 * added themselves to the topology, if not this is their last chance to do
	 * so
	 *
	 * @param context
	 */
	public abstract void finishRule(ReteTopologyBuilderContext context);

	protected String nextRuleName() {
		unnamedRules += 1;
		return String.format("unnamed_rule_%d", unnamedRules);
	}

}
