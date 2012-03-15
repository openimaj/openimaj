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
	private String[] fstage;

	@Override
	public void perform(final HadoopTwitterTokenToolOptions opts) throws Exception {
		Path outpath = HadoopToolsUtil.getOutputPath(opts);
		this.stages = new MultiStagedJob(HadoopToolsUtil.getInputPaths(opts),outpath,opts.getArgs());
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
		
		stages.queueStage(new CountTweetsInTimeperiod(opts.getNonHadoopArgs()).stage());
		
		
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
		stages.queueStage(new CountWordsAcrossTimeperiod(opts.getNonHadoopArgs()).stage());
		
		stages.runAll();
		this.fstage = new String[]{outpath.toString()};
	}

	@Override
	public String[] finalOutput(HadoopTwitterTokenToolOptions opts) throws Exception {
		return this.fstage;
	}
	
}
