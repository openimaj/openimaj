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

import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.CsparqlUtils.CSparqlComponentHolder;

import backtype.storm.topology.TopologyBuilder;
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

	Logger logger = Logger.getLogger(SPARQLReteTopologyBuilder.class);

	/**
	 * The default name for the axiom spout
	 */
	public static final String AXIOM_SPOUT = "axiom_spout";

	private int unnamedRules = 0;

	/**
	 * Given a builder and a set of streams, add the source spout to the builder
	 * and return the name
	 * of the source spout
	 * 
	 * @param builder
	 * @param streams
	 * @return the name of the source spout
	 */
	public abstract String prepareSourceSpout(TopologyBuilder builder, Set<StreamInfo> streams);

	/**
	 * Initialise the topology. Might be used to create and hold on to nodes
	 * that are required by all other parts of the topology (e.g. the final node
	 * that actually outputs triples)
	 * 
	 * Context not-null values: {@link SPARQLReteTopologyBuilderContext #builder}
	 * , {@link SPARQLReteTopologyBuilderContext#source} and
	 * {@link SPARQLReteTopologyBuilderContext#query}
	 * 
	 * @param context
	 */
	public abstract void initTopology(SPARQLReteTopologyBuilderContext context);

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
		String source = prepareSourceSpout(builder, query.streams);
		SPARQLReteTopologyBuilderContext context = new SPARQLReteTopologyBuilderContext(builder, source, query);
		initTopology(context);

		System.out.println("Query binding variables: " + query.simpleQuery.getProjectVars());

		// now handle the query pattern
		compile();
		finishQuery();
	}

	/**
	 * Expected to compile the query held in the context
	 */
	public abstract void compile();

	/**
	 * This particular query is completed. Finish the query off.
	 * so
	 * 
	 */
	public abstract void finishQuery();

	protected String nextRuleName() {
		unnamedRules += 1;
		return String.format("unnamed_rule_%d", unnamedRules);
	}

}
