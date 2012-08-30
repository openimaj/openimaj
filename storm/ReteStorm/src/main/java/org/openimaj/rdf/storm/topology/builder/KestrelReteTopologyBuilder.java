package org.openimaj.rdf.storm.topology.builder;

import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.writing.NTripleWritingScheme;
import org.openimaj.rdf.storm.topology.bolt.KestrelReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;

import backtype.storm.spout.KestrelThriftSpout;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.TopologyBuilder;


/**
 * The {@link KestrelReteTopologyBuilder} is mostly the same as a {@link NTriplesReteTopologyBuilder}
 * but allows for integration and use of kestrel queues for output of triples fed to the network as well
 * as infered triples.
 *
 * This culminates in the replacement of the {@link ReteConflictSetBolt} with the {@link KestrelReteConflictSetBolt}
 * and a slight variation to the default connection of {@link ReteFilterBolt} instances
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelReteTopologyBuilder extends BaseReteTopologyBuilder {
	private KestrelServerSpec spec;
	private String outputQueue;
	private String inputQueue;

	/**
	 * @param spec the kestrel server to connect to
	 * @param inputQueue the queue which the rete network will read again
	 * @param outputQueue the output queue
	 */
	public KestrelReteTopologyBuilder(KestrelServerSpec spec, String inputQueue, String outputQueue) {
		this.spec = spec;
		this.outputQueue = outputQueue;
		this.inputQueue = inputQueue;
	}

	@Override
	public ReteConflictSetBolt constructConflictSetBolt(ReteTopologyBuilderContext context) {
		return new KestrelReteConflictSetBolt(this.spec, this.inputQueue,this.outputQueue);
	}

	@Override
	public void connectFilterBolt(ReteTopologyBuilderContext context,String name, IRichBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt);
		// Just add it to the source, it doesn't need to be attached to the conflict set instance directly
		midBuild.shuffleGrouping(context.source);
	}

	@Override
	public String prepareSourceSpout(TopologyBuilder builder) {
		KestrelThriftSpout tripleSpout = new KestrelThriftSpout(spec.host, spec.port, inputQueue, new NTripleWritingScheme());
		builder.setSpout(NTriplesReteTopologyBuilder.TRIPLE_SPOUT, tripleSpout, 1);
		return NTriplesReteTopologyBuilder.TRIPLE_SPOUT;
	}
}
