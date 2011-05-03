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
package org.openimaj.hadoop.tools.sequencefile.index;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;



public class SequenceFileIndexer extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		SequenceFileIndexerOptions options = new SequenceFileIndexerOptions(args);
		options.prepare();

		Job job = new Job(getConf());
		
		job.setJarByClass(SequenceFileIndexer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setMapperClass(RecordIndexMapper.class);
		
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setNumReduceTasks(1);
		
		FileInputFormat.setInputPaths(job, options.getInputPaths());
		TextOutputFormat.setOutputPath(job, options.getOutputPath());
		TextOutputFormat.setCompressOutput(job, false);
//		SequenceFileOutputFormat.setOutputCompressorClass(job, DefaultCodec.class);
//		SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);
		

		job.waitForCompletion(true);
		return 0;
	}
	public static void main(String [] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new SequenceFileIndexer(), args);
		System.exit(res);
	}
}
