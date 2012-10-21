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
package org.openimaj.hadoop.tools.twitter.token.mode.dfidf;

import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;

/**
 * Perform DFIDF and output such that each timeslot is a instance and each word
 * a feature
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class DFIDFTokenMode implements TwitterTokenMode {

	@Option(name = "--time-delta", aliases = "-t", required = false, usage = "The length of a time window in minutes (defaults to 1 hour (60))", metaVar = "STRING")
	private long timeDelta = 60;

	private boolean combiningReducers = false;
	private MultiStagedJob stages;
	private String[] fstage;

	@Override
	public void perform(final HadoopTwitterTokenToolOptions opts) throws Exception {
		Path outpath = HadoopToolsUtil.getOutputPath(opts);
		this.stages = new MultiStagedJob(HadoopToolsUtil.getInputPaths(opts), outpath, opts.getArgs());

		// Associate words with time periods and construct the time index
		stages.queueStage(new CountTweetsInTimeperiod(opts.getNonHadoopArgs(), combiningReducers, timeDelta).stage());
		// produce the DFIDF per word per time period
		stages.queueStage(new CountWordsAcrossTimeperiod(opts.getNonHadoopArgs(), combiningReducers).stage());

		stages.runAll();
		this.fstage = new String[] { outpath.toString() };
	}

	@Override
	public String[] finalOutput(HadoopTwitterTokenToolOptions opts) throws Exception {
		return this.fstage;
	}

}
