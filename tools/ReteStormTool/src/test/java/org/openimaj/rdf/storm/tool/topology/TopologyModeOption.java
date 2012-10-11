package org.openimaj.rdf.storm.tool.topology;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * Topology submission instructions.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum TopologyModeOption implements CmdLineOptionsProvider{
	/**
	 * Submit to the specified Storm cluster
	 */
	STORM {
		@Override
		public TopologyMode getOptions() {
			return new StormTopologyMode();
		}
	},
	/**
	 * Construct a local cluster
	 */
	LOCAL {
		@Override
		public TopologyMode getOptions() {
			return new LocalTopologyMode();
		}
	}
	;

	@Override
	public abstract TopologyMode getOptions() ;

}
