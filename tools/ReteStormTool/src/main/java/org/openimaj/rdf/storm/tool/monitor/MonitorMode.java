package org.openimaj.rdf.storm.tool.monitor;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.Option;
import org.openimaj.rdf.storm.tool.ReteStormOptions;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class MonitorMode implements Runnable{

	@Option(
			name = "--monitor-mode-output",
			aliases = "-mmo",
			required = false,
			usage = "Where the monitor should output")
	public File monitorOutput = new File("monitor.out");
	/**
	 * The options object is handed for initilisation
	 * @param opts
	 * @throws IOException
	 */
	public abstract void init(ReteStormOptions opts) throws IOException;
}
