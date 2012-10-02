package org.openimaj.rdf.storm.topology.builder;

import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;

import backtype.storm.topology.TopologyBuilder;


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

	@Override
	public String prepareSourceSpout(TopologyBuilder builder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initTopology(SPARQLReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startRule(SPARQLReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFilter(SPARQLReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createJoins(SPARQLReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finishRule(SPARQLReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub

	}


}
