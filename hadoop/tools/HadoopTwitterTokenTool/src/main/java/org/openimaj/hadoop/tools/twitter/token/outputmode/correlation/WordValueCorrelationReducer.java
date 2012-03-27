package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class WordValueCorrelationReducer extends Reducer<Text, BytesWritable, NullWritable, Text>{

}
