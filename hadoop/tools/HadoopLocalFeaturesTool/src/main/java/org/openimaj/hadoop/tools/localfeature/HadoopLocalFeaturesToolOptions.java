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
package org.openimaj.hadoop.tools.localfeature;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.map.FImageMultithreadedMapper;
import org.apache.hadoop.mapreduce.lib.map.FastByteWritableMultithreadedMapper;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.apache.hadoop.mapreduce.lib.map.PassThruMultithreadedMapper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;

import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.localfeature.HadoopLocalFeaturesTool.JKeypointMapper;
import org.openimaj.hadoop.tools.localfeature.HadoopLocalFeaturesToolOptions.MapperMode.MapperModeOp;
import org.openimaj.tools.clusterquantiser.ClusterQuantiserOptions;
import org.openimaj.tools.localfeature.LocalFeaturesToolOptions;

public class HadoopLocalFeaturesToolOptions extends LocalFeaturesToolOptions {
	enum MapperMode  implements CmdLineOptionsProvider {
		STANDARD {
			@Override
			public MapperModeOp getOptions() {
				return new MapperModeOp() {
					@Override
					public void prepareJobMapper(Job job, Class<JKeypointMapper> mapperClass) {
						job.setMapperClass(mapperClass);
					}		
				};
			}
		},
		MULTITHREAD{
			@Override
			public MapperModeOp getOptions() {
				return new MapperModeOp() {
					@Option(name = "--threads", aliases = "-j", required = false, usage = "Use NUMBER threads per mapper. defaults n processors.", metaVar = "NUMBER")
					private int concurrency = Runtime.getRuntime().availableProcessors();

					@Override
					public void prepareJobMapper(Job job, Class<JKeypointMapper> mapperClass) {
						if(concurrency <= 0 ) concurrency = Runtime.getRuntime().availableProcessors();

						job.setMapperClass(MultithreadedMapper.class);
						MultithreadedMapper.setNumberOfThreads(job, concurrency);
						MultithreadedMapper.setMapperClass(job, mapperClass);
						System.out.println("Using multithreaded mapper");
					}
				};
			}
		},
		MULTITHREAD_FAST{
			@Override
			public MapperModeOp getOptions() {
				return new MapperModeOp() {
					@Option(name = "--threads", aliases = "-j", required = false, usage = "Use NUMBER threads per mapper. defaults n processors.", metaVar = "NUMBER")
					private int concurrency = Runtime.getRuntime().availableProcessors();

					@Override
					public void prepareJobMapper(Job job, Class<JKeypointMapper> mapperClass) {
						if(concurrency <= 0 ) concurrency = Runtime.getRuntime().availableProcessors();

						job.setMapperClass(FastByteWritableMultithreadedMapper.class);
						FastByteWritableMultithreadedMapper.setNumberOfThreads(job, concurrency);
						FastByteWritableMultithreadedMapper.setMapperClass(job, mapperClass);
						System.out.println("Using specialised fast bytewritable multithreaded mapper");
					}
				};
			}
		},
		MULTITHREAD_FIMAGE {
			@Override
			public MapperModeOp getOptions() {
				return new MapperModeOp() {
					@Option(name = "--threads", aliases = "-j", required = false, usage = "Use NUMBER threads per mapper. defaults n processors.", metaVar = "NUMBER")
					private int concurrency = Runtime.getRuntime().availableProcessors();

					@Override
					public void prepareJobMapper(Job job, Class<JKeypointMapper> mapperClass) {
						if(concurrency <= 0 ) concurrency = Runtime.getRuntime().availableProcessors();

						job.setMapperClass(FImageMultithreadedMapper.class);
						FImageMultithreadedMapper.setNumberOfThreads(job, concurrency);
						FImageMultithreadedMapper.setMapperClass(job, mapperClass);
						System.out.println("Using specialised FImage multithreaded mapper");
					}
				};
			}
		},
		MULTITHREAD_PASSTHRU {
			@Override
			public MapperModeOp getOptions() {
				return new MapperModeOp() {
					@Override
					public void prepareJobMapper(Job job, Class<JKeypointMapper> mapperClass) {

						job.setMapperClass(PassThruMultithreadedMapper.class);
						PassThruMultithreadedMapper.setNumberOfThreads(job, 1);
						PassThruMultithreadedMapper.setMapperClass(job, mapperClass);
						System.out.println("Using Passthur multithreaded mapper");
					}
				};
			}
		}
		;

		@Override
		public abstract MapperModeOp getOptions();

		public interface MapperModeOp {
			public abstract void prepareJobMapper(Job job, Class<JKeypointMapper> mapperClass);
		}
	}

	private String[] args;

	@Option(name="--remove", aliases="-rm", required=false, usage="Remove the existing output location if it exists.", metaVar="BOOLEAN")
	private boolean replace = false;

	@Option(name="--mapper-mode", aliases="-mm", required=false, usage="Choose a mapper mode.", handler=ProxyOptionHandler.class ) 
	MapperMode mapperMode = MapperMode.STANDARD;
	MapperModeOp mapperModeOp;

	@Option(name="--dont-write", aliases="-dr", required=false, usage="Don't actually emmit. Only useful for testing.", metaVar="BOOLEAN") boolean dontwrite = false;

	private boolean beforeMap;

	public HadoopLocalFeaturesToolOptions(String[] args) {
		this(args,false);
	}

	public HadoopLocalFeaturesToolOptions(String[] args, boolean beforeMap) {
		this.args = args;
		this.beforeMap = beforeMap;
	}

	public void prepare(){
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar JClusterQuantiser.jar [options...] [files...]");
			parser.printUsage(System.err);
			System.err.print(ClusterQuantiserOptions.EXTRA_USAGE_INFO);

			System.exit(1);
		}
	}

	private void validate() {
		if(replace && beforeMap){
			try {
				URI outuri = SequenceFileUtility.convertToURI(this.getOutputString());
				FileSystem fs = getFileSystem(outuri);
				fs.delete(new Path(outuri.toString()), true);
			} catch (IOException e) {

			}
		}
	}

	public static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}

	public Path[] getInputPaths() throws IOException {
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(this.getInputString(), "part");
		return sequenceFiles;
	}

	public Path getOutputPath() {
		return new Path(SequenceFileUtility.convertToURI(this.getOutputString()).toString());
	}
}
