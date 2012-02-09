package org.openimaj.tools.twitter.modes;

import java.io.IOException;
import java.io.PrintWriter;

import org.openimaj.twitter.TwitterStatus;

public interface TwitterOutputMode {

	public void output(TwitterStatus twitterStatus, PrintWriter outputWriter) throws IOException;

}
