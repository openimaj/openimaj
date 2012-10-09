package org.openimaj.rdf.storm.tool;

import java.io.IOException;

import org.openimaj.rdf.storm.topology.RuleReteStormTopologyFactory;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;

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
	ReteStormOptions options;

	/**
	 * Prepare and launch the ReteStorm
	 * @param args
	 * @throws AlreadyAliveException
	 * @throws InvalidTopologyException
	 * @throws IOException
	 */
	public ReteStorm(String[] args) throws AlreadyAliveException, InvalidTopologyException, IOException {
		options = new ReteStormOptions(args);
		options.prepare();

		submitTopology();
	}

	private void submitTopology() throws AlreadyAliveException, InvalidTopologyException {
		Config conf = new Config();
		conf.setNumWorkers(20);
		conf.setMaxSpoutPending(5000);
		StormSubmitter.submitTopology(options.topologyName, conf, options.constructTopology(conf));
	}

	/**
	 * Runs the tool
	 * @param args
	 * @throws AlreadyAliveException
	 * @throws InvalidTopologyException
	 * @throws IOException
	 */
	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException, IOException {
		ReteStorm storm = new ReteStorm(args);
		storm.submitTopology();
	}
}
