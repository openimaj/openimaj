package org.openimaj.rdf.storm.tool.topology;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.tool.ReteStormOptions;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;

/**
 * A {@link StormTopologyMode} uses a {@link StormSubmitter} to submit a
 * {@link StormTopology} constructed
 * using {@link ReteStormOptions#constructTopology}. The topology is submitted
 * as {@link ReteStormOptions#topologyName}
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormTopologyMode implements TopologyMode {
	public final static Logger logger = Logger.getLogger(StormTopologyMode.class);
	@Override
	public void submitTopology(ReteStormOptions options) throws Exception {
		Config conf = options.prepareConfig();
		logger.info("\nStarting topology: \n" + conf);
		StormSubmitter.submitTopology(options.topologyName, conf, options.constructTopology(conf));
	}

	@Override
	public void finish(ReteStormOptions options) throws Exception {
		// does nothing
	}

}
