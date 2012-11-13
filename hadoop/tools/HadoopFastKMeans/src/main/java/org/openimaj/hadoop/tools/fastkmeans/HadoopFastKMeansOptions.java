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
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.ml.clustering.kmeans.ByteKMeansInit;
import org.openimaj.tools.clusterquantiser.FileType;

public class HadoopFastKMeansOptions {
	@Option(
			name = "--threads",
			aliases = "-j",
			required = false,
			usage = "Use NUMBER threads for quantization.",
			metaVar = "NUMBER")
	public int concurrency = Runtime.getRuntime().availableProcessors();

	public ByteKMeansInit init = new ByteKMeansInit.RANDOM();

	@Option(name = "--input", aliases = "-i", required = true, usage = "set the input sequencefile", multiValued = true)
	public List<String> inputs;

	@Option(
			name = "--output",
			aliases = "-o",
			required = true,
			usage = "set the cluster output directory. The final cluster will go into output/final")
	public String output;

	@Option(name = "--number-of-clusters", aliases = "-k", required = false, usage = "Number of clusters.")
	public int k = 100;

	@Option(name = "--file-type", aliases = "-t", required = false, usage = "Specify the type of file to be read.")
	public String fileType = FileType.BINARY_KEYPOINT.toString();

	@Option(name = "--nsamples", aliases = "-s", required = false, usage = "How many samples should be selected")
	public int nsamples = -1;

	@Option(name = "--exact-mode", aliases = "-e", required = false, usage = "Compare the features in exact mode")
	public boolean exact = false;

	@Option(
			name = "--force-delete",
			aliases = "-rm",
			required = false,
			usage = "If it exists, remove the output directory before starting")
	public boolean forceRM = false;

	@Option(
			name = "--number-of-iterations",
			aliases = "-iters",
			required = false,
			usage = "How many times should the Kmeans iterate")
	public int iter = 3;

	@Option(name = "--samples-only", aliases = "-so", required = false, usage = "Extract samples only.")
	public boolean samplesOnly = false;

	@Option(
			name = "--check-sample-equality",
			aliases = "-cse",
			required = false,
			usage = "Extract samples but only check which features are identical (euclidian sense).")
	public boolean checkSampleEquality = false;

	@Option(
			name = "--check-sample-equality-threshold",
			aliases = "-cset",
			required = false,
			usage = "The threshold for sample equality.")
	public int checkSampleEqualityThreshold = 0;

	private boolean beforeMaps;

	public String[] args;
	public String[] original_args;

	public HadoopFastKMeansOptions(String[] args) {
		this(args, false);
	}

	public HadoopFastKMeansOptions(String[] args, boolean beforeMaps) {
		this.beforeMaps = beforeMaps;
		this.args = args;
	}

	public HadoopFastKMeansOptions(String[] args, String[] original_args, boolean b) {
		this.args = args;
		this.original_args = original_args;
		this.beforeMaps = b;
	}

	public static FileSystem getFileSystem(URI uri) throws IOException {
		final Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem)
			fs = ((LocalFileSystem) fs).getRaw();
		return fs;
	}

	public void prepare() {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar HadoopFastKMeans.jar [options...] [files...]");
			parser.printUsage(System.err);
			System.err.print(HadoopFastKMeans.EXTRA_USAGE_INFO);

			System.exit(1);
		}

	}

	private void validate() {
		System.out.println("forcerm " + this.forceRM + " beforemaps " + this.beforeMaps);
		if (this.forceRM && this.beforeMaps) {
			System.out.println("Attempting to delete: " + this.output);
			try {
				final URI outuri = SequenceFileUtility.convertToURI(this.output);
				final FileSystem fs = getFileSystem(outuri);
				fs.delete(new Path(outuri.toString()), true);

			} catch (final IOException e) {
				System.out.println("Error deleting!!");
				e.printStackTrace();
			}
		}
	}

}
