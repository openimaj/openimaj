package org.openimaj.tools.twitter.modes.filter;

import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * Command line options for regex engine selection
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum RegexEngineMode implements CmdLineOptionsProvider{
	/**
	 * The default {@link Pattern} regex engine
	 */
	JAVA {
		@Override
		public RegexEngine getOptions() {
			return new JavaRegexEngine();
		}
	};

	@Override
	public abstract RegexEngine getOptions();

}
