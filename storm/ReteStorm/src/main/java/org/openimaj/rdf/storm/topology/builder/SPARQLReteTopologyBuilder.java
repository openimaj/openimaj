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

import java.util.Set;

import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteTerminalBolt;
import org.openimaj.rdf.storm.utils.CsparqlUtils.CSparqlComponentHolder;

import backtype.storm.topology.TopologyBuilder;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.larkc.csparql.parser.StreamInfo;

/**
 * {@link SPARQLReteTopologyBuilder} instances can accept the filter parts,
 * construct the joins and add the terminal nodes of a Rete topology using a
 * SPARQL query
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class SPARQLReteTopologyBuilder {

	/**
	 * The default name for the axiom spout
	 */
	public static final String AXIOM_SPOUT = "axiom_spout";

	/**
	 * A {@link SPARQLReteTopologyBuilderContext} holds variables needed by the
	 * various stages of a {@link SPARQLReteTopologyBuilder}
	 *
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class SPARQLReteTopologyBuilderContext {

		/**
		 * The query being compiled
		 */
		public CSparqlComponentHolder query;

		/**
		 * the builder
		 */
		public TopologyBuilder builder;
		/**
		 * the initial source of tuples
		 */
		public String source;

		/**
		 * the current element
		 */
		public ElementPathBlock filterClause;


		SPARQLReteTopologyBuilderContext() {
		}

		/**
		 * @param builder
		 *            the Storm {@link TopologyBuilder}
		 * @param source
		 *            the Storm tuples
		 * @param query
		 *            the entire query being worked on
		 */
		public SPARQLReteTopologyBuilderContext(TopologyBuilder builder, String source, CSparqlComponentHolder query) {
			this.query = query;
			this.builder = builder;
			this.source = source;
		}



	}

	private int unnamedRules = 0;

	/**
	 * Given a builder and a set of rules, drive the construction of the Rete
	 * topology
	 *
	 * @param builder
	 *            the builder to add bolts/spouts to
	 * @param query
	 *            the query to compile
	 */
	public void compile(TopologyBuilder builder, CSparqlComponentHolder query) {
		String source = prepareSourceSpout(builder,query.streams);
		SPARQLReteTopologyBuilderContext context = new SPARQLReteTopologyBuilderContext(builder, source, query);
		initTopology(context);

		// Start the first, implied, outer group of the where clause
		startGroup(context);
		QueryExecutionFactory.create(context.query.simpleQuery);
		System.out.println("Query binding variables: " + query.simpleQuery.getProjectVars());
		// now handle the query pattern
		handleElement(query.simpleQuery.getQueryPattern(),context);
		// now close (finish, pop, whatever...) the implied outer group.
		endGroup(context);

		context.filterClause = null;
		createJoins(context);
		finishQuery(context);
	}

	private void handleElement(Element element, SPARQLReteTopologyBuilderContext context) {
		if(element instanceof ElementPathBlock){
			handleElement((ElementPathBlock)element, context);
		}else if (element instanceof ElementGroup){
			handleElement((ElementGroup)element, context);
		}

	}

	private void handleElement(ElementPathBlock path, SPARQLReteTopologyBuilderContext context) {
		context.filterClause = path;
		addFilter(context);
	}

	private void handleElement(ElementGroup group, SPARQLReteTopologyBuilderContext context) {
		startGroup(context); // start a new group.
		for (Element elm : group.getElements()) {
			handleElement(elm, context);
		}
		endGroup(context);

	}

	/**
	 * Given a builder and a set of streams, add the source spout to the builder and return the name
	 * of the source spout
	 *
	 * @param builder
	 * @param streams
	 * @return the name of the source spout
	 */
	public abstract String prepareSourceSpout(TopologyBuilder builder,Set<StreamInfo> streams);

	/**
	 * Initialise the topology. Might be used to create and hold on to nodes
	 * that are required by all other parts of the topology (e.g. the final node
	 * that actually outputs triples)
	 *
	 * Context not-null values: {@link SPARQLReteTopologyBuilderContext #builder},
	 * {@link SPARQLReteTopologyBuilderContext#source} and
	 * {@link SPARQLReteTopologyBuilderContext#query}
	 *
	 * @param context
	 */
	public abstract void initTopology(SPARQLReteTopologyBuilderContext context);

	/**
	 * Start a new group.
	 *
	 * @param context
	 */
	public abstract void startGroup(SPARQLReteTopologyBuilderContext context);

	/**
	 * End the current group.
	 *
	 * @param context
	 */
	public abstract void endGroup(SPARQLReteTopologyBuilderContext context);

	/**
	 * Add a new filter clause. The
	 * {@link SPARQLReteTopologyBuilderContext#filterClause} becomes not null. This
	 * stage may result in the construction and addition of
	 * {@link ReteFilterBolt} instances
	 *
	 * So far the {@link SPARQLReteTopologyBuilderContext#filterClause} can only be
	 * {@link TriplePattern} instance
	 *
	 * @param context
	 */
	public abstract void addFilter(SPARQLReteTopologyBuilderContext context);

	/**
	 * All the filters have been provided. Organise the various filters into
	 * joins. The {@link SPARQLReteTopologyBuilderContext#filterClause} becomes null
	 * This stage may result in the construction and addition of
	 * {@link ReteJoinBolt} instances
	 *
	 * @param context
	 */
	public abstract void createJoins(SPARQLReteTopologyBuilderContext context);

	/**
	 * This particular rule is completed. Finish the rule off, possible with a
	 * {@link ReteTerminalBolt} instance. The various bolts may have already
	 * added themselves to the topology, if not this is their last chance to do
	 * so
	 *
	 * @param context
	 */
	public abstract void finishQuery(SPARQLReteTopologyBuilderContext context);

	protected String nextRuleName() {
		unnamedRules += 1;
		return String.format("unnamed_rule_%d", unnamedRules);
	}

}
