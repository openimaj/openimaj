/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.twitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.tools.twitter.modes.output.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.TwitterPreprocessingToolOptions;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.TwitterStatusList;
import org.openimaj.utils.threads.WatchedRunner;

/**
 * A tool for applying preprocessing to a set of tweets and outputting the results in json
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
		try {
			options = new TwitterPreprocessingToolOptions(args);
		} catch (CmdLineException e1) {
			System.err.println(e1.getMessage());
			System.err.println("Usage: java -jar JClusterQuantiser.jar [options...] [files...]");
			e1.getParser().printUsage(System.err);
			System.exit(1);
		}
		TwitterOutputMode outputMode;
		final List<TwitterPreprocessingMode<?>> modes;
		try {
			modes = options.preprocessingMode();
			outputMode = options.ouputMode();
			outputMode.delimit("\n");
		} catch (Exception e) {
			System.err.println("Could not create processing mode!");
			e.printStackTrace();
			return;
		}

		while(options.hasNextFile()){
			options.nextFile();
			options.progress("Preparing tweets\n");
			TwitterStatusList<USMFStatus> tweets = options.getTwitterStatusList();
			options.progress("Processing " + tweets.size() + " tweets\n");

			long done = 0;
			long skipped = 0;
			long start = System.currentTimeMillis();
			PrintWriter oWriter = options.outputWriter();
			for (final USMFStatus twitterStatus : tweets) {
				if(twitterStatus.isInvalid() || twitterStatus.text.isEmpty()){
					if(options.veryLoud()){
						System.out.println("\nTWEET INVALID, skipping.");
					}
					continue;
				}
				if(options.veryLoud()){
					System.out.println("\nPROCESSING TWEET");
					System.out.println(twitterStatus);
				}

				if(options.preProcessesSkip(twitterStatus)) continue;

				WatchedRunner runner = new WatchedRunner(options.getTimeBeforeSkip()){
					@Override
					public void doTask() {
						for (TwitterPreprocessingMode<?> mode : modes) {
							try {
								TwitterPreprocessingMode.results(twitterStatus, mode);
							} catch (Exception e) {
								System.err.println("Mode failed: " + mode);
							}
						}
					}
				};
				runner.go();
				if(runner.taskCompleted()){
					done++;
					options.progress("\rDone: " + done);


					if(!options.postProcessesSkip(twitterStatus))
					{
						outputMode.output(options.convertToOutputFormat(twitterStatus),oWriter);
						oWriter.flush();
					}
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
		}
		options.outputWriter().flush();
		options.outputWriter().close();
	}
}
