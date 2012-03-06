package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;


public class TimeIndex {

	/**
	 * Emits each word with the total number of times the word was seen
	 * @author ss
	 *
	 */
	public static class Map extends Mapper<Text,BytesWritable,LongWritable,LongWritable>{
		public Map() {
			// TODO Auto-generated constructor stub
		}
		public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,LongWritable,LongWritable>.Context context){
			try {
				IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
					boolean readmore = true;
					Set<Long> seenTimes = new HashSet<Long>();
					@Override
					protected Object readValue(DataInput in) throws IOException {
						if(readmore){
							WordDFIDF idf = new WordDFIDF();
							readmore = false;
							idf.readBinary(in);
							if(!seenTimes.contains(idf.timeperiod)){
								seenTimes.add(idf.timeperiod);
								try {
									context.write(new LongWritable(idf.timeperiod), new LongWritable(idf.tf));
								} catch (InterruptedException e) {
									throw new IOException("");
								}
							}	
						}
						return new Object();
					}
				});
				
			} catch (IOException e) {
				System.err.println("Couldnt read timeperiod from word: " + key);
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
				for (LongWritable count : counts) {
					StringWriter swriter = new StringWriter();
					CSVPrinter writer = new CSVPrinter(swriter);
					writer.write(new String[]{timeStr,count.toString()});
					writer.flush();
					context.write(NullWritable.get(), new Text(swriter.toString()));
					return;
				}
				
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
	public static HashMap<String, IndependentPair<Long, Long>> readTimeCountLines(String path) throws IOException {
		String wordPath = path + "/times";
		Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataInputStream toRead = fs.open(p);
		BufferedReader reader = new BufferedReader(new InputStreamReader(toRead));
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

}
