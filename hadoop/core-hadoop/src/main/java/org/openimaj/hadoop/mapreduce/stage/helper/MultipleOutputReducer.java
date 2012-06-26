package org.openimaj.hadoop.mapreduce.stage.helper;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <MAP_OUTPUT_KEY>
 * @param <MAP_OUTPUT_VALUE>
 * @param <OUTPUT_KEY>
 * @param <OUTPUT_VALUE>
 */
public abstract class MultipleOutputReducer<MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE, OUTPUT_KEY, OUTPUT_VALUE> extends 
	Reducer<MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE, OUTPUT_KEY, OUTPUT_VALUE>{
	
	protected MultipleOutputs<OUTPUT_KEY, OUTPUT_VALUE> multiOut;

	@Override
	protected void setup(Reducer<MAP_OUTPUT_KEY,MAP_OUTPUT_VALUE,OUTPUT_KEY,OUTPUT_VALUE>.Context context) throws IOException ,InterruptedException {
		this.multiOut = new MultipleOutputs<OUTPUT_KEY,OUTPUT_VALUE>(context);
	};
}
