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
package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.PairMutualInformation;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort.PMIPairSort;

/**
 * Perform DFIDF and output such that each timeslot is a instance and each word a feature
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PairwiseMutualInformationMode implements TwitterTokenMode {
	
	private MultiStagedJob stages;
	private String[] fstage;
	@Option(name="--time-delta", aliases="-t", required=false, usage="The length of a time window in minutes (defaults to -1, i.e. not used, one time period)", metaVar="STRING")
	private long timeDelta = -1;
	@Option(name="--min-p-value", aliases="-minp", required=false, usage="The minimum PMI value")
	double minp = 0;
	@Option(name="--min-pair-count", aliases="-minpc", required=false, usage="The minimum number of times a pair must occur")
	int minPairCount = 0;

	@Override
	public void perform(final HadoopTwitterTokenToolOptions opts) throws Exception {
		Path outpath = HadoopToolsUtil.getOutputPath(opts);
		this.stages = new MultiStagedJob(HadoopToolsUtil.getInputPaths(opts),outpath,opts.getArgs());
		stages.queueStage(new PairMutualInformation(opts.getNonHadoopArgs(),timeDelta));
		stages.queueStage(new PMIPairSort(minp, minPairCount, outpath));
		stages.runAll();
	}

	@Override
	public String[] finalOutput(HadoopTwitterTokenToolOptions opts) throws Exception {
		return this.fstage;
	}

	public static BufferedReader sortedPMIReader(File outputLocation) throws IOException {
		Path path = HadoopToolsUtil.getInputPaths(outputLocation.getAbsolutePath() + Path.SEPARATOR + PMIPairSort.PMI_NAME)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(path);
		FSDataInputStream is = fs.open(path);
		return new BufferedReader(new InputStreamReader(is,"UTF-8"));
	}
	
}
