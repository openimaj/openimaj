package org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;

/**
 * given a list of configured words, emits only those words
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SpecificWordSelectionMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
	
	
	private static List<String> wordlist;

	@Override
	protected void setup(org.apache.hadoop.mapreduce.Mapper<Text,BytesWritable,Text,BytesWritable>.Context context) throws java.io.IOException ,InterruptedException {
		load(context);
	}

	private static void load(Mapper<Text,BytesWritable,Text,BytesWritable>.Context context) {
		if(wordlist == null){
			
			wordlist = Arrays.asList(context.getConfiguration().getStrings(SpecificWordStageProvider.WORD_TIME_SERIES));
		}
	};
	
	@Override
	protected void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,Text,BytesWritable>.Context context) throws java.io.IOException ,InterruptedException {
		if(wordlist.contains(key.toString())){
			IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
				@Override
				protected Object readValue(DataInput in) throws IOException {
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(in);
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						IOUtils.writeBinary(baos, idf);
						context.write(key, new BytesWritable(baos.toByteArray()));
					} catch (InterruptedException e) {
						throw new IOException("");
					}
					return NullWritable.get();
				}
			});
		}
	};
}
