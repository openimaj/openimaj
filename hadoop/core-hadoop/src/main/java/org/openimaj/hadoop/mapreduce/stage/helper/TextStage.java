package org.openimaj.hadoop.mapreduce.stage.helper;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;

import com.hadoop.mapreduce.LzoTextInputFormat;

/**
 * A stage that takes text in and spits text out
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public abstract class TextStage extends Stage<TextInputFormat,TextOutputFormat<NullWritable,Text>,LongWritable,Text,NullWritable,Text,NullWritable,Text>{

}
