package org.openimaj.rdf.storm.topology.builder;

import java.util.ArrayList;

import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteJoinBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteTerminalBolt;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder.ReteTopologyBuilderContext;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;

/**
 * The {@link SimpleReteTopologyBuilder} provides the basic types of {@link ReteFilterBolt}, {@link ReteJoinBolt}, {@link ReteTerminalBolt} and {@link ReteConflictSetBolt}
 * instances. Specifically the triples from the spout are only emitted to the Rete network itself and all {@link ReteFilterBolt} instances listen to the {@link ReteConflictSetBolt} 
 * for processing of derived triples
 *  
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SimpleReteTopologyBuilder extends BaseReteTopologyBuilder{

	@Override
	public ReteConflictSetBolt constructConflictSetBolt(ReteTopologyBuilderContext context) {
		return new ReteConflictSetBolt();
	}

	@Override
	public ReteTerminalBolt constructTerminalBolt(ReteTopologyBuilderContext context) {
		return new ReteTerminalBolt(context.rule);
	}

	@Override
	public ReteFilterBolt constructReteFilterBolt(ReteTopologyBuilderContext context, int filterCount) {
		return new ReteFilterBolt(context.rule, filterCount);
	}

	@Override
	public ReteJoinBolt constructReteJoinBolt(String left, String right,ArrayList<Byte> matchIndices) {
		return new ReteJoinBolt(left, right, matchIndices);
	}
	
	@Override
	public void connectFilterBolt(ReteTopologyBuilderContext context,String name, IRichBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt);
		// All the filter bolts are given triples from the source spout
		// and the final terminal
		midBuild.shuffleGrouping(context.source);
	}

}
