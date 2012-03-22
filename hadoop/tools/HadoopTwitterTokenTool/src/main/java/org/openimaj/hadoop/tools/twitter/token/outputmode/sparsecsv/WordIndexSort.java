package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class WordIndexSort {

	public static class Reduce extends Reducer<LongWritable,Text,NullWritable,Text>{
		public Reduce() {
		}
		protected void reduce(LongWritable count, Iterable<Text> words, Reducer<LongWritable,Text,NullWritable,Text>.Context context) throws IOException ,InterruptedException {
			for (Text text : words) {
				context.write(NullWritable.get(), text);
			}
		};
	}

}
