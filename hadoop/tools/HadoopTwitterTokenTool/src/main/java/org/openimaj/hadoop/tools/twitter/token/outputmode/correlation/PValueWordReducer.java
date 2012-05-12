package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PValueWordReducer extends Reducer<DoubleWritable, Text, NullWritable, Text> {
	public PValueWordReducer(){
		
	}
	@Override
	protected void reduce(DoubleWritable pvalue, java.lang.Iterable<Text> textvalues, Reducer<DoubleWritable,Text,NullWritable,Text>.Context context) throws IOException ,InterruptedException {
		for (Text text : textvalues) {
			context.write(NullWritable.get(), text);
		}
	};
}
