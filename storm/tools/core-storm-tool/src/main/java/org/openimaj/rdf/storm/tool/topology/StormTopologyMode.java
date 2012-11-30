package org.openimaj.rdf.storm.tool.topology;

import org.apache.log4j.Logger;
import org.openimaj.storm.tool.StormToolOptions;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;

/**
 * A {@link StormTopologyMode} uses a {@link StormSubmitter} to submit a
 * {@link StormTopology} constructed
 * using {@link StormToolOptions#constructTopology}. The topology is submitted
 * as {@link StormToolOptions#topologyName}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StormTopologyMode implements TopologyMode {
	private final static Logger logger = Logger.getLogger(StormTopologyMode.class);

	@Override
	public void submitTopology(StormToolOptions options) throws Exception {
		Config conf = options.prepareConfig();
		logger.info("\nStarting topology: \n" + conf);
		StormSubmitter.submitTopology(options.topologyName(), conf, options.constructTopology());
	}

	@Override
	public void finish(StormToolOptions options) throws Exception {
		// does nothing
	}

}
