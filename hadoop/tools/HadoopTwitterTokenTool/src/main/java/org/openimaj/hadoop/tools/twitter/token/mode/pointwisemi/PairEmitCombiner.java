package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.io.IOUtils;

/**
 * Assumes each key is a timeperiod split set of words ordered by single/pair words then by word order.
 * The key is only used to get time.
 * Using this time the values are combined and used to construct new keys
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
		TokenPairCollector collector = new TokenPairCollector();
		long time = Long.parseLong(timeword.toString().substring(timeword.toString().indexOf('#')+1).split(Pattern.quote(PairEmit.TIMESPLIT))[0]);
		for (BytesWritable bytesWritable : paircounts) {
			TokenPairCount paircount = IOUtils.deserialize(bytesWritable.getBytes(), TokenPairCount.class);
			TokenPairCount collectorRet = collector.add(paircount);
			if(collectorRet != null){
				context.write(new Text(time + PairEmit.TIMESPLIT + collectorRet.toString()), new BytesWritable(IOUtils.serialize(collectorRet)));
			}
		}
		// Final write
		TokenPairCount collectorRet = collector.getCurrent();
		context.write(new Text(time + PairEmit.TIMESPLIT + collectorRet.toString()), new BytesWritable(IOUtils.serialize(collectorRet)));
	}
}
