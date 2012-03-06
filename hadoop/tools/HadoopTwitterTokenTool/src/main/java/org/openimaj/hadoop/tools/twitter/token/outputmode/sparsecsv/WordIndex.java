package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class WordIndex {

	/**
	 * Emits each word with the total number of times the word was seen
	 * @author ss
	 *
	 */
	public static class Map extends Mapper<Text,BytesWritable,LongWritable,Text>{
		public Map() {
			// TODO Auto-generated constructor stub
		}
		public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,LongWritable,Text>.Context context){
			try {
				IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
					boolean readmore = true;
					@Override
					protected Object readValue(DataInput in) throws IOException {
						if(readmore){
							WordDFIDF idf = new WordDFIDF();
							readmore = false;
							idf.readBinary(in);
							try {
								context.write(new LongWritable(idf.Twf), key);
							} catch (InterruptedException e) {
								throw new IOException("");
							}
							
						}
						return new Object();
					}
				});
				
			} catch (IOException e) {
				System.err.println("Couldnt read word: " + key);
			}
		}
	}
	/**
	 * Writes each word,count
	 * @author ss
	 *
	 */
	public static class Reduce extends Reducer<LongWritable,Text,NullWritable,Text>{
		public Reduce() {
			// TODO Auto-generated constructor stub
		}
		public void reduce(LongWritable count, Iterable<Text> words, Reducer<LongWritable,Text,NullWritable,Text>.Context context){
			try {
				String countStr = count.toString();
				for (Text text : words) {
					StringWriter swriter = new StringWriter();
					CSVWriter writer = new CSVWriter(
							swriter,
							CSVWriter.DEFAULT_SEPARATOR, 
							CSVWriter.DEFAULT_QUOTE_CHARACTER, 
							CSVWriter.DEFAULT_ESCAPE_CHARACTER, "");
					writer.writeNext(new String[]{text.toString(),countStr});
					writer.flush();
					context.write(NullWritable.get(), new Text(swriter.toString()));
				}
				
			} catch (Exception e) {
				System.err.println("Couldn't reduce to final file");
			}
		}
	}
	
	/**
	 * from a report output path get the words
	 * @param path report output path
	 * @return map of words to counts and index
	 * @throws IOException 
	 */
	public static HashMap<String, IndependentPair<Long, Long>> readWordCountLines(String path) throws IOException {
		String wordPath = path + "/words";
		Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataInputStream toRead = fs.open(p);
		BufferedReader reader = new BufferedReader(new InputStreamReader(toRead,"UTF-8"));
		CSVReader csvreader = new CSVReader(reader);
		long lineN = 0;
		String[] next = null;
		HashMap<String, IndependentPair<Long, Long>> toRet = new HashMap<String, IndependentPair<Long,Long>>();
		while((next = csvreader.readNext())!=null && next.length > 0){
			toRet.put(next[0], IndependentPair.pair(Long.parseLong(next[1]), lineN));
			lineN ++;
		}
		return toRet;
	}

}
