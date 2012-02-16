package org.openimaj.tools.twitter.modes.preprocessing;

import java.util.List;

import org.openimaj.twitter.TwitterStatus;

public interface TwitterPreprocessingMode {

	public void process(TwitterStatus twitterStatus);

}
