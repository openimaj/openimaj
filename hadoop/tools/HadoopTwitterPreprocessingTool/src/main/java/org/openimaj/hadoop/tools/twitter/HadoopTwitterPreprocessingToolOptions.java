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
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;

/**
 * Hadoop specific options for twitter preprocessing
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class HadoopTwitterPreprocessingToolOptions extends AbstractTwitterPreprocessingToolOptions {

	private boolean beforeMaps;

	/**
	 * The hadoop options, assume these are the options before mapping
	 * 
	 * @param args
	 * @throws CmdLineException
	 */
	public HadoopTwitterPreprocessingToolOptions(String[] args) throws CmdLineException {
		this(args, false);
	}

	/**
	 * The hadoop twitter preprocessing options
	 * 
	 * @param args
	 *            command line optios
	 * @param beforeMaps
	 *            if true, the output location is removed if the option to do so
	 *            is set
	 * @throws CmdLineException
	 */
	public HadoopTwitterPreprocessingToolOptions(String[] args, boolean beforeMaps) throws CmdLineException {
		super(args, false); // don't prepare using the superclass
		this.beforeMaps = beforeMaps;
	}

	/*
	 * IO args
	 */
	@Option(
			name = "--mapper-mode",
			aliases = "-mm",
			required = false,
			usage = "Choose a mapper mode.",
			handler = ProxyOptionHandler.class)
	MapperMode mapperMode = MapperMode.STANDARD;
	MapperMode.Mode mapperModeOp;

	@Option(name = "--reudcer-mode", aliases = "-redm", required = false, usage = "Choose a reducer mode mode.")
	ReducerModeOption reducerMode = ReducerModeOption.NULL;

	@Option(
			name = "--return-immediately",
			aliases = "-ri",
			required = false,
			usage = "If set, the job is submitted to the cluster and this returns immediately")
	boolean returnImmediately = false;

	@Option(
			name = "--lzo-compress",
			aliases = "-lzoc",
			required = false,
			usage = "If set, compress the output of the preprocessing pipeline as LZO")
	boolean lzoCompress = false;

	@Override
	public boolean validate() throws CmdLineException {
		if (this.beforeMaps) {
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
		final Path[] sequenceFiles = SequenceFileUtility.getFilePaths(this.getAllInputs(), "part");
		return sequenceFiles;
	}

	/**
	 * @return the output path
	 */
	public Path getOutputPath() {
		return new Path(this.getOutput());
	}

}
