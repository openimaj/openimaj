package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileStage;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.hadoop.mapreduce.stage.helper.SimpleSequenceFileStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.token.mode.CountTweetsInTimeperiod;
import org.openimaj.hadoop.tools.twitter.utils.TweetCountWordMap;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;


public class TimeIndex extends StageProvider{

	/**
	 * Emits each word with the total number of times the word was seen
	 * @author ss
	 *
	 */
	public static class Map extends Mapper<LongWritable,BytesWritable,LongWritable,LongWritable>{
		public Map() {
			// TODO Auto-generated constructor stub
		}
		public void map(final LongWritable key, BytesWritable value, final Mapper<LongWritable,BytesWritable,LongWritable,LongWritable>.Context context){
			try {
				final TweetCountWordMap periodCountWordCount = IOUtils.read(new ByteArrayInputStream(value.getBytes()), TweetCountWordMap.class);
				if(!key.equals(CountTweetsInTimeperiod.Map.END_TIME)){
					context.write(key, new LongWritable(periodCountWordCount.getNTweets()));
				}
				
			} catch (Exception e) {
				System.err.println("Couldnt read timeperiod: " + key);
			}
		}
	}
	/**
	 * Writes each word,count
	 * @author ss
	 *
	 */
	public static class Reduce extends Reducer<LongWritable,LongWritable,NullWritable,Text>{
		public Reduce() {
			// TODO Auto-generated constructor stub
		}
		public void reduce(LongWritable timeslot, Iterable<LongWritable> counts, Reducer<LongWritable,LongWritable,NullWritable,Text>.Context context){
			try {
				String timeStr = timeslot.toString();
				long total = 0;
				for (LongWritable count : counts) {
					total += count.get();
				}
				StringWriter swriter = new StringWriter();
				CSVPrinter writer = new CSVPrinter(swriter);
				writer.write(new String[]{timeStr,total + ""});
				writer.flush();
				String toWrote = swriter.toString();
				System.out.println("Line being written: '" + toWrote +"'");
				context.write(NullWritable.get(), new Text(toWrote));
				return;
				
			} catch (Exception e) {
				System.err.println("Couldn't reduce to final file");
			}
		}
	}
	
	/**
	 * from a report output path get the words
	 * @param path report output path
	 * @return map of time to an a pair containing <count, lineindex> 
	 * @throws IOException 
	 */
	public static LinkedHashMap<Long, IndependentPair<Long, Long>> readTimeCountLines(String path) throws IOException {
		String wordPath = path + "/times";
		Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataInputStream toRead = fs.open(p);
		BufferedReader reader = new BufferedReader(new InputStreamReader(toRead));
		CSVParser csvreader = new CSVParser(reader);
		long lineN = 0;
		String[] next = null;
		LinkedHashMap<Long, IndependentPair<Long, Long>> toRet = new LinkedHashMap<Long, IndependentPair<Long,Long>>();
		while((next = csvreader.getLine())!=null && next.length > 0){
			toRet.put(Long.parseLong(next[0]), IndependentPair.pair(Long.parseLong(next[1]), lineN));
			lineN ++;
		}
		return toRet;
	}

	@Override
	public SequenceFileTextStage<LongWritable,BytesWritable, LongWritable,LongWritable,NullWritable,Text>stage() {
		return new SequenceFileTextStage<LongWritable,BytesWritable, LongWritable,LongWritable,NullWritable,Text>() {
			
			@Override
			public void setup(Job job) {
				job.setSortComparatorClass(LongWritable.Comparator.class);
				job.setNumReduceTasks(1);
			}
			@Override
			public Class<? extends Mapper<LongWritable, BytesWritable, LongWritable, LongWritable>> mapper() {
				return TimeIndex.Map.class;
			}
			@Override
			public Class<? extends Reducer<LongWritable, LongWritable,NullWritable,Text>> reducer() {
				return TimeIndex.Reduce.class;
			}
			
			@Override
			public String outname() {
				return "times";
			}
		};
	}

}
