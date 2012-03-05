package org.openimaj.hadoop.tools.twitter.token.mode.dfidf;

import java.io.DataInput;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountTweetsInTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

/**
 * Perform DFIDF and output such that each timeslot is a instance and each word a feature
 * @author ss
 *
 */
public class DFIDFTokenMode implements TwitterTokenMode {

	private MultiStagedJob stages;
	private Path fstage;

	@Override
	public void perform(final HadoopTwitterTokenToolOptions opts) throws Exception {
		this.stages = new MultiStagedJob(HadoopToolsUtil.getInputPaths(opts),HadoopToolsUtil.getOutputPath(opts));
		/*
		*			Multi stage DF-IDF process:
		*				Calculate DF for a word in a time period (t) = number of tweets with word in time period (t) / number of tweets in time period (t)
		*				Calculate IDF = number of tweets up to final time period (T) / number of tweets with word up to time period (T)
		*
		*				function(timePeriodLength)
		*				So a word in a tweet can happen in the time period between t - 1 and t.
		*				First task:
		*					map input:
		*						tweetstatus # json twitter status with JSONPath to words
		*					map output:
		*						<timePeriod: <word:#freq,tweets:#freq>, -1:<word:#freq,tweets:#freq> > 
		*					reduce input:
		*						<timePeriod: [<word:#freq,tweets:#freq>,...,<word:#freq,tweets:#freq>]> 
		*					reduce output:
		*						<timePeriod: <<tweet:#freq>,<word:#freq>,<word:#freq>,...>
		*/
		stages.queueStage(new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output) throws IOException {
				Job job = new Job(new Configuration());
				
				job.setInputFormatClass(TextInputFormat.class);
				job.setOutputKeyClass(LongWritable.class);
				job.setOutputValueClass(BytesWritable.class);
				job.setOutputFormatClass(SequenceFileOutputFormat.class);
				job.setJarByClass(this.getClass());
			
				TextInputFormat.setInputPaths(job, inputs);
				SequenceFileOutputFormat.setOutputPath(job, output);
				SequenceFileOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(CountTweetsInTimeperiod.Map.class);
				job.setReducerClass(CountTweetsInTimeperiod.Reduce.class);
				job.getConfiguration().setStrings(CountTweetsInTimeperiod.ARGS_KEY, opts.getArgs());
				return job;
			}
			
			@Override
			public String outname() {
				return "timeperiodTweet";
			}
		});
		
		
		/*
		*
		*				Second task:
		*					map input:
		*						<timePeriod: <<tweet:#freq>,<word:#freq>,<word:#freq>,...> 
		*					map output:
		*						[
		*							word: <timeperiod, tweet:#freq, word:#freq>,
		*							word: <timeperiod, tweet:#freq, word:#freq>,
		*							...
		*						]
		*					reduce input:
		*						<word: [
		* 								<timeperiod, tweet:#freq, word:#freq>,
		*								<timeperiod, tweet:#freq, word:#freq>,...
		*						]
		*					reduce output:
		*						# read total tweet frequency from timeperiod -1 Ttf
		*						# read total word tweet frequency from timeperiod -1 Twf
		*						# read time period tweet frequency from entry tf
		*						# read time period word frequency from entry wf
		*						# for entry in input:
		*						#	(skip for time period -1)
		*						# 	DF =  wf/tf
		*						# 	IDF = Ttf/Twf
		*						# 	<word: <timePeriod, DFIDF>,...>
		*/
		stages.queueStage(new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output) throws IOException {
				Job job = new Job(new Configuration());
				
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
				job.getConfiguration().setStrings(CountWordsAcrossTimeperiod.ARGS_KEY, opts.getArgs());
				return job;
			}
			
			@Override
			public String outname() {
				return "wordtimeperiodDFIDF";
			}
		});
		
		this.fstage = stages.runAll();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void output(HadoopTwitterTokenToolOptions opts) throws Exception {
		if(this.fstage == null) throw new IOException("Output not created yet!");
		TwitterTokenOutputMode output = opts.outputMode();
		// Use the sequence file utility to read in the generated sequence file in fstage.
		URI uri = this.fstage.toUri();
		Configuration config = new Configuration();
		// feed each entry to the
		try {
			FileSystem fs = SequenceFileUtility.getFileSystem(uri,config);
			Path[] paths = SequenceFileUtility.getFilePaths(this.fstage.toString(), "part");
			for(Path path : paths){
				Reader reader = null;
				reader = new Reader(fs, path, config);
				Text key = ReflectionUtils.newInstance((Class<Text>) reader.getKeyClass(), config);
				BytesWritable val = ReflectionUtils.newInstance((Class<BytesWritable>)reader.getValueClass(), config);
				while (reader.next(key, val)) {
					String word = key.toString();
					List<IndependentPair<String,Double>> timeIDF = new ArrayList<IndependentPair<String,Double>>();
					IOUtils.deserialize(val.getBytes(), new ReadableListBinary<IndependentPair<String,Double>>(timeIDF){
						@Override
						protected IndependentPair<String,Double> readValue(DataInput in) throws IOException {
							WordDFIDF idf = new WordDFIDF();
							idf.readBinary(in);
							return IndependentPair.pair(idf.timeperiod + "", idf.dfidf());
						}
					});
					output.acceptFeat(word, timeIDF);
				}
			}
		}	
		catch(Exception e ){
			e.printStackTrace();
		}
	}
	
}
