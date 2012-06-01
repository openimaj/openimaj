package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.IOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.log4j.Logger;
import org.openimaj.util.pair.IndependentPair;

public class TokenPairPartitioner extends Partitioner<BytesWritable,BytesWritable> implements Configurable{


	private Configuration conf;
	private long timedelta;
	Logger logger = Logger.getLogger(TokenPairPartitioner.class);

	@Override
	public int getPartition(BytesWritable key, BytesWritable value, int numPartitions) {
		long time = -1;
		try {
			time = TokenPairCount.timeFromBinaryIdentity(key.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(timedelta == -1) return (int) (time % numPartitions);
		else return (int) ((time / timedelta) % numPartitions);
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
		this.timedelta = conf.getLong(PairMutualInformation.TIMEDELTA,-1);
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

}
