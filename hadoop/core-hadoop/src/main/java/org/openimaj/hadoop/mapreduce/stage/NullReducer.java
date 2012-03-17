package org.openimaj.hadoop.mapreduce.stage;

import org.apache.hadoop.mapreduce.Reducer;

public class NullReducer<MAP_OUTPUT_KEY,MAP_OUTPUT_VALUE,OUTPUT_KEY,OUTPUT_VALUE> extends Reducer<MAP_OUTPUT_KEY,MAP_OUTPUT_VALUE,OUTPUT_KEY,OUTPUT_VALUE> {
	public NullReducer() {
	}
}
