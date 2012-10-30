package org.openimaj.tools.twitter.modes.filter;

/**
 * Regex engines can be told about a set of regex patterns to match strings
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface RegexEngine {
	/**
	 * @param str to match against
	 * @return whether the string matched the regex
	 */
	public boolean matches(String str);
	/**
	 * @param pat add this pattern to the list of patterns to match
	 */
	public void add(String pat);
}
