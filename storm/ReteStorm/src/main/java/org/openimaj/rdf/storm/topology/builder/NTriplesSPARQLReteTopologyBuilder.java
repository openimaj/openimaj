package org.openimaj.rdf.storm.topology.builder;

import org.openimaj.rdf.storm.spout.NTriplesSpout;

import backtype.storm.topology.TopologyBuilder;

/**
 * The {@link NTriplesSPARQLReteTopologyBuilder} provides triples from a URI via
 * the {@link NTriplesSpout}.
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NTriplesSPARQLReteTopologyBuilder extends BaseSPARQLReteTopologyBuilder {
	/**
	 * The name of the spout outputting triples
	 */
	public static final String TRIPLE_SPOUT = "tripleSpout";
	private String nTriples;

	/**
	 * @param nTriplesURI the source of the nTriples
	 */
	public NTriplesSPARQLReteTopologyBuilder(String nTriplesURI) {
		this.nTriples = nTriplesURI;
	}

	@Override
	public String prepareSourceSpout(TopologyBuilder builder) {
		NTriplesSpout tripleSpout = new NTriplesSpout(nTriples);
		builder.setSpout(TRIPLE_SPOUT, tripleSpout, 1);
		return TRIPLE_SPOUT;
	}

}
