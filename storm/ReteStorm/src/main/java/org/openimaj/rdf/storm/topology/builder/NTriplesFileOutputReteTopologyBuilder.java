package org.openimaj.rdf.storm.topology.builder;

import org.openimaj.rdf.storm.topology.bolt.FileConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;

/**
 * A {@link NTriplesReteTopologyBuilder} which constructs a
 * {@link FileConflictSetBolt}. This is mainly helpful for tests
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NTriplesFileOutputReteTopologyBuilder extends NTriplesReteTopologyBuilder {

	private String output;

	/**
	 * @param nTriplesURI the triple source
	 * @param output the triple output location
	 */
	public NTriplesFileOutputReteTopologyBuilder(String nTriplesURI, String output) {
		super(nTriplesURI);
		this.output = output;
	}

	@Override
	public ReteConflictSetBolt constructConflictSetBolt(ReteTopologyBuilderContext context) {
		return new FileConflictSetBolt(this.output);
	}
}
