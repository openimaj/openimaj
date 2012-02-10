package org.openimaj.tools.twitter;

import java.io.IOException;

import org.openimaj.tools.twitter.modes.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;
import org.openimaj.tools.twitter.options.TwitterPreprocessingToolOptions;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.TwitterStatusList;

public class TwitterPreprocessingTool 
{
	static TwitterPreprocessingToolOptions options;
	
	public static void main(String[] args) throws IOException {
		options = new TwitterPreprocessingToolOptions(args);
		options.progress("Preparing tweets\n");
		TwitterStatusList tweets = options.getTwitterStatusList();
		options.progress("Processing " + tweets.size() + " tweets\n");
		TwitterPreprocessingMode mode = options.preprocessingMode();
		TwitterOutputMode outputMode = options.ouputMode();
		long done = 0;
		long start = System.currentTimeMillis();
		for (TwitterStatus twitterStatus : tweets) {
			mode.process(twitterStatus);
//			if(done%1000 == 0) 
				options.progress("\rDone: " + done);
			done++;
			outputMode.output(twitterStatus,options.outputWriter());
		}
		long end = System.currentTimeMillis();
		options.progress(String.format("\nTook: %d\n",(end-start)));
		options.progress("Done!\n");
		options.outputWriter().flush();
		options.outputWriter().close();
	}
}
