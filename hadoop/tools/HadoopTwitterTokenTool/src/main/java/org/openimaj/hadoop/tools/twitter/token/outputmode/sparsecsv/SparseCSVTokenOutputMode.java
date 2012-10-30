/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.CountTweetsInTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;

/**
 * Create a sparse CSV token output. The directory created contains 3 files:
 * 	words/ : contains a list of words ordered by count across all time.
 * 	times/ : contains a list of times ordered by count of all tweets
 * 	values/ : a list of (wordIndex,timeIndex,wordTimeCount,tweetTimeCount,tweetCount,wordCount)
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SparseCSVTokenOutputMode extends TwitterTokenOutputMode {

	private MultiStagedJob stages;
	@Option(name="--value-reduce-split", aliases="-vrs", required=false, usage="The number of reducers to use when spitting out the DFIDF values")
	int valueSplitReduce = 1;

	@Option(name="--word-occurence-threshold", aliases="-wot", required=false, usage="The number of times a given word must appear total throughout the time period before it is involved in the count and index")
	int wordCountThreshold = 0;

	@Option(name="--word-time-occurence-threshold", aliases="-wtot", required=false, usage="The number of times a given word must appear in one or more time period before the word is chosen for indexing")
	int wordTimeCountThreshold = 0;

	@Option(name="--top-n-words", aliases="-tnw", required=false, usage="Select only the top n words (as ordered by total occurence in the time period)")
	int topNWords = -1;

	@Option(name="--sort-value-by-time", aliases="-svbt", required=false, usage="This flag sorts value by time instead of word")
	boolean sortValueByTime = false;

	@Option(name="--matlab-output", aliases="-matlab", required=false, usage="This flag sorts value by time instead of word")
	boolean matlabOutput = false;
	@Override
	public void write(
			HadoopTwitterTokenToolOptions opts,
			TwitterTokenMode completedMode) throws Exception{

		HadoopToolsUtil.validateOutput(outputPath,replace);

		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		matlabOutput = matlabOutput && sortValueByTime;
		// Three stage process
		// 1a. Write all the words (word per line)
//		stages.queueStage(new WordIndex().stage());
		new WordIndex(wordCountThreshold,wordTimeCountThreshold,topNWords).stage(stages);
		final Path wordIndex = stages.runAll();
		// 1b. Write all the times (time per line)
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountTweetsInTimeperiod.TIMECOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new TimeIndex().stage());
		final Path timeIndex = stages.runAll();
		// 3. Write all the values (loading in the words and times)

		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new Values(outputPath,valueSplitReduce,sortValueByTime,matlabOutput).stage());
		stages.runAll();
	}


}
