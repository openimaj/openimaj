package org.openimaj.rdf.storm.tool.source;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum TriplesInputModeOption implements CmdLineOptionsProvider{
	/**
	 * Consume triples from a URI
	 */
	URI{
		@Override
		public TriplesInputMode getOptions() {
			return new URITriplesInputMode();
		}

	};

	@Override
	public abstract TriplesInputMode getOptions() ;
}
