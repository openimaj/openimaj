package org.openimaj.hadoop.mapreduce.stage.helper;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;

import com.hadoop.mapreduce.LzoTextInputFormat;

/**
 * A helper class for a common stage type. In this case, a stage that goes from a sequence file to a sequence file with types 
 * @author ss
 * @param <MAP_OUTPUT_KEY> The key format of the output of the map task (and therefore the input of the reduce)
 * @param <MAP_OUTPUT_VALUE> The value format of the output of the map task (and therefore the input of the reduce)
 * @param <OUTPUT_KEY> The key format of the output of the reduce task
 * @param <OUTPUT_VALUE> The valueformat of the output of the reduce task 
 *
 */
public abstract class TextSequenceFileStage<
			MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE
		> extends Stage<
			TextInputFormat,
			SequenceFileOutputFormat<OUTPUT_KEY, OUTPUT_VALUE>,
			LongWritable, Text,
			MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE>{

}
