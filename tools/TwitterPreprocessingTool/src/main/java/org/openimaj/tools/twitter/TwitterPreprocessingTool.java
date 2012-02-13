package org.openimaj.tools.twitter;

import java.io.IOException;

import org.openimaj.tools.twitter.modes.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;
import org.openimaj.tools.twitter.options.TwitterPreprocessingToolOptions;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.TwitterStatusList;
import org.openimaj.utils.threads.WatchedRunner;

public class TwitterPreprocessingTool 
{
	static TwitterPreprocessingToolOptions options;
	
	public static void main(String[] args) throws IOException {
		options = new TwitterPreprocessingToolOptions(args);
		options.progress("Preparing tweets\n");
		TwitterStatusList tweets = options.getTwitterStatusList();
		options.progress("Processing " + tweets.size() + " tweets\n");
		final TwitterPreprocessingMode mode = options.preprocessingMode();
		TwitterOutputMode outputMode = options.ouputMode();
		long done = 0;
		long skipped = 0;
		long start = System.currentTimeMillis();
		for (final TwitterStatus twitterStatus : tweets) {
			if(options.veryLoud()){
				System.out.println("\nPROCESSING TWEET");
				System.out.println(twitterStatus);
			}
			WatchedRunner runner = new WatchedRunner(options.timeBeforeSkip){
				@Override
				public void doTask() {
					mode.process(twitterStatus);
				}
			};
			runner.go();
			if(runner.taskCompleted()){
				done++;
//				if(done%1000 == 0) 
					options.progress("\rDone: " + done);
				
				outputMode.output(twitterStatus,options.outputWriter());
			}
			else{
				skipped ++;
			}
			if(skipped > 0){
				options.progress(" (Skipped: " + skipped + ") ");
			}
			
			
			
		}
		long end = System.currentTimeMillis();
		options.progress(String.format("\nTook: %d\n",(end-start)));
		options.progress("Done!\n");
		options.outputWriter().flush();
		options.outputWriter().close();
	}
}
