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
package org.openimaj.hadoop.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;

/**
 * Map-Reduce based tool that merges sequence files
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SequenceFileMerger extends Configured implements Tool {
	@Option(name = "--input", aliases = "-i", usage = "input paths or uris", multiValued = true, required = true)
	private List<String> inputs;

	@Option(name = "--output", aliases = "-o", usage = "output path", required = true)
	private String output;

	@Option(name = "--num-outputs", aliases = "-n", usage = "number of outputs", required = true)
	private int numOutputs;

	/**
	 * Runs the tool
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new SequenceFileMerger(), args);
	}

	@Override
	public int run(String[] args) throws Exception {
		final CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: hadoop jar SequenceFileMerger.jar [options...]");
			parser.printUsage(System.err);
			return 1;
		}

		final List<Path> allPaths = new ArrayList<Path>();
		for (final String p : inputs) {
			allPaths.addAll(Arrays.asList(HadoopToolsUtil.getInputPaths(p)));
		}

		final Job job = TextBytesJobUtil.createJob(allPaths, new Path(output), null, this.getConf());
		job.setJarByClass(this.getClass());
		job.setMapperClass(Mapper.class);
		job.setReducerClass(Reducer.class);
		job.setNumReduceTasks(this.numOutputs);

		job.waitForCompletion(true);

		return 0;
	}
}
