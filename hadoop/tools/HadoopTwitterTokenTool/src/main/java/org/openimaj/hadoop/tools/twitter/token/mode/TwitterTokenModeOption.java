package org.openimaj.hadoop.tools.twitter.token.mode;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;

/**
 * A twitter tweet token counting mode
 * 
 * @author ss
 *
 */
public enum TwitterTokenModeOption implements CmdLineOptionsProvider{
	/**
	 * Calculates DF-IDF for each term as described by: "Event Detection in Twitter" by J. Weng et. al. 
	 */
	DFIDF {
		@Override
		public void perform(HadoopTwitterTokenToolOptions opts) {
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
		}
	};

	@Override
	public Object getOptions() {
		return this;
	}

	/**
	 * @param opts
	 */
	public abstract void perform(HadoopTwitterTokenToolOptions opts);
	
	
}
