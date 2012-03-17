package org.openimaj.hadoop.mapreduce.stage.helper;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;

/**
 * A helper class for a common stage type. In this case, a stage that goes from text to a sequence file of bytes indexed by longs
 * @author ss
 *
 */
public abstract class TextLongByteStage extends Stage<
			TextInputFormat,
			SequenceFileOutputFormat<LongWritable,BytesWritable>,
			LongWritable, Text,
			LongWritable, BytesWritable,
			LongWritable,BytesWritable>{

}
