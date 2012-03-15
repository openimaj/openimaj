package org.openimaj.hadoop.tools.twitter.token.mode;

import gnu.trove.TObjectIntProcedure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;
import org.openimaj.hadoop.mapreduce.StageProvider;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.utils.TimeperiodTweetCountWordCount;
import org.openimaj.hadoop.tools.twitter.utils.TweetCountWordMap;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.WriteableListBinary;

/**
 * A mapper/reducer whose purpose is to do the following:
 * function(timePeriodLength)
 * 	map input:
 * 		<timePeriod: <<tweet:#freq>,<word:#freq>,<word:#freq>,...> 
 * 	map output:
 * 	[
 * 		word: <timeperiod, tweet:#freq, word:#freq>,
 * 		word: <timeperiod, tweet:#freq, word:#freq>,
 * 		...
 * 	]
 * 	reduce input:
 * 		<word: 
 * 		[
 * 			<timeperiod, tweet:#freq, word:#freq>,
 * 			<timeperiod, tweet:#freq, word:#freq>,...
 * 		]
 * 	reduce output:
 * 		# read total tweet frequency from timeperiod -1 Ttf
 * 		# read total word tweet frequency from timeperiod -1 Twf
 * 		# read time period tweet frequency from entry tf
 * 		# read time period word frequency from entry wf
 * 		# for entry in input:
 * 		# 	(skip for time period -1)
 * 		# 	DF =  wf/tf
 * 		# 	IDF = Ttf/Twf
 * 		# 	<word: <timePeriod, DFIDF>,...>
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class CountWordsAcrossTimeperiod implements StageProvider {
	private String[] nonHadoopArgs;
	public CountWordsAcrossTimeperiod(String[] nonHadoopArgs) {
		this.nonHadoopArgs = nonHadoopArgs;
	}
	/**
	 * arg key
	 */
	public static final String ARGS_KEY = "TOKEN_ARGS";
	private static final LongWritable END_TIME = new LongWritable(-1);
	public final static String WORDCOUNT_DIR = "wordtimeperiodDFIDF";
	/**
	 * function(timePeriodLength)
	 * 	map input:
	 * 		<timePeriod: <<tweet:#freq>,<word:#freq>,<word:#freq>,...> 
	 * 	map output:
	 * 	[
	 * 		word: <timeperiod, tweet:#freq, word:#freq>,
	 * 		word: <timeperiod, tweet:#freq, word:#freq>,
	 * 		...
	 * 	]
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
	 * 
	 */
	public static class Map extends Mapper<LongWritable, BytesWritable, Text, BytesWritable> {

		/**
		 * Mapper constructor doesn't do anything (Mapper constructor doesn't give a fuck)
		 */
		public Map(){
			
		}
		private static HadoopTwitterTokenToolOptions options;

		protected static synchronized void loadOptions(Mapper<LongWritable, BytesWritable, Text, BytesWritable>.Context context) throws IOException {
			if (options == null) {
				try {
					options = new HadoopTwitterTokenToolOptions(context.getConfiguration().getStrings(ARGS_KEY));
					options.prepare();
				} catch (CmdLineException e) {
					throw new IOException(e);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}

		private HashMap<String, TimeperiodTweetCountWordCount> wordTimeMap;

		@Override
		protected void setup(Mapper<LongWritable, BytesWritable, Text, BytesWritable>.Context context) throws IOException, InterruptedException {
			loadOptions(context);
			this.wordTimeMap = new HashMap<String, TimeperiodTweetCountWordCount>();
		}

		@Override
		protected void map(final LongWritable key,BytesWritable value,final Mapper<LongWritable, BytesWritable, Text, BytesWritable>.Context context) throws java.io.IOException, InterruptedException {
			
			final TweetCountWordMap periodCountWordCount = IOUtils.read(new ByteArrayInputStream(value.getBytes()), TweetCountWordMap.class);
			boolean written = periodCountWordCount.getTweetWordMap().forEachEntry(new TObjectIntProcedure<String>() {
				
				@Override
				public boolean execute(String word, int wordCount) {
					TimeperiodTweetCountWordCount timeMap = new TimeperiodTweetCountWordCount(key.get(),wordCount,periodCountWordCount.getNTweets());
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					try {
						IOUtils.writeBinary(os, timeMap);
						BytesWritable writeable = new BytesWritable(os.toByteArray());
						context.write(new Text(word), writeable);
					} catch (IOException e) {
						return false;
					} catch (InterruptedException e) {
						return false;
					}
					
					return true;
				}
			});
			if(!written){
				throw new IOException("Couldn't write the TimeperiodTweetCountWordCount object");
			}
		}
	}
	

	/**
	 * 	reduce input:
	 * 		<word: 
	 * 		[
	 * 			<timeperiod, tweet:#freq, word:#freq>,
	 * 			<timeperiod, tweet:#freq, word:#freq>,...
	 * 		]
	 * 	reduce output:
	 * 		# 	<word: <timePeriod, DFIDF>,...>
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
	 *
	 */
	public static class Reduce extends Reducer<Text, BytesWritable, Text, BytesWritable>{
		
		/**
		 * default construct does nothing
		 */
		public Reduce(){
			
		}
		@Override
		protected void reduce(Text word, Iterable<BytesWritable> values, Reducer<Text, BytesWritable, Text, BytesWritable>.Context context) throws IOException,InterruptedException{
			// read all timeperiods to objects, find the END_TIME instance, hold the rest
			/* # read total tweet frequency from timeperiod -1 Ttf
			 * 		# read total word tweet frequency from timeperiod -1 Twf
			 * 		# read time period tweet frequency from entry tf
			 * 		# read time period word frequency from entry wf
			 */
			TimeperiodTweetCountWordCount endTime = null;
			List<TimeperiodTweetCountWordCount> otherTimes = new ArrayList<TimeperiodTweetCountWordCount>();
			for (BytesWritable inputArr : values) {
				ByteArrayInputStream stream = new ByteArrayInputStream(inputArr.getBytes());
				TimeperiodTweetCountWordCount instance = IOUtils.read(stream, TimeperiodTweetCountWordCount.class);
				if(instance.timeperiod == END_TIME.get())
					endTime = instance;
				else
					otherTimes.add(instance);
			}
			/*
			 * 	# for entry in input:
			 * 	# 	DF =  wf/tf
			 * 	# 	IDF = Ttf/Twf
			 */
			// Total number of tweets in all timeperiods
			long Ttf = endTime.tweetcount;
			// Number of tweets containing this word in all timeperiods
			long Twf = endTime.wordcount;
			TreeSet<WordDFIDF> allDFIDF = new TreeSet<WordDFIDF>();
			for (TimeperiodTweetCountWordCount tcwc : otherTimes) {
				// Number of tweets in this timeperiod
				long tf = tcwc.tweetcount;
				// Number of tweets containing this word in this time period
				long wf = tcwc.wordcount;
				
				WordDFIDF dfidf = new WordDFIDF(tcwc.timeperiod,wf,tf,Twf,Ttf);
				allDFIDF.add(dfidf);
			}
			List<WordDFIDF> listVersion = new ArrayList<WordDFIDF>();
			listVersion.addAll(allDFIDF);
			WriteableListBinary<WordDFIDF> writeableCollection = new WriteableListBinary<WordDFIDF>(listVersion){
				@Override
				protected void writeValue(WordDFIDF v, DataOutput out)throws IOException {
					v.writeBinary(out);
				}
				
			};
			context.write(word, new BytesWritable(IOUtils.serialize(writeableCollection)));
		}
	}




	@Override
	public Stage stage() {
		return new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output, Configuration conf) throws IOException {
				Job job = new Job(conf);
				
				job.setInputFormatClass(SequenceFileInputFormat.class);
				job.setOutputKeyClass(Text.class);
				job.setOutputValueClass(BytesWritable.class);
				job.setOutputFormatClass(SequenceFileOutputFormat.class);
				
				job.setJarByClass(this.getClass());
			
				SequenceFileInputFormat.setInputPaths(job, inputs);
				SequenceFileOutputFormat.setOutputPath(job, output);
				SequenceFileOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(CountWordsAcrossTimeperiod.Map.class);
				job.setReducerClass(CountWordsAcrossTimeperiod.Reduce.class);
				job.getConfiguration().setStrings(CountWordsAcrossTimeperiod.ARGS_KEY, nonHadoopArgs);
				return job;
			}
			
			@Override
			public String outname() {
				return WORDCOUNT_DIR;
			}
		};
	}
}