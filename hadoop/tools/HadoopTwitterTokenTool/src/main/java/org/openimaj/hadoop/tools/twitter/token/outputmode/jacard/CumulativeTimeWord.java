package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
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
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;
import org.openimaj.hadoop.mapreduce.StageProvider;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;

/**
 * Count word instances (not occurences) across times. Allows for investigation of how
 * the vocabulary has changed over time.
 * 
 * @author ss
 *
 */
public class CumulativeTimeWord implements StageProvider{
	private long timeDelta;
	
	/**
	 * @param timeDelta the delta between times
	 * @param timeEldest the eldest time
	 */
	public CumulativeTimeWord(long timeDelta, long timeEldest) {
		this.timeDelta = timeDelta;
		this.timeEldest = timeEldest;
	}
	private long timeEldest;
	/**
	 * For every word occurrence, emit <word,true> for its time period, and <word,false> for every time period from
	 * timePeriod + delta until eldestTime. The final time period should be comparing itself to every word ever emitted.
	 * 
	 * This is in the order of 2 million unique words per day which should still be not THAT many words. hopefully.
	 * @author ss
	 */
	public static class Map extends Mapper<Text,BytesWritable,LongWritable,BytesWritable>{
		private long eldestTime;
		private long deltaTime;
		
		public Map() { }
		protected void setup(Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
			this.eldestTime = context.getConfiguration().getLong(TIME_ELDEST, -1);
			this.deltaTime = context.getConfiguration().getLong(TIME_DELTA, -1);
			if(eldestTime < 0 || deltaTime < 0){
				throw new IOException("Couldn't read reasonable time configurations");
			}
		};
		protected void map(final Text word, BytesWritable value, final Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws java.io.IOException ,InterruptedException {
			IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
				@Override
				protected Object readValue(DataInput in) throws IOException {
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(in);
					try {
						String currentword = word.toString();
						ReadWritableStringBoolean timePair = new ReadWritableStringBoolean(Map.this, currentword, true);
						context.write(new LongWritable(idf.timeperiod), new BytesWritable(IOUtils.serialize(timePair)));
						for (long futureTime = idf.timeperiod + deltaTime; futureTime <= eldestTime; futureTime+=deltaTime) {
							ReadWritableStringBoolean futurePair = new ReadWritableStringBoolean(Map.this, currentword, false);
							context.write(new LongWritable(futureTime), new BytesWritable(IOUtils.serialize(futurePair)));
						}
					} catch (InterruptedException e) {
						throw new IOException("");
					}
					return new Object();
				}
			});
		};
	}
	
	/**
	 * Recieve every time period with a list of words either from the current time period or from past time periods.
	 * Construct a union set and intersection set of all words.
	 * 
	 * emit the time period with the length of the union set, the length of the intersection set and the ratio of these two (The Jacard Index)
	 * 
	 * @author ss
	 *
	 */
	public static class Reduce extends Reducer<LongWritable,BytesWritable,NullWritable,Text>{
		public Reduce() {}
		protected void reduce(LongWritable time, Iterable<BytesWritable> wordBools, Reducer<LongWritable,BytesWritable,NullWritable,Text>.Context context) throws IOException ,InterruptedException {
			Set<String> union = new HashSet<String>();
			Set<String> intersection = new HashSet<String>();
			Set<String> historic = new HashSet<String>();
			for (BytesWritable bWordBool : wordBools) {
				ReadWritableStringBoolean wordBool = IOUtils.deserialize(bWordBool.getBytes(), ReadWritableStringBoolean.class);
				union.add(wordBool.firstObject());
				if(wordBool.secondObject()){
					intersection.add(wordBool.firstObject());
				}
				else{
					historic.add(wordBool.firstObject());
				}
			}
			if(intersection.size() == 0){
				// No words actually emitted at this time, skip!
				return;
			}
			long historicWords = historic.size();
			long currentWords = intersection.size();
			
			intersection.retainAll(historic);
			StringWriter swriter = new StringWriter();
			JacardIndex jacardIndex = new JacardIndex(time.get(),currentWords,historicWords,intersection.size(),union.size());
			IOUtils.writeASCII(swriter, jacardIndex);
//			CSVPrinter writer = new CSVPrinter(swriter);
//			writer.write(new String[]{
//					"" + currentWords,
//					"" + historicWords,
//					"" + intersection.size(),
//					"" + union.size(),
//					"" + (double)intersection.size() / (double)union.size()
//			});
//			writer.flush();
			context.write(NullWritable.get(), new Text(swriter.toString()));
		};
	}
	
	protected static final String TIME_DELTA = "org.openimaj.hadoop.tools.twitter.token.time_delta";
	protected static final String TIME_ELDEST = "org.openimaj.hadoop.tools.twitter.token.time_eldest";
	@Override
	public Stage stage() {
		Stage s = new Stage() {

			@Override
			public Job stage(Path[] inputs, Path output, Configuration conf) throws IOException {
				Job job = new Job(conf);
				
				job.setInputFormatClass(SequenceFileInputFormat.class);
				job.setOutputKeyClass(LongWritable.class);
				job.setOutputValueClass(BytesWritable.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				job.setJarByClass(this.getClass());
			
				SequenceFileInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(CumulativeTimeWord.Map.class);
				job.setReducerClass(CumulativeTimeWord.Reduce.class);
				job.getConfiguration().setLong(CumulativeTimeWord.TIME_DELTA, timeDelta);
				job.getConfiguration().setLong(CumulativeTimeWord.TIME_ELDEST, timeEldest);
				job.setNumReduceTasks((int) (1.75 * 6));
				return job;
			}
			
			@Override
			public String outname() {
				return "jacardindex";
			}
		};
		return s;
	}
	
	/**
	 * from a report output path get the words
	 * @param path report output path
	 * @return map of time to an a pair containing <count, JacardIndex> 
	 * @throws IOException 
	 */
	public static LinkedHashMap<Long, JacardIndex> readTimeCountLines(String path) throws IOException {
		String wordPath = path + "/jacardindex";
		Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataInputStream toRead = fs.open(p);
		BufferedReader reader = new BufferedReader(new InputStreamReader(toRead));
		LinkedHashMap<Long, JacardIndex> toRet = new LinkedHashMap<Long, JacardIndex>();
		String next = null;
		while((next = reader.readLine())!=null){
			JacardIndex jindex = JacardIndex.fromString(next);
			toRet.put(jindex.time, jindex);
		}
		return toRet;
	}

}
