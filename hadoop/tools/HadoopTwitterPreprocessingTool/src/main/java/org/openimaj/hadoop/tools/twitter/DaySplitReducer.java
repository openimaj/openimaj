package org.openimaj.hadoop.tools.twitter;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * @author ss
 *
 */
public class DaySplitReducer extends Reducer<LongWritable, Text, NullWritable, Text> {
//	protected void reduce(LongWritable key, Iterable<Text> tweets, Reducer<NullWritable,Text,NullWritable,Text>.Context context) throws IOException ,InterruptedException {
//		for (Text text : tweets) {
//			MultipleOutputFormat<K, V>
//			context.write(NullWritable.get(), text);
//		}
//	};
}
