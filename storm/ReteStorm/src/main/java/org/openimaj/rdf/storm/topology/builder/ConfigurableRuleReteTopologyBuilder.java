package org.openimaj.rdf.storm.topology.builder;

import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;


/**
 * The {@link ConfigurableRuleReteTopologyBuilder} allows for use of arbitrary types of spouts of triples
 * and terminals
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ConfigurableRuleReteTopologyBuilder extends BaseReteTopologyBuilder {

	private IRichSpout tripleSpout;
	private ReteConflictSetBolt terminal;

	/**
	 * @param spec the kestrel server to connect to
	 * @param inputQueue the queue which the rete network will read again
	 * @param outputQueue the output queue
	 */
	public ConfigurableRuleReteTopologyBuilder(IRichSpout tripleSpout, ReteConflictSetBolt terminal) {
		this.tripleSpout = tripleSpout;
		this.terminal = terminal;
	}

	@Override
	public ReteConflictSetBolt constructConflictSetBolt(ReteTopologyBuilderContext context) {
		return terminal;
	}

	@Override
	public void connectFilterBolt(ReteTopologyBuilderContext context,String name, IRichBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt);
		// Add the filter to the source
		midBuild.shuffleGrouping(context.source);
		// Optionally attach the filter to the final terminal
//		if(connect)
	}

	@Override
	public String prepareSourceSpout(TopologyBuilder builder) {
		builder.setSpout(NTriplesReteTopologyBuilder.TRIPLE_SPOUT, tripleSpout);
		return NTriplesReteTopologyBuilder.TRIPLE_SPOUT;
	}
}
