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

import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.TopologyBuilder;

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

	/**
	 * the name of the final bolt
	 */
	public static final String FINAL_TERMINAL = "final_term";
	private BoltDeclarer finalTerminalBuilder;

	@Override
	public String prepareSourceSpout(TopologyBuilder builder, Set<StreamInfo> streams) {
		return null;
	}

	@Override
	public void initTopology(SPARQLReteTopologyBuilderContext context) {
		ReteConflictSetBolt finalTerm = constructConflictSetBolt(context);
		if (finalTerm != null)
		{
			this.finalTerminalBuilder = context.builder.setBolt(FINAL_TERMINAL, finalTerm,1); // There is explicity 1 and only 1 Conflict set
		}
	}

	private ReteConflictSetBolt constructConflictSetBolt(SPARQLReteTopologyBuilderContext context) {
		return new ReteConflictSetBolt();
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
		for (TriplePath tp : context.filterClause.getPattern().getList()) {
			if(tp.isTriple()){
				// it is a simple triple, construct a normal filter
			}
		}
	}

	@Override
	public void createJoins(SPARQLReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finishQuery(SPARQLReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub

	}


}
