package org.openimaj.hadoop.mapreduce.stage.helper;

import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;

/**
 * A helper class for a common stage type. In this case, a stage that goes from a sequence file to a sequence file with types 
 * @author ss
 * @param <INPUT_KEY> The key format of the input to the map task 
 * @param <INPUT_VALUE> The value format of the input to the map task
 * @param <MAP_OUTPUT_KEY> The key format of the output of the map task (and therefore the input of the reduce)
 * @param <MAP_OUTPUT_VALUE> The value format of the output of the map task (and therefore the input of the reduce)
 * @param <OUTPUT_KEY> The key format of the output of the reduce task
 * @param <OUTPUT_VALUE> The valueformat of the output of the reduce task 
 *
 */
public abstract class SequenceFileStage<
			INPUT_KEY, INPUT_VALUE,
			MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE
		> extends Stage<
			SequenceFileInputFormat<INPUT_KEY, INPUT_VALUE>,
			SequenceFileOutputFormat<OUTPUT_KEY, OUTPUT_VALUE>,
			INPUT_KEY, INPUT_VALUE,
			MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE>{

}
