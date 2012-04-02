/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.hadoop.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
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
