package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.IOException;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.openimaj.util.pair.IndependentPair;

public class TokenPairPartitioner extends Partitioner<Text,BytesWritable> {


	@Override
	public int getPartition(Text key, BytesWritable value, int numPartitions) {
		IndependentPair<Long, TokenPairCount> timetokenpair;
		long time = -1;
		try {
			timetokenpair = TokenPairCount.parseTimeTokenID(key.toString());
			time = timetokenpair.firstObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (int) (time % numPartitions);
	}

}
