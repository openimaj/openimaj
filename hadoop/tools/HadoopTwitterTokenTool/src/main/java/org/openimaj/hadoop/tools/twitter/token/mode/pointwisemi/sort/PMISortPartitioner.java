package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.openimaj.util.pair.IndependentPair;

public class PMISortPartitioner extends Partitioner<Text,BytesWritable> {


	@Override
	public int getPartition(Text key, BytesWritable value, int numPartitions) {
		IndependentPair<Long, Double> timepmi = PMIPairSort.parseTimeStr(key.toString());
		return (int) (timepmi.firstObject() % numPartitions);
	}

}
