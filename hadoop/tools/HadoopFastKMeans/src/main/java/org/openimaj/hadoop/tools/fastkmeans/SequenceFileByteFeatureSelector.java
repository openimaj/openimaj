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
package org.openimaj.hadoop.tools.fastkmeans;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;


public class SequenceFileByteFeatureSelector extends Configured implements Tool  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5976796322589912944L;
	private String inputFilePath;
	private String outputFilePath;
	private int nRandomRows;
	private HadoopFastKMeansOptions options;
	
	public SequenceFileByteFeatureSelector(String inputFilePath, String outputFilePath, HadoopFastKMeansOptions options) throws IOException, InterruptedException, ClassNotFoundException{
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
		this.options = options;
	}

	public String getRandomFeatures(int k) throws Exception {
		this.nRandomRows = k;
		ToolRunner.run(this, options.original_args);
        return this.outputFilePath;
	}

	@Override
	public int run(String[] args) throws Exception {
		// Create the output path
		Path outpath = new Path(SequenceFileUtility.convertToURI(this.outputFilePath).toString());
	    System.out.println("It is all going to: " + outpath);
		
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(inputFilePath, "part");
        
        Job job = new Job(this.getConf(), "featureselect");
        job.setNumReduceTasks(1);
        job.setJarByClass(SequenceFileByteImageFeatureSelector.class); 
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(BytesWritable.class);
        
        job.setMapperClass(FeatureSelect.Map.class);
        job.setReducerClass(FeatureSelect.Reduce.class);
        
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        
        job.getConfiguration().setStrings(FeatureSelect.FILETYPE_KEY, new String[]{options.fileType});
		job.getConfiguration().setStrings(FeatureSelect.NFEATURE_KEY, new String[]{"" + this.nRandomRows});
		
		((JobConf)job.getConfiguration()).setNumTasksToExecutePerJvm(-1);
		
		SequenceFileInputFormat.setInputPaths(job, sequenceFiles);
	    SequenceFileOutputFormat.setOutputPath(job, outpath);
		SequenceFileOutputFormat.setCompressOutput(job, false);
        job.waitForCompletion(true);
		return 0;
	}

}
