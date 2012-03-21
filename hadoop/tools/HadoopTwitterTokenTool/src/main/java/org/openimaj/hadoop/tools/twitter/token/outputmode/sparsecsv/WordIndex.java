package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;


public class WordIndex extends StageProvider {

	/**
	 * Emits each word with the total number of times the word was seen
	 * @author ss
	 *
	 */
	public static class Map extends Mapper<Text,BytesWritable,Text,LongWritable>{
		public Map() {
			// TODO Auto-generated constructor stub
		}
		public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,Text,LongWritable>.Context context){
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
								context.write(key, new LongWritable(idf.Twf));
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
	public static class Reduce extends Reducer<Text,LongWritable,NullWritable,Text>{
		public Reduce() {
			// TODO Auto-generated constructor stub
		}
		public void reduce(Text word, Iterable<LongWritable> counts, final Reducer<LongWritable,Text,NullWritable,Text>.Context context) throws IOException, InterruptedException{
			long countL = 0;
			for (LongWritable count : counts) {
				countL += count.get();
			}
			StringWriter swriter = new StringWriter();
			CSVPrinter writer = new CSVPrinter(swriter);
			writer.write(new String[]{word.toString(),countL + ""});
			writer.flush();
			context.write(NullWritable.get(), new Text(swriter.toString()));
		}
	}
	
	/**
	 * @param path
	 * @return map of words to counts and index
	 * @throws IOException
	 */
	public static HashMap<String, IndependentPair<Long, Long>> readWordCountLines(String path) throws IOException {
		return readWordCountLines(path,"/words");
	}
	/**
	 * from a report output path get the words
	 * @param path report output path
	 * @param ext where the words are in the path
	 * @return map of words to counts and index
	 * @throws IOException 
	 */
	public static HashMap<String, IndependentPair<Long, Long>> readWordCountLines(String path, String ext) throws IOException {
		String wordPath = path + ext;
		Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataInputStream toRead = fs.open(p);
		BufferedReader reader = new BufferedReader(new InputStreamReader(toRead,"UTF-8"));
		CSVParser csvreader = new CSVParser(reader);
		long lineN = 0;
		String[] next = null;
		HashMap<String, IndependentPair<Long, Long>> toRet = new HashMap<String, IndependentPair<Long,Long>>();
		while((next = csvreader.getLine())!=null && next.length > 0){
			toRet.put(next[0], IndependentPair.pair(Long.parseLong(next[1]), lineN));
			lineN ++;
		}
		return toRet;
	}
	@Override
	public SequenceFileTextStage<Text,BytesWritable, Text,LongWritable,NullWritable,Text> stage() {
		return new SequenceFileTextStage<Text,BytesWritable, Text,LongWritable,NullWritable,Text>() {
			@Override
			public void setup(Job job) {
				job.setSortComparatorClass(LongWritable.Comparator.class);
				job.setNumReduceTasks(1);
			}
			@Override
			public Class<? extends Mapper<Text, BytesWritable, Text,LongWritable>> mapper() {
				return WordIndex.Map.class;
			}
			@Override
			public Class<? extends Reducer<Text,LongWritable,NullWritable,Text>> reducer() {
				return WordIndex.Reduce.class;
			}
			
			@Override
			public String outname() {
				return "words";
			}
		};
	}

}
