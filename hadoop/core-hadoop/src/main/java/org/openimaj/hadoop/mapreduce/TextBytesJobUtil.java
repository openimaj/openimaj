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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.openimaj.hadoop.sequencefile.MetadataConfiguration;
import org.openimaj.hadoop.sequencefile.MetadataSequenceFileOutputFormat;

/**
 * Utility methods for creating {@link Job}s that injest and output {@link Text}
 * keys and {@link BytesWritable} values.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TextBytesJobUtil {
	public static Job createJob(String inputPath, String outputPath, Map<String, String> metadata, Configuration config)
			throws IOException
	{
		return createJob(new Path(inputPath), new Path(outputPath), metadata, config);
	}

	public static Job createJob(Collection<String> inputPaths, String outputPath, Map<String, String> metadata,
			Configuration config) throws IOException
	{
		final List<Path> paths = new ArrayList<Path>();

		for (final String s : inputPaths)
			paths.add(new Path(s));

		return createJob(paths, new Path(outputPath), metadata, config);
	}

	public static Job createJob(Path inputPath, Path outputPath, Map<String, String> metadata, Configuration config)
			throws IOException
	{
		return createJob(new Path[] { inputPath }, outputPath, metadata, config);
	}

	public static Job createJob(Collection<Path> inputPaths, Path outputPath, Map<String, String> metadata,
			Configuration config) throws IOException
	{
		return createJob(inputPaths.toArray(new Path[inputPaths.size()]), outputPath, metadata, config);
	}

	public static Job createJob(Path[] inputPaths, Path outputPath, Map<String, String> metadata, Configuration config)
			throws IOException
	{
		final Job job = new Job(config);

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(BytesWritable.class);
		job.setOutputFormatClass(MetadataSequenceFileOutputFormat.class);

		SequenceFileInputFormat.setInputPaths(job, inputPaths);
		SequenceFileOutputFormat.setOutputPath(job, outputPath);
		SequenceFileOutputFormat.setCompressOutput(job, true);
		SequenceFileOutputFormat.setOutputCompressorClass(job, DefaultCodec.class);
		SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);

		if (metadata != null)
			MetadataConfiguration.setMetadata(metadata, job.getConfiguration());

		return job;
	}
}
