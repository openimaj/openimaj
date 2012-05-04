package org.openimaj.hadoop.tools.twitter.token.mode.pairwisemi;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
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
	private static final String PAIR_STATS_FILE = "pairstats";
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
	}
	
	@Override
	public Class<PairEmit> mapper() {
		return PairEmit.class;
	}
	
	@Override
	public Class<? extends Reducer<Text, BytesWritable, Text, BytesWritable>> combiner() {
		return PairEmitCounter.class;
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
	
	@Override
	public void finished(Job job) {
		Path out = new Path(actualOutputLocation, PAIR_STATS_FILE);
		FileSystem fs;
		try {
			fs = HadoopToolsUtil.getFileSystem(out);
			FSDataOutputStream os = fs.create(out);
			IOUtils.writeASCII(os, new WritableEnumCounter<PairEnum>(job.getCounters(),PairEnum.values()){
				@Override
				public PairEnum valueOf(String str) {
					return PairEnum.valueOf(str);
				}
			});
		} catch (IOException e) {
		}
	}
}
