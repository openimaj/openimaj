package org.openimaj.hadoop.tools.twitter;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;

public class TextDaySplitStage extends Stage<TextInputFormat,TextOutputFormat<NullWritable,Text>,LongWritable,Text,NullWritable,Text,NullWritable,Text> {

}
