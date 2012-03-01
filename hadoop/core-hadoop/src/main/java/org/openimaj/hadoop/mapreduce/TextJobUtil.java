package org.openimaj.hadoop.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;



public class TextJobUtil {
	public static Job createJob(Path [] inputPaths, Path outputPath) throws IOException {
		return createJob(inputPaths,outputPath, new Configuration());
	}
	
	public static Job createJob(Path [] inputPaths, Path outputPath, Configuration configuration) throws IOException {
		return createJob(inputPaths,outputPath,configuration,TextInputFormat.class,TextOutputFormat.class,NullWritable.class,Text.class,false);
	}
	/**
	 * @param inputPaths
	 * @param outputPath
	 * @param configuration
	 * @param inputFormat
	 * @param outpuFormat
	 * @param outputKey
	 * @param outputValue
	 * @param compressed
	 * @return
	 * @throws IOException
	 */
	public static Job createJob(Path [] inputPaths, Path outputPath, Configuration configuration,
			Class<? extends InputFormat> inputFormat,Class<? extends OutputFormat> outpuFormat,Class<?> outputKey, Class<?> outputValue,
			boolean compressed
	) throws IOException {
		Job job = new Job(configuration);
		job.setInputFormatClass( inputFormat);
		job.setOutputKeyClass(outputKey);
		job.setOutputValueClass(outputValue);
		job.setOutputFormatClass(outpuFormat);
	
		TextInputFormat.setInputPaths(job, inputPaths);
		TextOutputFormat.setOutputPath(job, outputPath);
		TextOutputFormat.setCompressOutput(job, compressed);
		
		return job;
	}
	
}
