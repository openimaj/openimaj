package org.openimaj.hadoop.tools.twitter.token.mode.match;

import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.mapreduce.stage.Stage;
import org.openimaj.hadoop.mapreduce.stage.helper.TextStage;
import org.openimaj.hadoop.tools.twitter.token.mode.CountTweetsInTimeperiod;


/**
 * If tokens in set of terms spit out the whole line, otherwise ignore.
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TokenRegexStage extends TextStage{
	
	/**
	 * The key where regexs are stored
	 */
	public static final String REGEX_KEY = "org.openimaj.hadoop.tools.twitter.token.mode.match.regex";
	/**
	 * The output folder name used returned by {@link Stage#outname()}
	 */
	public static final String OUT_NAME = "tokenmatch";

	private List<String> tomatch;
	private String[] args;
	
	@Override
	public void setup(Job job) {
		job.getConfiguration().setStrings(REGEX_KEY, tomatch.toArray(new String[tomatch.size()]));
		job.getConfiguration().setStrings(CountTweetsInTimeperiod.ARGS_KEY, this.args);
	}
	/**
	 * @param rstrings the list of regexes to match
	 */
	public TokenRegexStage(List<String> rstrings, String[] args) {
		this.tomatch = rstrings;
		this.args = args;
	}
	
	@Override
	public Class<? extends Mapper<LongWritable, Text, NullWritable, Text>> mapper() {
		return TokenRegexMapper.class;
	}
	
	@Override
	public String outname() {
		return OUT_NAME;
	}

}
