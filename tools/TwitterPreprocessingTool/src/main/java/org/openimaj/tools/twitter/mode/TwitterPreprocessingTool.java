package org.openimaj.tools.twitter.mode;

import java.io.IOException;

import org.openimaj.tools.twitter.modes.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.TwitterPreprocessingToolOptions;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.TwitterStatusList;

public class TwitterPreprocessingTool 
{
	static TwitterPreprocessingToolOptions options;
	
	public static void main(String[] args) throws IOException {
		options = new TwitterPreprocessingToolOptions(args);
		
		TwitterStatusList tweets = options.getTwitterStatusList();
		
		TwitterPreprocessingMode mode = options.preprocessingMode();
		TwitterOutputMode outputMode = options.ouputMode();
		for (TwitterStatus twitterStatus : tweets) {
			mode.process(twitterStatus);
			outputMode.output(twitterStatus,options.outputWriter());
		}
	}
}
