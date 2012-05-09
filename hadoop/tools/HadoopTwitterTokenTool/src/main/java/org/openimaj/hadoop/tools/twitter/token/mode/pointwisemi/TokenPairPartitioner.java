package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.util.regex.Pattern;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Partitioner;

public class TokenPairPartitioner extends Partitioner<Text,BytesWritable> {


	@Override
	public int getPartition(Text key, BytesWritable value, int numPartitions) {
		long time = Long.parseLong(key.toString().substring(key.toString().indexOf('#')+1).split(Pattern.quote(PairEmit.TIMESPLIT))[0]);
		return (int) (time % numPartitions);
	}

}
