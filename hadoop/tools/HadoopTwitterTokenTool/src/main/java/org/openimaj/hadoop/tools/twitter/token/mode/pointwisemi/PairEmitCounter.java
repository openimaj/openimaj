package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.io.IOUtils;

/**
 * Given a pair of words, count the times the pair was seen and the number of 
 * times each word was seen paired with any other word
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PairEmitCounter extends Reducer<Text, BytesWritable, Text, BytesWritable> {
	@Override
	protected void reduce(Text pair, Iterable<BytesWritable> paircounts, Reducer<Text,BytesWritable,Text,BytesWritable>.Context context) throws IOException ,InterruptedException {
		TokenPairCount counter = null;
		for (BytesWritable bytesWritable : paircounts) {
			TokenPairCount paircount = IOUtils.deserialize(bytesWritable.getBytes(), TokenPairCount.class);
			if(counter == null) 
				counter = paircount;
			else
				counter.add(paircount);
		}
		context.write(pair, new BytesWritable(IOUtils.serialize(counter)));
	}
}
