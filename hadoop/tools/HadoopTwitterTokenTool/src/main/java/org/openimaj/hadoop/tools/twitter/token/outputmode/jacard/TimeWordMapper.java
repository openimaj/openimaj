package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;

/**
 * @author ss
 *
 * A mapper which given a word with many {@link WordDFIDF} entries, outputs the word as value and the earliest time as key
 */
public class TimeWordMapper extends Mapper<Text, BytesWritable, LongWritable, Text> {
	
	/**
	 * 
	 */
	public TimeWordMapper() {}
	
	protected void map(final Text key, BytesWritable value, final org.apache.hadoop.mapreduce.Mapper<Text,BytesWritable,LongWritable,Text>.Context context) throws java.io.IOException ,InterruptedException {
		try {
			IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
				@Override
				protected Object readValue(DataInput in) throws IOException {
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(in);
					
					try {
						context.write(new LongWritable(idf.timeperiod), key);
					} catch (InterruptedException e) {
						throw new IOException(e);
					}
					return new Object();
				}
			});
		} catch (IOException e) {
			System.err.println("Couldnt read word: " + key);
		}
	};
}
