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
package org.openimaj.hadoop.tools.twitter;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.mapreduce.StageRunner;
import org.openimaj.hadoop.mapreduce.stage.Stage;
import org.openimaj.hadoop.mapreduce.stage.helper.TextStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;



/**
 * A hadoop implementation of twitter preprocessing
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class HadoopTwitterPreprocessingTool extends StageRunner {
	String[] args;
	private HadoopTwitterPreprocessingToolOptions options;
	/**
	 * where arguments are held
	 */
	public static final String ARGS_KEY = "twitter.preprocessing.args";


	@Override
	public Stage<?, ?, ?, ?, ?, ?, ?, ?> stage() {

		return new TextStage(){
			@Override
			public Class<? extends Mapper<LongWritable, Text, NullWritable, Text>> mapper() {
				return SimpleTwitterPreprocessingMapper.class;
			}

			@Override
			public void setup(Job job) {
				if(options.reducerMode == ReducerModeOption.NULL){
					job.setNumReduceTasks(0);
				}
				else if(options.reducerMode == ReducerModeOption.IDENTITY){
					job.setNumReduceTasks(1);
				}
				job.getConfiguration().setStrings(HadoopTwitterPreprocessingTool.ARGS_KEY, HadoopToolsUtil.encodeArgs(args));
			}

			@Override
			public boolean lzoCompress() {
				return options.lzoCompress;
			}
		};
	}

	@Override
	public Path output() {
		return options.getOutputPath();
	}

	@Override
	public Path[] inputs() throws IOException {
		return options.getInputPaths();
	}

	@Override
	public void args(String[] args) throws CmdLineException, IOException {
		this.options = new HadoopTwitterPreprocessingToolOptions(args,true);
		options.prepare();
		this.args = args;
	}

	@Override
	public boolean shouldWait() {
		return !options.returnImmediately;
	}

	/**
	 * run the tool
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			new HadoopTwitterPreprocessingTool().runMain(args);
		} catch (CmdLineException e) {
			System.err.print(e);
		}
	}
}
