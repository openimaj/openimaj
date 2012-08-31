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
package org.openimaj.hadoop.tools.clusterquantiser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.clusterquantiser.HadoopClusterQuantiserOptions.MapperMode.MapperModeOp;
import org.openimaj.hadoop.tools.clusterquantiser.HadoopClusterQuantiserTool.ClusterQuantiserMapper;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.tools.clusterquantiser.AbstractClusterQuantiserOptions;
import org.openimaj.tools.clusterquantiser.ClusterType;
import org.openimaj.tools.clusterquantiser.ClusterType.ClusterTypeOp;

public class HadoopClusterQuantiserOptions extends AbstractClusterQuantiserOptions {

	enum MapperMode implements CmdLineOptionsProvider {
		STANDARD {
			@Override
			public MapperModeOp getOptions() {
				return new StandardOp();
			}
		},
		MULTITHREAD {
			@Override
			public MapperModeOp getOptions() {
				return new MultithreadOp();
			}
		};

		public static abstract class MapperModeOp {
			public abstract void prepareJobMapper(Job job, Class<ClusterQuantiserMapper> mapperClass,
					AbstractClusterQuantiserOptions opts);
		}

		private static class StandardOp extends MapperModeOp {
			@Override
			public void prepareJobMapper(Job job, Class<ClusterQuantiserMapper> mapperClass,
					AbstractClusterQuantiserOptions opts)
			{
				job.setMapperClass(mapperClass);
			}
		}

		private static class MultithreadOp extends MapperModeOp {

			@Override
			public void prepareJobMapper(Job job, Class<ClusterQuantiserMapper> mapperClass,
					AbstractClusterQuantiserOptions opts)
			{
				int concurrency = opts.getConcurrency();
				if (opts.getConcurrency() <= 0)
					concurrency = Runtime.getRuntime().availableProcessors();

				job.setMapperClass(MultithreadedMapper.class);
				MultithreadedMapper.setNumberOfThreads(job, concurrency);
				MultithreadedMapper.setMapperClass(job, mapperClass);
				System.out.println("NThreads = " + MultithreadedMapper.getNumberOfThreads(job));
			}
		}
	}

	private boolean beforeMaps;

	public HadoopClusterQuantiserOptions(String[] args) throws CmdLineException {
		this(args, false);
	}

	public HadoopClusterQuantiserOptions(String[] args, boolean beforeMaps) throws CmdLineException {
		super(args);
		this.beforeMaps = beforeMaps;
	}

	/*
	 * IO args
	 */
	@Option(name = "--input", aliases = "-i", required = true, usage = "set the input sequencefile")
	private String input = null;

	@Option(name = "--output", aliases = "-o", required = true, usage = "set the output directory")
	private String output = null;

	@Option(
			name = "--force-delete",
			aliases = "-rm",
			required = false,
			usage = "If it exists, remove the output directory before starting")
	private boolean forceRM = false;

	@Option(
			name = "--mapper-mode",
			aliases = "-mm",
			required = false,
			usage = "Choose a mapper mode.",
			handler = ProxyOptionHandler.class)
	MapperMode mapperMode = MapperMode.STANDARD;
	protected MapperModeOp mapperModeOp = (MapperModeOp) MapperMode.STANDARD.getOptions();

	private ClusterTypeOp clusterTypeOp;

	private Class<? extends SpatialClusters<?>> clusterClass;

	@Override
	public String getInputFileString() {
		return input;
	}

	@Override
	public String getOutputFileString() {
		return output;
	}

	@Override
	public void validate() throws CmdLineException {

		if (infoFile != null) {
			info_mode = true;
			try {
				this.clusterTypeOp = sniffClusterType(infoFile);
				if (this.clusterTypeOp == null)
					throw new CmdLineException(null, "Could not identify the clustertype");

				this.clusterClass = this.clusterTypeOp.getClusterClass();
			} catch (final IOException e) {
				throw new CmdLineException(null, "Could not identify the clustertype. File: " + infoFile, e);
			}

		}
		if (quantLocation != null) {
			if (info_mode)
				throw new CmdLineException(null,
						"--quant and --info are mutually exclusive.");
			quant_mode = true;
			try {
				this.clusterTypeOp = sniffClusterType(quantLocation);
				if (this.clusterTypeOp == null)
					throw new CmdLineException(null, "Could not identify the clustertype");

				this.clusterClass = this.clusterTypeOp.getClusterClass();
			} catch (final Exception e) {
				e.printStackTrace();
				throw new CmdLineException(null, "Could not identify the clustertype. File: " + quantLocation, e);
			}
		}

		if (this.getCountMode()) {
			if (this.extension.equals(".loc"))
				this.extension = ".counts";
		}
		if (forceRM && this.beforeMaps) {

			try {
				final URI outuri = SequenceFileUtility.convertToURI(this.output);
				final FileSystem fs = getFileSystem(outuri);
				fs.delete(new Path(outuri.toString()), true);
			} catch (final IOException e) {

			}
		}
	}

	public static FileSystem getFileSystem(URI uri) throws IOException {
		final Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem)
			fs = ((LocalFileSystem) fs).getRaw();
		return fs;
	}

	public static ClusterTypeOp sniffClusterType(String quantFile) throws IOException {
		InputStream fios = null;
		try {
			fios = getClusterInputStream(quantFile);
			return ClusterType.sniffClusterType(new BufferedInputStream(fios));
		} finally {
			if (fios != null)
				try {
					fios.close();
				} catch (final IOException e) { /* don't care */
				}
		}
	}

	@Override
	public ClusterTypeOp getClusterType() {
		return this.clusterTypeOp;
	}

	public static InputStream getClusterInputStream(String uriStr) throws IOException {
		final URI uri = SequenceFileUtility.convertToURI(uriStr);
		final FileSystem fs = getFileSystem(uri);
		final Path p = new Path(uri.toString());
		return fs.open(p);
	}

	public InputStream getClusterInputStream() throws IOException {
		return getClusterInputStream(this.quantLocation);
	}

	public String getClusterInputString() {
		return this.quantLocation;
	}

	public Path[] getInputPaths() throws IOException {
		final Path[] sequenceFiles = SequenceFileUtility.getFilePaths(this.getInputFileString(), "part");
		return sequenceFiles;
	}

	@Override
	public ClusterTypeOp getOtherInfoType() {
		return null;
	}

	@Override
	public Class<? extends SpatialClusters<?>> getClusterClass() {
		return this.clusterClass;
	}

	@Override
	public Class<SpatialClusters<?>> getOtherInfoClass() {
		return null;
	}
}
