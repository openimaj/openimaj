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
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.localfeature.HadoopLocalFeaturesTool.LocalFeaturesMapper;
import org.openimaj.hadoop.tools.localfeature.HadoopLocalFeaturesToolOptions.MapperMode.MapperModeOp;
import org.openimaj.tools.localfeature.options.ExtractorOptions;

/**
 * Options for the {@link HadoopLocalFeaturesTool}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HadoopLocalFeaturesToolOptions extends ExtractorOptions {
	static enum MapperMode implements CmdLineOptionsProvider {
		STANDARD {
			@Override
			public MapperModeOp getOptions() {
				return new MapperModeOp() {
					@Override
					public void prepareJobMapper(Job job, Class<LocalFeaturesMapper> mapperClass) {
						job.setMapperClass(mapperClass);
					}
				};
			}
		},
		MULTITHREAD {
			@Override
			public MapperModeOp getOptions() {
				return new MapperModeOp() {
					@Option(
							name = "--threads",
							aliases = "-j",
							required = false,
							usage = "Use NUMBER threads per mapper. defaults n processors.",
							metaVar = "NUMBER")
					private int concurrency = Runtime.getRuntime().availableProcessors();

					@Override
					public void prepareJobMapper(Job job, Class<LocalFeaturesMapper> mapperClass) {
						if (concurrency <= 0)
							concurrency = Runtime.getRuntime().availableProcessors();

						job.setMapperClass(MultithreadedMapper.class);
						MultithreadedMapper.setNumberOfThreads(job, concurrency);
						MultithreadedMapper.setMapperClass(job, mapperClass);
						System.out.println("Using multithreaded mapper");
					}
				};
			}
		};

		@Override
		public abstract MapperModeOp getOptions();

		public interface MapperModeOp {
			public abstract void prepareJobMapper(Job job, Class<LocalFeaturesMapper> mapperClass);
		}
	}

	private String[] args;

	@Option(
			name = "--remove",
			aliases = "-rm",
			required = false,
			usage = "Remove the existing output location if it exists.",
			metaVar = "BOOLEAN")
	private boolean replace = false;

	@Option(
			name = "--mapper-mode",
			aliases = "-mm",
			required = false,
			usage = "Choose a mapper mode.",
			handler = ProxyOptionHandler.class)
	MapperMode mapperMode = MapperMode.STANDARD;
	MapperModeOp mapperModeOp;

	@Option(
			name = "--dont-write",
			aliases = "-dr",
			required = false,
			usage = "Don't actually emmit. Only useful for testing.",
			metaVar = "BOOLEAN")
	boolean dontwrite = false;

	@Option(
			name = "--dont-compress-output",
			required = false,
			usage = "Don't compress sequencefile records.",
			metaVar = "BOOLEAN")
	boolean dontcompress = false;

	private boolean beforeMap;

	/**
	 * Construct with the given arguments string
	 * 
	 * @param args
	 */
	public HadoopLocalFeaturesToolOptions(String[] args) {
		this(args, false);
	}

	/**
	 * Construct with the given arguments string
	 * 
	 * @param args
	 * @param beforeMap
	 */
	public HadoopLocalFeaturesToolOptions(String[] args, boolean beforeMap) {
		this.args = args;
		this.beforeMap = beforeMap;
	}

	/**
	 * Prepare the options
	 */
	public void prepare() {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: hadoop jar HadoopLocalFeaturesTool.jar [options...] [files...]");
			parser.printUsage(System.err);

			System.exit(1);
		}
	}

	private void validate() {
		if (replace && beforeMap) {
			try {
				final URI outuri = SequenceFileUtility.convertToURI(this.getOutput());
				final FileSystem fs = getFileSystem(outuri);
				fs.delete(new Path(outuri.toString()), true);
			} catch (final IOException e) {

			}
		}
	}

	static FileSystem getFileSystem(URI uri) throws IOException {
		final Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem)
			fs = ((LocalFileSystem) fs).getRaw();
		return fs;
	}

	/**
	 * @return the input paths
	 * @throws IOException
	 */
	public Path[] getInputPaths() throws IOException {
		final Path[] sequenceFiles = SequenceFileUtility.getFilePaths(this.getInput(), "part");
		return sequenceFiles;
	}

	/**
	 * @return the output path
	 */
	public Path getOutputPath() {
		return new Path(SequenceFileUtility.convertToURI(this.getOutput()).toString());
	}
}
