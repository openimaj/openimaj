package org.kohsuke.args4j;

/**
 * Interface for objects that have associated objects that can
 * provide args4j options and arguments.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public interface CmdLineOptionsProvider {
	/**
	 * @return the object providing options/arguments
	 */
	public Object getOptions();
}
