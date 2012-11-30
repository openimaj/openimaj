package org.openimaj.rdf.storm.tool.topology;

import org.openimaj.storm.tool.StormToolOptions;

/**
 * A topoogy mode controls how a topology is submitted
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public interface TopologyMode {

	/**
	 * @param options
	 * @throws Exception
	 */
	public void submitTopology(StormToolOptions options) throws Exception;

	/**
	 * After everything else in the tool is done, what should happen?
	 * 
	 * @param options
	 * @throws Exception
	 */
	public void finish(StormToolOptions options) throws Exception;

}
