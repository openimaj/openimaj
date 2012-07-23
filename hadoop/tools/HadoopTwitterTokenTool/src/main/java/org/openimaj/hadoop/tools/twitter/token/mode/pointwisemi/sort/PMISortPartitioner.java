package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import java.io.IOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.Partitioner;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.PairMutualInformation;

public class PMISortPartitioner extends Partitioner<BytesWritable,BytesWritable> implements Configurable {


	private Configuration conf;
	private Long timedelta;

	@Override
	public int getPartition(BytesWritable key, BytesWritable value, int numPartitions) {;
		long time;
		try {
			time = PMIPairSort.parseTimeBinary(key.getBytes()).firstObject();
			if(timedelta == -1) {
				return (int) (time % numPartitions);
			}
			else {
				return (int) ((time / timedelta) % numPartitions);
			}
		} catch (IOException e) {
		}
		return 0;
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
