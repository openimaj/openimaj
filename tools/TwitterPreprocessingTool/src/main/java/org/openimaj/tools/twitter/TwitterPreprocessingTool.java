package org.openimaj.tools.twitter;

import java.io.IOException;
import java.util.List;

import org.openimaj.tools.twitter.modes.output.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.TwitterPreprocessingToolOptions;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.TwitterStatusList;
import org.openimaj.utils.threads.WatchedRunner;

/**
 * A tool for applying preprocessing to a set of tweets and outputting the results in json
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TwitterPreprocessingTool 
{
	static TwitterPreprocessingToolOptions options;
	
	/**
	 * Run the tool 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		options = new TwitterPreprocessingToolOptions(args);
		options.progress("Preparing tweets\n");
		TwitterStatusList tweets = options.getTwitterStatusList();
		options.progress("Processing " + tweets.size() + " tweets\n");
		final List<TwitterPreprocessingMode<?>> modes;
		TwitterOutputMode outputMode;
		try {
			modes = options.preprocessingMode();
			outputMode = options.ouputMode();
			outputMode.deliminate("\n");
		} catch (Exception e) {
			System.err.println("Could not create processing mode!");
			e.printStackTrace();
			return;
		}
		
		long done = 0;
		long skipped = 0;
		long start = System.currentTimeMillis();
		for (final TwitterStatus twitterStatus : tweets) {
			if(options.veryLoud()){
				System.out.println("\nPROCESSING TWEET");
				System.out.println(twitterStatus);
			}
			WatchedRunner runner = new WatchedRunner(options.getTimeBeforeSkip()){
				@Override
				public void doTask() {
					for (TwitterPreprocessingMode<?> mode : modes) {
						mode.process(twitterStatus);
					}
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
