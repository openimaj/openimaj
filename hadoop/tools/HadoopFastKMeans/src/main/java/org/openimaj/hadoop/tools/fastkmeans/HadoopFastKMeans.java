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
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.ByteCentroidsResult;

/**
 * Approximate/Exact K-Means over Hadoop
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class HadoopFastKMeans extends Configured implements Tool {
	public static final String EXTRA_USAGE_INFO = "";
	private HadoopFastKMeansOptions options = null;
	private String[] original_args;

	public HadoopFastKMeans(String[] args) {
		this.original_args = args;
	}

	public HadoopFastKMeans() {
		this.original_args = new String[0];
	}

	@Override
	public int run(String[] args) throws Exception {
		if (options == null) {
			options = new HadoopFastKMeansOptions(args, original_args, true);
			options.prepare();
		}

		final String base = options.output;
		// Select a subset of the features
		final String inputName = new Path(options.inputs.get(0)).getName();
		String selected = options.output + "/" + inputName + "_select_" + options.nsamples;
		final URI selectOutFileURI = new Path(selected).toUri();
		if (!HadoopFastKMeansOptions.getFileSystem(selectOutFileURI).exists(new Path(selected))) {
			final SequenceFileByteImageFeatureSelector sfbis = new SequenceFileByteImageFeatureSelector(options.inputs,
					selected, options);
			selected = sfbis.getFeatures(options.nsamples);
		}

		if (options.samplesOnly)
			return 0;
		if (options.checkSampleEquality) {
			System.out.println("Checking sample equality");
			System.out.println("Using sequence file: " + selected);
			SampleEqualityChecker.checkSampleEquality(selected + "/part-r-00000", options);
			return 0;
		}

		// Select the intital centroids
		final SequenceFileByteFeatureSelector sfbs = new SequenceFileByteFeatureSelector(selected, options.output
				+ "/init", options);
		final String initialCentroids = sfbs.getRandomFeatures(options.k);

		ByteCentroidsResult cluster = AKMeans.sequenceFileToCluster(initialCentroids + "/part-r-00000", options.k);

		// at this point there might be fewer centroids than we wanted as a
		// result of having fewer features than centroids... this should
		// probably be considered to be an error.
		cluster.centroids = trimNullClusters(cluster.centroids);
		replaceSequenceFileWithCluster(initialCentroids, cluster);
		if (cluster.centroids.length < options.k) {
			System.err.println("More clusters were requested than there are features. K-Means cannot be performed.");
			replaceSequenceFileWithCluster(initialCentroids, cluster);
			replaceSequenceFileWithCluster(base + "/final", cluster);
			return 1;
		}

		// Prepare the AKM procedure
		String currentCompletePath = initialCentroids;
		for (int i = 0; i < options.iter; i++) {
			// create job...
			// set input from previous job if i!=0, otherwise use given input
			// from args
			// set output to a file/directory named using i (combined with
			// something in args)
			System.out.println("Calling iteration: " + i);
			String newOutPath = base + "/" + i;
			if (i == options.iter - 1)
				newOutPath = base + "/final";
			final Job job = TextBytesJobUtil.createJob(new Path(selected), new Path(newOutPath),
					new HashMap<String, String>(), this.getConf());
			job.setJarByClass(this.getClass());
			job.setMapperClass(MultithreadedMapper.class);
			MultithreadedMapper.setNumberOfThreads(job, options.concurrency);
			MultithreadedMapper.setMapperClass(job, AKMeans.Map.class);

			job.setCombinerClass(AKMeans.Combine.class);
			job.setReducerClass(AKMeans.Reduce.class);
			job.setOutputKeyClass(IntWritable.class);
			job.setOutputValueClass(BytesWritable.class);
			job.getConfiguration().setStrings(AKMeans.CENTROIDS_PATH, currentCompletePath);
			job.getConfiguration().setStrings(AKMeans.CENTROIDS_K, options.k + "");
			job.getConfiguration().setStrings(AKMeans.CENTROIDS_EXACT, options.exact + "");
			((JobConf) job.getConfiguration()).setNumTasksToExecutePerJvm(-1);
			job.waitForCompletion(true);

			currentCompletePath = newOutPath;
			cluster = AKMeans.completeCentroids(currentCompletePath + "/part-r-00000", selected, options);
			replaceSequenceFileWithCluster(currentCompletePath, cluster);
			cluster = null;
		}
		return 0;
	}

	static byte[][] trimNullClusters(byte[][] bytes) {
		int i = 0;
		while (i < bytes.length && bytes[i] != null) {
			i++;
		}

		return Arrays.copyOf(bytes, i);
	}

	private void replaceSequenceFileWithCluster(String sequenceFile, ByteCentroidsResult cluster) throws IOException {
		final Path p = new Path(sequenceFile);
		final FileSystem fs = HadoopFastKMeansOptions.getFileSystem(p.toUri());

		fs.delete(p, true); // Delete the sequence file of this name

		FSDataOutputStream stream = null;
		try {
			stream = fs.create(p);
			IOUtils.writeBinary(stream, cluster); // Write the cluster
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new HadoopFastKMeans(args), args);
	}

	/**
	 * 
	 * @param hfkmo
	 */
	public void setOptions(HadoopFastKMeansOptions hfkmo) {
		this.options = hfkmo;
	}
}
