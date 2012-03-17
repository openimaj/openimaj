package org.openimaj.hadoop.mapreduce.stage.helper;

import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;

/**
 * A helper class for a common stage type. In this case, a stage that goes from a sequence file to a sequence file with types 
 * @author ss
 * @param <INPUT_KEY> The key format of the input to the map task 
 * @param <INPUT_VALUE> The value format of the input to the map task
 * @param <OUTPUT_KEY> The key format of the output of the reduce task
 * @param <OUTPUT_VALUE> The valueformat of the output of the reduce task 
 *
 */
public abstract class SimpleSequenceFileStage<
			INPUT_KEY, INPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE
		> extends SequenceFileStage<
			INPUT_KEY, INPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE>{

}
