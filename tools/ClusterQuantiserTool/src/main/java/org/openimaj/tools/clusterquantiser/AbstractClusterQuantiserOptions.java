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
package org.openimaj.tools.clusterquantiser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.tools.clusterquantiser.ClusterType.ClusterTypeOp;

/**
 * Options for clustering/quantising tool
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public abstract class AbstractClusterQuantiserOptions {
	/**
	 * Usage info
	 */
	public static String EXTRA_USAGE_INFO = "\n"
			+ "Note: the create, info and quant options are mutually exclusive. The depth,\n"
			+ "clusters, verbosity, and samples arguments are only valid in conjuction with\n"
			+ "the create option; they are silently ignored otherwise. The file-type argument"
			+ "is required in create and quant modes.\n" + "\n"
			+ "Mail bug reports and suggestions to <jsh2@ecs.soton.ac.uk>.\n";

	@Option(
			name = "--info",
			aliases = "-if",
			required = false,
			usage = "Print statistics about STRING.",
			metaVar = "STRING")
	protected String infoFile;
	protected boolean info_mode = false;

	@Option(
			name = "--info-diff",
			aliases = "-dif",
			required = false,
			usage = "Calculate the distance between two comparable clusters.",
			metaVar = "STRING")
	protected String otherInfoFile;

	@Option(
			name = "--quant",
			aliases = "-q",
			required = false,
			usage = "Quantize features using vocabulary in FILE.",
			metaVar = "STRING")
	protected String quantLocation;
	// protected File quantLocation;
	protected boolean quant_mode = false;

	@Option(
			name = "--count-mode",
			aliases = "-cm",
			required = false,
			usage = "Output quantisation counts only (rather than each feature quantised)")
	private boolean count_mode = false;

	@Option(
			name = "--verbosity",
			aliases = "-v",
			required = false,
			usage = "Specify verbosity during creation.",
			metaVar = "NUMBER")
	private int verbosity = 0;

	@Option(
			name = "--file-type",
			aliases = "-t",
			required = false,
			usage = "Specify the type of file to be read.",
			handler = ProxyOptionHandler.class)
	protected FileType fileType;

	@Option(
			name = "--threads",
			aliases = "-j",
			required = false,
			usage = "Use NUMBER threads for quantization.",
			metaVar = "NUMBER")
	private int concurrency = Runtime.getRuntime().availableProcessors();

	@Option(
			name = "--extension",
			aliases = "-e",
			required = false,
			usage = "Specify the extension to be added to quantiser output.")
	protected String extension = ".loc";

	@Option(
			name = "--exact-quantisation-mode",
			aliases = "-eqm",
			required = false,
			usage = "Specify the quantisation mode.")
	protected boolean exactQuant = false;

	@Option(
			name = "--random-seed",
			aliases = "-rs",
			required = false,
			usage = "Specify the random seed for all the algorithms which happen to be random.",
			metaVar = "NUMBER")
	private long randomSeed = -1;

	@Argument(required = false)
	protected List<File> inputFiles = new ArrayList<File>();

	private String[] args;

	/**
	 * Construct with arguments
	 * 
	 * @param args
	 */
	public AbstractClusterQuantiserOptions(String[] args) {
		this.args = args;
	}

	/**
	 * Prepare options by parsing arguments
	 * 
	 * @throws CmdLineException
	 */
	public void prepare() throws CmdLineException {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
		} catch (final CmdLineException e) {
			String message = "";
			message += e.getMessage() + "\n";
			message += "Usage: java -jar JClusterQuantiser.jar [options...] [files...]" + "\n";

			final StringWriter sw = new StringWriter();
			parser.printUsage(sw, null);

			message += sw.toString();
			message += ClusterQuantiserOptions.EXTRA_USAGE_INFO + "\n";

			throw new CmdLineException(parser, message);
		}
	}

	/**
	 * @return the file
	 * @throws IOException
	 */
	public String getTreeFile() throws IOException {
		if (info_mode)
			return infoFile;
		if (quant_mode)
			return quantLocation;
		return null;
	}

	/**
	 * @return otherInfoFile
	 */
	public String getOtherInfoFile() {
		if (info_mode)
			return otherInfoFile;
		return null;
	}

	/**
	 * @return true if in info mode
	 */
	public boolean isInfoMode() {
		return info_mode;
	}

	/**
	 * @return true if in quant mode
	 */
	public boolean isQuantMode() {
		return quant_mode;
	}

	/**
	 * @return the verbosity
	 */
	public int getVerbosity() {
		return verbosity;
	}

	/**
	 * @return the file type
	 */
	public FileType getFileType() {
		return fileType;
	}

	/**
	 * @return the number of threads to use
	 */
	public int getConcurrency() {
		return concurrency;
	}

	/**
	 * @return the file extension
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * @return true if in count mode
	 */
	public boolean getCountMode() {
		return this.count_mode;
	}

	/**
	 * @return the random seed
	 */
	public long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * @return the input file
	 */
	public abstract String getInputFileString();

	/**
	 * @return the output file
	 */
	public abstract String getOutputFileString();

	/**
	 * Validate the options
	 * 
	 * @throws CmdLineException
	 */
	public abstract void validate() throws CmdLineException;

	/**
	 * @return the cluster type
	 */
	public abstract ClusterTypeOp getClusterType();

	/**
	 * @return the other-info type
	 */
	public abstract ClusterTypeOp getOtherInfoType();

	/**
	 * @return the java class representing the clusters
	 */
	public abstract Class<? extends SpatialClusters<?>> getClusterClass();

	/**
	 * @return the java class representing the clusters
	 */
	public abstract Class<? extends SpatialClusters<?>> getOtherInfoClass();

	/**
	 * Set the file type
	 * 
	 * @param fileType
	 */
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
}
