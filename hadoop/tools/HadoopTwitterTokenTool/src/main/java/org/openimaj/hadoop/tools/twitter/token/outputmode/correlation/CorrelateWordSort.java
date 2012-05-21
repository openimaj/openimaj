package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.helper.TextSomethingTextStage;

public class CorrelateWordSort extends TextSomethingTextStage<DoubleWritable, Text>{
	public static final String MAXP_KEY = "org.openimaj.hadoop.tools.twitter.token.outputmode.correlation.maxp";
	private double maxp;

	public CorrelateWordSort(double maxp) {
		this.maxp = maxp;
	}

	@Override
	public Class<? extends Mapper<LongWritable, Text, DoubleWritable, Text>> mapper() {
		return PValueWordMapper.class;
	}
	
	@Override
	public Class<? extends Reducer<DoubleWritable, Text, NullWritable, Text>> reducer() {
		return PValueWordReducer.class;
	}
	@Override
	public String outname() {
		return "correlate_sort";
	}
	@Override
	public void setup(Job job) {
		job.getConfiguration().setFloat(MAXP_KEY, (float) this.maxp);
		job.setSortComparatorClass(DoubleWritable.Comparator.class);
	}
}
