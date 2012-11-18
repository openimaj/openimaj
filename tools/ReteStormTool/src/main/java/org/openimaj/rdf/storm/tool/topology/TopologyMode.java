package org.openimaj.rdf.storm.tool.topology;

import org.openimaj.rdf.storm.tool.ReteStormOptions;

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
	public void submitTopology(ReteStormOptions options) throws Exception;

	/**
	 * After everything else in the tool is done, what should happen?
	 * 
	 * @param options
	 */
	public void finish(ReteStormOptions options) throws Exception;

}
