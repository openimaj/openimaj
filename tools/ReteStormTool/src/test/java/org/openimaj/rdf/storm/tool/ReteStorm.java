package org.openimaj.rdf.storm.tool;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.topology.RuleReteStormTopologyFactory;

/**
 * The rete storm tool wraps the functionality of {@link RuleReteStormTopologyFactory} and
 * allows the construction and deployment of Rete topologies based on various rule languages
 *
 * Currently only the Jena rules language is supported.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteStorm {
	Logger logger = Logger.getLogger(ReteStorm.class);
	ReteStormOptions options;

	/**
	 * Prepare and launch the ReteStorm
	 * @param args
	 * @throws Exception
	 */
	public ReteStorm(String[] args) throws Exception {
		options = new ReteStormOptions(args);
		logger.debug("Parsing arguments");
		options.prepare();
	}

	private void submitTopology() throws Exception {
		logger.debug("Submitting topology");
		this.options.tmOp.submitTopology(this.options);
	}

	/**
	 * Runs the tool
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ReteStorm storm = new ReteStorm(args);
		storm.submitTopology();
	}
}
