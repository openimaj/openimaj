package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import gnu.trove.TLongObjectHashMap;
import gnu.trove.TObjectLongHashMap;
import gnu.trove.TObjectLongProcedure;

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
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;
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
		public void reduce(LongWritable count, Iterable<Text> words, final Reducer<LongWritable,Text,NullWritable,Text>.Context context){
			try {
				TObjectLongHashMap<String> wordcount = new TObjectLongHashMap<String>();
				String countStr = count.toString();
				long countL = count.get();
				for (Text text : words) {
					String word = text.toString();
					wordcount.adjustOrPutValue(word, countL, countL);
				}
				boolean worked = wordcount.forEachEntry(new TObjectLongProcedure<String>(){

					@Override
					public boolean execute(String t, long l) {
						StringWriter swriter = new StringWriter();
						CSVPrinter writer = new CSVPrinter(swriter);
						try {
							writer.write(new String[]{t,l + ""});
							writer.flush();
							context.write(NullWritable.get(), new Text(swriter.toString()));
						} catch (Exception e) {
							return false;
						}
						return true;
					}
					
				});
				if(!worked) throw new IOException("Failed to reduce ");
			} catch (Exception e) {
				System.err.println("Couldn't reduce to final file");
			}
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
	public SequenceFileTextStage<Text,BytesWritable, LongWritable,Text,NullWritable,Text> stage() {
		return new SequenceFileTextStage<Text,BytesWritable, LongWritable,Text,NullWritable,Text>() {
			@Override
			public void setup(Job job) {
				job.setSortComparatorClass(LongWritable.Comparator.class);
				job.setNumReduceTasks(1);
			}
			@Override
			public Class<? extends Mapper<Text, BytesWritable, LongWritable, Text>> mapper() {
				return WordIndex.Map.class;
			}
			@Override
			public Class<? extends Reducer<LongWritable, Text,NullWritable,Text>> reducer() {
				return WordIndex.Reduce.class;
			}
			
			@Override
			public String outname() {
				return "words";
			}
		};
	}

}
