package org.openimaj.hadoop.mapreduce.stage.helper;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;

/**
 * A helper class for a common stage type. In this case, a stage that goes from text to a sequence file of bytes indexed by longs
 * @author ss
 *
 */
public abstract class TextLongByteStage extends TextSequenceFileStage<
			LongWritable,BytesWritable,
			LongWritable,BytesWritable>{

}
