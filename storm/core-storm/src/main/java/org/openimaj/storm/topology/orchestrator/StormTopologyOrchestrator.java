package org.openimaj.storm.topology.orchestrator;

import backtype.storm.generated.StormTopology;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface StormTopologyOrchestrator {
	/**
	 * @return constructs a storm topology
	 */
	public StormTopology buildTopology();
}
