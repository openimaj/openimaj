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
package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.helper.TextByteByteStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.io.IOUtils;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PairMutualInformation extends TextByteByteStage{

	/**
	 * The time delta between time periods
	 */
	public static final String TIMEDELTA = "org.openimaj.hadoop.tools.twitter.token.mode.pairwisemi.timedelta";
	/**
	 * The location of the statistics file
	 */
	public static final String PAIR_STATS_FILE = "pairstats";
	/**
	 * The pairMI output directory
	 */
	public static final String PAIRMI_DIR = "pairmi";
	/**
	 * The root directory where timeperiod pair counts will be stored
	 */
	public static final String TIMEPERIOD_COUNT_OUTPUT_ROOT = "org.openimaj.hadoop.tools.twitter.token.mode.pairwisemi.timeoutloc";
	/**
	 * Name of the timeperiod count directory
	 */
	public static final String TIMEPERIOD_OUTPUT_NAME = "timeperiod_counts";
	private String[] nonHadoopArgs;
	private long timedelta;
	private Path actualOutputLocation;

	/**
	 * @param outpath where the output is going
	 * @param nonHadoopArgs the arguments for configuration
	 */
	public PairMutualInformation(String[] nonHadoopArgs, long timedelta) {
		this.nonHadoopArgs = nonHadoopArgs;
		this.timedelta = timedelta;
	}

	@Override
	public void setup(Job job) throws IOException {
		job.getConfiguration().setStrings(HadoopTwitterTokenToolOptions.ARGS_KEY, nonHadoopArgs);
		job.getConfiguration().setLong(TIMEDELTA, timedelta);
		Path tpcOutRoot = new Path(this.actualOutputLocation,TIMEPERIOD_OUTPUT_NAME);
		job.getConfiguration().set(TIMEPERIOD_COUNT_OUTPUT_ROOT, tpcOutRoot.toString());
		if(timedelta!=-1){
			// if there are multiple times, split a file per day 
			job.setNumReduceTasks(365);
		}
		
		((JobConf)job.getConfiguration()).setOutputValueGroupingComparator(TokenPairValueGroupingComparator.class);
		((JobConf)job.getConfiguration()).setOutputKeyComparatorClass(TokenPairKeyComparator.class);
		job.setPartitionerClass(TokenPairPartitioner.class);
	}
	
	@Override
	public Class<PairEmit> mapper() {
		return PairEmit.class;
	}
	
	
	@Override
	public Class<? extends Reducer<BytesWritable, BytesWritable, BytesWritable, BytesWritable>> combiner() {
		return PairEmitCombiner.class;
	}
	
	@Override
	public Job stage(Path[] inputs, Path output, Configuration conf) throws Exception {
		this.actualOutputLocation = output; 
		return super.stage(inputs, output, conf);
	}
	
	@Override
	public Class<? extends Reducer<BytesWritable, BytesWritable, BytesWritable, BytesWritable>> reducer() {
		return PairEmitCounter.class;
	}
	
	@Override
	public String outname() {
		return PAIRMI_DIR;
	}
	@Override
	public void finished(Job job) {
		Path out = new Path(actualOutputLocation, PAIR_STATS_FILE);
		FileSystem fs;
		try {
			fs = HadoopToolsUtil.getFileSystem(out);
			FSDataOutputStream os = fs.create(out);
			IOUtils.writeASCII(os, new WritablePairEnum(job.getCounters(),PairEnum.values()));
		} catch (IOException e) {
		}
	}
	
	/**
	 * Load the PointwisePMI stats file from an output location (Path: outpath/{@link PairMutualInformation#PAIR_STATS_FILE}
	 * @param outpath
	 * @return a WritablePairEnum instance with the counter values filled
	 * @throws IOException
	 */
	public static WritablePairEnum loadStats(Path outpath) throws IOException{
		Path pmistats = new Path(outpath,PairMutualInformation.PAIRMI_DIR);
		pmistats = new Path(pmistats,PairMutualInformation.PAIR_STATS_FILE);
		FileSystem fs = HadoopToolsUtil.getFileSystem(pmistats);
		FSDataInputStream inp = fs.open(pmistats);
		WritablePairEnum ret = IOUtils.read(inp,WritablePairEnum.class);
		return ret;
	}

	/**
	 * Load the total pairs seen in every time period from the pairmi location provided
	 * @param pairmiloc a directory which contains {@link #PAIRMI_DIR}/{@link #TIMEPERIOD_OUTPUT_NAME}
	 * @return map of a time period to a count
	 * @throws IOException 
	 */
	public static Map<Long,Long> loadTimeCounts(Path pairmiloc) throws IOException {
		Path dir = new Path(new Path(pairmiloc,PAIRMI_DIR),TIMEPERIOD_OUTPUT_NAME);
		FileSystem fs = HadoopToolsUtil.getFileSystem(dir);
		FileStatus[] timePaths = fs.listStatus(dir);
		
		Map<Long, Long> out = new HashMap<Long, Long>();
		for (FileStatus fileStatus : timePaths) {
			Path fsp = fileStatus.getPath();
			Long time = Long.parseLong(fsp.getName());
			BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(fsp)));
			Long count = Long.parseLong(reader.readLine());
			out.put(time, count);
		}
		return out ;
	}
	
}
