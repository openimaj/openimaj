package org.openimaj.rdf.storm.tool.topology;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;
import org.openimaj.rdf.storm.tool.ReteStormOptions;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.Utils;

/**
 * The local topology for testing. Allows the specification of sleep time etc.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LocalTopologyMode implements TopologyMode {
	private static final int DEFAULT_SLEEP_TIME = 10000;
	private final static Logger logger = Logger.getLogger(LocalTopologyMode.class);
	/**
	 * Time to wait in local mode
	 */
	@Option(
			name = "--sleep-time",
			aliases = "-st",
			required = false,
			usage = "How long the local topology should wait while processing happens, -1 waits forever",
			metaVar = "STRING")
	public long sleepTime = DEFAULT_SLEEP_TIME;
	private LocalCluster cluster;

	@Override
	public void submitTopology(ReteStormOptions options) throws Exception {
		logger.debug("Configuring topology");
		Config conf = options.prepareConfig();
		logger.debug("Instantiating cluster");
		this.cluster = new LocalCluster();
		logger.debug("Constructing topology");
		StormTopology topology = options.constructTopology();
		logger.debug("Submitting topology");
		cluster.submitTopology(options.topologyName, conf, topology);
	}

	@Override
	public void finish(ReteStormOptions options) throws Exception {
		try {
			if (sleepTime < 0) {
				logger.debug("Waiting forever");
				while (true) {
					Utils.sleep(DEFAULT_SLEEP_TIME);
				}
			} else {
				logger.debug("Waiting " + sleepTime + " milliseconds");
				Utils.sleep(sleepTime);

			}
		} finally {
			logger.debug("Killing topology");
			cluster.killTopology(options.topologyName);
			logger.debug("Shutting down cluster");
			cluster.shutdown();
			options.mmOp.close();
		}
	}

}
