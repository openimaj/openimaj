package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.mapreduce.stage.Stage;
import org.openimaj.hadoop.mapreduce.stage.StageAppender;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileStage;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;

/**
 * Count word instances (not occurences) across times. Allows for investigation of how
 * the vocabulary has changed over time.
 * 
 * @author ss
 *
 */
public class SingleReducerTimeWord extends SequenceFileTextStage<Text, BytesWritable, LongWritable, Text, NullWritable, Text>{
	@Override
	public void setup(Job job) {
		job.setNumReduceTasks(1);
	}
	@Override
	public Class<? extends Mapper<Text, BytesWritable, LongWritable, Text>> mapper() {
		return TimeWordMapper.class;
	}
	
	@Override
	public Class<? extends Reducer<LongWritable, Text, NullWritable, Text>> reducer() {
		return CumulativeJacardReducer.class;
	}
	@Override
	public String outname() {
		return "jacardindex";
	}
}
