package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.StringWriter;
import java.util.HashSet;

import gnu.trove.TLongHashSet;
import gnu.trove.TObjectHash;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.io.IOUtils;

/**
 * A cumulative jacard reducer designed to work as a single reducer. This reducer recieves the timeperiods
 * in order and holds a set of words seen. Using this set the intersection and union with the current time's words can be
 * calculated
 * @author ss
 *
 */
public class CumulativeJacardReducer extends Reducer<LongWritable, Text, NullWritable, Text>{
	private HashSet<String> seenwords;

	public CumulativeJacardReducer() {
		this.seenwords = new HashSet<String>();
	}
	
	protected void reduce(LongWritable time, java.lang.Iterable<Text> words, org.apache.hadoop.mapreduce.Reducer<LongWritable,Text,NullWritable,Text>.Context context) throws java.io.IOException ,InterruptedException {
		HashSet<String> unseenwords = new HashSet<String>();
		StringWriter writer = new StringWriter();
		
		for (Text text : words) {
			unseenwords.add(text.toString());
		}
		long intersection = 0;
		for (String string : unseenwords) {
			if(this.seenwords.contains(string)) intersection += 1;
			this.seenwords.add(string);
		}
		
		JacardIndex index = new JacardIndex(time.get(),intersection,this.seenwords.size());
		IOUtils.writeASCII(writer, index);
		context.write(NullWritable.get(), new Text(writer.toString()));
	};
}
