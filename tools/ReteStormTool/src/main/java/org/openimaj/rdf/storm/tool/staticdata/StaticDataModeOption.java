package org.openimaj.rdf.storm.tool.staticdata;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * The various ways with which static data can be used with the streaming
 * reasoners
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public enum StaticDataModeOption implements CmdLineOptionsProvider {
	/**
	 * Load RDF from files into memory
	 */
	IN_MEMORY {
		@Override
		public StaticDataMode getOptions() {
			return new InMemoryStaticDataMode();
		}
	},
	/**
	 * Load RDF into a database using SDB
	 */
	SDB {

		@Override
		public StaticDataMode getOptions() {
			return new SDBStaticDataMode();
		}

	};

	@Override
	public abstract StaticDataMode getOptions();

}
