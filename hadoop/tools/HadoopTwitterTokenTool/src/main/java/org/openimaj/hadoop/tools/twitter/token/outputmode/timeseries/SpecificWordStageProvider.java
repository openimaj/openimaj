package org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries;

import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.Stage;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileStage;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;

/**
 * Given a set of specific words, this a mapper is prepared which emits only the dfidf values of those specific words
 * and a reducer is created which emits a serialised time series for each word.
 * @author ss
 *
 */
public class SpecificWordStageProvider extends StageProvider {

	protected static final String WORD_TIME_SERIES = "org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries.wts";
	public static final String SPECIFIC_WORD = "specificword";
	private List<String> wordtimeseries;

	public SpecificWordStageProvider(List<String> wordtimeseries) {
		this.wordtimeseries = wordtimeseries;
	}

	@Override
	public SequenceFileTextStage<Text, BytesWritable, Text, BytesWritable, NullWritable, Text> stage() {
		SequenceFileTextStage<Text,BytesWritable, Text, BytesWritable, NullWritable,Text> writeSpecificWords = new SequenceFileTextStage<Text,BytesWritable, Text, BytesWritable,NullWritable,Text>() {
			@Override
			public void setup(Job job) {
				job.getConfiguration().setStrings(WORD_TIME_SERIES, wordtimeseries.toArray(new String[wordtimeseries.size()]));
			}
			
			@Override
			public Class<? extends Mapper<Text, BytesWritable, Text, BytesWritable>> mapper() {
				return SpecificWordSelectionMapper.class;
			}
			
			@Override
			public Class<? extends Reducer<Text, BytesWritable, NullWritable, Text>> reducer() {
				return WordDFIDFTimeSeriesReducer.class;
			}
			
			@Override
			public String outname() {
				return SPECIFIC_WORD;
			}
		};
		return writeSpecificWords;
	}

}
