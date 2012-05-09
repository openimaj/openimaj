package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.io.IOUtils;

/**
 * Given a key string, split on time and word. The key looks like:
 * 
 * <time> + {@link PairEmit#TIMESPLIT} + <key>
 * 
 * Emit for the given time a combined version of the word's count. 
 * The word might be a pair or a unary count.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PairEmitCombiner extends Reducer<Text, BytesWritable, Text, BytesWritable> {
	@Override
	protected void reduce(Text timeword, Iterable<BytesWritable> paircounts, Reducer<Text,BytesWritable,Text,BytesWritable>.Context context) throws IOException ,InterruptedException {
		String[] split = timeword.toString().split(PairEmit.TIMESPLIT);
		long time = Long.parseLong(split[0]);
		TokenPairCount counter = null;
		for (BytesWritable bytesWritable : paircounts) {
			TokenPairCount paircount = IOUtils.deserialize(bytesWritable.getBytes(), TokenPairCount.class);
			if(counter == null) 
				counter = paircount;
			else
				counter.add(paircount);
		}
		context.write(new Text(time + ""), new BytesWritable(IOUtils.serialize(counter)));
	}
}
