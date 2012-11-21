package org.openimaj.rdf.storm.tool.monitor;

import java.io.IOException;

import org.openimaj.rdf.storm.tool.ReteStormOptions;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface MonitorMode extends Runnable{
	/**
	 * The options object is handed for initilisation
	 * @param opts
	 * @throws IOException
	 */
	public abstract void init(ReteStormOptions opts) throws IOException;
}
