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
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;

/**
 * Hadoop specific options for twitter preprocessing
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class HadoopTwitterPreprocessingToolOptions extends AbstractTwitterPreprocessingToolOptions {
	
	enum MapperMode  implements CmdLineOptionsProvider{
		STANDARD{

			@Override
			public void prepareJobMapper(Job job, Class<TwitterPreprocessingMapper> mapperClass) {
				job.setMapperClass(mapperClass);
			}
		},
		MULTITHREAD{
			
			@Option(name = "--threads", aliases = "-j", required = false, usage = "Use NUMBER threads per mapper. defaults n processors.", metaVar = "NUMBER")
			private int concurrency = Runtime.getRuntime().availableProcessors();
			
			@Override
			public void prepareJobMapper(Job job, Class<TwitterPreprocessingMapper> mapperClass) {
				if(concurrency <= 0 ) concurrency = Runtime.getRuntime().availableProcessors();
				
				job.setMapperClass(MultithreadedMapper.class);
				MultithreadedMapper.setNumberOfThreads(job, concurrency);
				MultithreadedMapper.setMapperClass(job, mapperClass);
				System.out.println("NThreads = " + MultithreadedMapper.getNumberOfThreads(job));
			}	
		};
		
		public abstract void prepareJobMapper(Job job, Class<TwitterPreprocessingMapper> mapperClass);
		@Override
		public Object getOptions() {
			return this;
		}
	}
	
	private boolean  beforeMaps;
	/**
	 * The hadoop options, assume these are the options before mapping
	 * @param args
	 * @throws CmdLineException
	 */
	public HadoopTwitterPreprocessingToolOptions(String[] args) throws CmdLineException {
		this(args,false);
	}
	
	/**
	 * The hadoop twitter preprocessing options
	 * 
	 * @param args command line optios
	 * @param beforeMaps if true, the output location is removed if the option to do so is set
	 * @throws CmdLineException
	 */
	public HadoopTwitterPreprocessingToolOptions(String[] args,boolean beforeMaps) throws CmdLineException {
		super(args);
		this.beforeMaps = beforeMaps;
	}

	/*
	 * IO args
	 */
	@Option(name="--mapper-mode", aliases="-mm", required=false, usage="Choose a mapper mode.", handler=ProxyOptionHandler.class ) 
	MapperMode mapperMode = MapperMode.STANDARD;
	
	@Override
	public boolean validate() throws CmdLineException {
		if(this.beforeMaps){
			HadoopToolsUtil.validateInput(this);
			HadoopToolsUtil.validateOutput(this);
		}
		return true;
	}

	/**
	 * @return the list of input files
	 * @throws IOException 
	 */
	public Path[] getInputPaths() throws IOException {
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(this.getAllInputs(), "part");
		return sequenceFiles;
	}

	/**
	 * @return the output path
	 */
	public Path getOutputPath() {
		return new Path(this.getOutput());
	}

	
}
