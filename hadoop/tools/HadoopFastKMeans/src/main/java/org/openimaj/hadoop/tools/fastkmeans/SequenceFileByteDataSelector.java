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

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;


public class SequenceFileByteDataSelector {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5976796322589912944L;
	private String sequenceFilePath;
	private String fileType;
	
	public SequenceFileByteDataSelector(String sequenceFilePath, String fileType) throws IOException, InterruptedException, ClassNotFoundException{
		
		this.sequenceFilePath = sequenceFilePath;
		this.fileType = fileType;
		
//		File tmpFile = File.createTempFile("feature",".count");
//		tmpFile.delete();
//		Path outpath = new Path(SequenceFileUtility.convertToURI(tmpFile.getAbsolutePath()).toString());
//	    System.out.println("It is all going to: " + outpath);
//		
//		
//		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(sequenceFilePath, "part");
//		Configuration conf = new Configuration();
//        
//        Job job = new Job(conf, "featurecount");
//        job.setNumReduceTasks(1);
//        job.setJarByClass(SequenceFileByteDataSelector.class); 
//        job.setOutputKeyClass(Text.class);
//        job.setOutputValueClass(IntWritable.class);
//        job.setMapperClass(FeatureCount.Map.class);
//        job.setCombinerClass(FeatureCount.Reduce.class); 
//        job.setReducerClass(FeatureCount.Reduce.class);
//		
//		job.setInputFormatClass(SequenceFileInputFormat.class);
//		job.setOutputFormatClass(TextOutputFormat.class);
//		
//		job.getConfiguration().setStrings(FeatureCount.FILETYPE_KEY, new String[]{fileType});
//		
//		SequenceFileInputFormat.setInputPaths(job, sequenceFiles);
//	    FileOutputFormat.setOutputPath(job, outpath);
//	    
//        job.waitForCompletion(true);
//        totalRecords = getTotalFromFile(tmpFile);
//		System.out.println("... Total records was: " + totalRecords);
	}

//	private int getTotalFromFile(File tmpFile) throws NumberFormatException, IOException {
//		// Get the part file
//		File[] fs = tmpFile.listFiles(new FileFilter(){ @Override public boolean accept(File arg0) {return arg0.getName().startsWith("part");} });
//		File f = fs[0];
//		BufferedReader reader = new BufferedReader(new FileReader(f));
//		return Integer.parseInt(reader.readLine().split("\t")[1]);
//	}

	public Path getRandomRows(int k) throws IOException, InterruptedException, ClassNotFoundException {
		// Create the output path
		File tmpFile = File.createTempFile("feature",".select");
		tmpFile.delete();
		Path outpath = new Path(SequenceFileUtility.convertToURI(tmpFile.getAbsolutePath()).toString());
	    System.out.println("It is all going to: " + outpath);
		
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(sequenceFilePath, "part");
		Configuration conf = new Configuration();
        
        Job job = new Job(conf, "featureselect");
        job.setNumReduceTasks(1);
        job.setJarByClass(SequenceFileByteDataSelector.class); 
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(BytesWritable.class);
        
        job.setMapperClass(FeatureSelect.Map.class);
//        job.setCombinerClass(FeatureSelect.Reduce.class); 
        job.setReducerClass(FeatureSelect.Reduce.class);
        
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
//        job.setOutputFormatClass(TextOutputFormat.class);
        
        job.getConfiguration().setStrings(FeatureSelect.FILETYPE_KEY, new String[]{fileType});
		job.getConfiguration().setStrings(FeatureSelect.NFEATURE_KEY, new String[]{"" + k});
		
		SequenceFileInputFormat.setInputPaths(job, sequenceFiles);
	    SequenceFileOutputFormat.setOutputPath(job, outpath);
		SequenceFileOutputFormat.setCompressOutput(job, false);
//		FileOutputFormat.setOutputPath(job, outpath);
        job.waitForCompletion(true);
        return outpath;
		
	}
}
