package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.PairEnum;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.PairMutualInformation;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.PairMutualInformation.WritablePairEnum;
import org.openimaj.util.pair.IndependentPair;

/**
 * Sort pairs by PMI within timeperiods
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PMIPairSort extends SequenceFileTextStage<Text,BytesWritable,Text,BytesWritable,NullWritable,Text>{
	/**
	 * The minimum PMI
	 */
	public static final String MINP_KEY = "org.openimaj.hadoop.tools.twitter.token.outputmode.pointwisemi.maxp";
	/**
	 * the number of pairs
	 */
	public static final String N_PAIRS = "org.openimaj.hadoop.tools.twitter.token.outputmode.pointwisemi.npairs";
	/**
	 * the output name
	 */
	public static final String PMI_NAME = "pmi_sort";
	private double maxp;
	private Path outpath;

	/**
	 * @param minp the minimum PMI value
	 * @param outpath for loading the PMIStats file
	 */
	public PMIPairSort(double minp,Path outpath) {
		this.maxp = minp;
		this.outpath = outpath;
	}

	@Override
	public Class<? extends Mapper<Text, BytesWritable, Text, BytesWritable>> mapper() {
		return PMISortMapper.class;
	}
	
	@Override
	public Class<? extends Reducer<Text, BytesWritable, NullWritable,Text>> reducer() {
		return PMISortReducer.class;
	}
	@Override
	public String outname() {
		return PMI_NAME;
	}
	@Override
	public void setup(Job job) {
		double n = 1;
		try {
			WritablePairEnum pmistats = PairMutualInformation.loadStats(outpath);
			n = pmistats.getValue(PairEnum.PAIR);
		} catch (IOException e) {
			n = 1;
		}
		job.getConfiguration().setFloat(MINP_KEY, (float) this.maxp);
		job.getConfiguration().setFloat(N_PAIRS, (float) n);
		((JobConf)job.getConfiguration()).setOutputValueGroupingComparator(PMISortValueGroupingComparator.class);
		((JobConf)job.getConfiguration()).setOutputKeyComparatorClass(PMISortKeyComparator.class);
		job.setPartitionerClass(PMISortPartitioner.class);
	}

	/**
	 * Parse the time/pmi string which is in the format: "T<time>#<pmi>"
	 * @param str
	 * @return the pair
	 */
	public static IndependentPair<Long, Double> parseTimeStr(String str) {
		String[] splt = str.split("#");
		String tstr = splt[0];
		tstr = tstr.substring(tstr.lastIndexOf("T") + 1);
		long time = Long.parseLong(tstr);
		double pmi = Double.parseDouble(splt[1]);
		return IndependentPair.pair(time, pmi);
	}
}
