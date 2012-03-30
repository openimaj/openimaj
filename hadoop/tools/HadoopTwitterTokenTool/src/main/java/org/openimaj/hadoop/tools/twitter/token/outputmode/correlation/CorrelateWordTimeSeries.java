package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.util.Map;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class CorrelateWordTimeSeries extends SequenceFileTextStage<Text, BytesWritable, Text, BytesWritable, NullWritable, Text>{
	/**
	 * the directory this stage will output
	 */
	public static final String CORRELATE_WORDTIME = "correlate_wordtime";
	private static final String PERIOD_START = "org.openimaj.hadoop.tools.twitter.time.startperiod";
	private static final String PERIOD_END = "org.openimaj.hadoop.tools.twitter.time.endperiod";
	public static final String FINANCE_DATA = "org.openimaj.hadoop.tools.twitter.finance.data";
	private String finance;
	private long start;
	private long end;

	public CorrelateWordTimeSeries(String financelocation,IndependentPair<Long, Long> startend) {
		this.finance = financelocation;
		this.start = startend.firstObject();
		this.end = startend.secondObject();
	}
	
	public void setup(org.apache.hadoop.mapreduce.Job job) {
		job.getConfiguration().setLong(PERIOD_START, start);
		job.getConfiguration().setLong(PERIOD_END, end);
		job.getConfiguration().setStrings(FINANCE_DATA, finance);
	};
	
	@Override
	public String outname() {
		return CORRELATE_WORDTIME;
	}
	
//	@Override
//	public Class<? extends Mapper<Text, BytesWritable, Text, BytesWritable>> mapper() {
//		return WordTimeperiodValueMapper.class;
//	}
	
	@Override
	public Class<? extends Reducer<Text, BytesWritable, NullWritable, Text>> reducer() {
		return WordValueCorrelationReducer.class;
	}
}
