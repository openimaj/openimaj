package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.helper.TextSomethingTextStage;
import org.openimaj.hadoop.mapreduce.stage.helper.TextTextByteStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.WritableEnumCounter;
import org.openimaj.io.IOUtils;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PairMutualInformation extends TextTextByteStage{

	protected static final String TIMEDELTA = "org.openimaj.hadoop.tools.twitter.token.mode.pairwisemi.timedelta";
	public static final String PAIR_STATS_FILE = "pairstats";
	public static final String PAIRMI_DIR = "pairmi";
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
	public void setup(Job job) {
		job.getConfiguration().setStrings(HadoopTwitterTokenToolOptions.ARGS_KEY, nonHadoopArgs);
		job.getConfiguration().setLong(TIMEDELTA, timedelta);
		((JobConf)job.getConfiguration()).setOutputValueGroupingComparator(TokenPairValueGroupingComparator.class);
		((JobConf)job.getConfiguration()).setOutputKeyComparatorClass(TokenPairKeyComparator.class);
		job.setPartitionerClass(TokenPairPartitioner.class);
	}
	
	@Override
	public Class<PairEmit> mapper() {
		return PairEmit.class;
	}
	
	@Override
	public Class<? extends Reducer<Text, BytesWritable, Text, BytesWritable>> combiner() {
		return PairEmitCombiner.class;
	}
	
	@Override
	public Job stage(Path[] inputs, Path output, Configuration conf) throws Exception {
		this.actualOutputLocation = output; 
		return super.stage(inputs, output, conf);
	}
	
	@Override
	public Class<? extends Reducer<Text, BytesWritable, Text, BytesWritable>> reducer() {
		return PairEmitCounter.class;
	}
	
	@Override
	public String outname() {
		return PAIRMI_DIR;
	}
	/**
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
	 *
	 */
	public static class WritablePairEnum extends WritableEnumCounter<PairEnum>{
		
		public WritablePairEnum() {
			// TODO Auto-generated constructor stub
		}
		/**
		 * @param counters
		 * @param values
		 */
		public WritablePairEnum(Counters counters, PairEnum[] values) {
			super(counters,values);
		}

		@Override
		public PairEnum valueOf(String str) {
			return PairEnum.valueOf(str);
		}
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
	
}
