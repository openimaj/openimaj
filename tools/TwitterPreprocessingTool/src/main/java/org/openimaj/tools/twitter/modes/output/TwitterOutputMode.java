package org.openimaj.tools.twitter.modes.output;

import java.io.IOException;
import java.io.PrintWriter;

import org.openimaj.twitter.TwitterStatus;

/**
 * how the processing should be outputed
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public interface TwitterOutputMode {

	/**
	 * Output a tweet to a printwriter
	 * @param twitterStatus
	 * @param outputWriter
	 * @throws IOException
	 */
	public void output(TwitterStatus twitterStatus, PrintWriter outputWriter) throws IOException;

	/**
	 * how outputs should be seperated
	 * @param string
	 */
	public void deliminate(String string);

}
