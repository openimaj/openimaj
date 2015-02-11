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
package org.openimaj.tools.localfeature.options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.kohsuke.args4j.Option;
import org.openimaj.tools.localfeature.BatchExtractor;
import org.openimaj.util.parallel.GlobalExecutorPool.DaemonThreadFactory;

/**
 * Options for the {@link BatchExtractor} tool.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class BatchExtractorOptions extends BaseExtractorOptions {
	@Option(name = "--input", aliases = "-i", usage = "input directory", required = true)
	private File inputBase;

	@Option(name = "--output", aliases = "-o", usage = "output directory", required = true)
	private File outputBase;

	@Option(name = "--output-ext", aliases = "-e", usage = "output extension", required = true)
	private File outputExt;

	@Option(name = "--input-regex", aliases = "-r", usage = "input regex")
	private String inputRegex;

	@Option(name = "--force", aliases = "-f", usage = "force regenerate")
	private boolean force = false;

	@Option(name = "-j", usage = "number of threads to use (defaults to the number of CPU cores)", required = false)
	private int njobs = 0;

	/**
	 * Get the input files
	 *
	 * @return the input files
	 */
	public List<File> getInputs() {
		final List<File> files = new ArrayList<File>();

		getInputs(files, inputBase);

		return files;
	}

	private void getInputs(List<File> files, File dir) {
		for (final File f : dir.listFiles()) {
			if (f.isDirectory()) {
				getInputs(files, f);
			} else {
				// check matches regex
				if (inputRegex == null || f.getName().matches(inputRegex)) {
					// check output
					final File output = getOutput(f);

					if (!output.exists() || force) {
						files.add(f);
					}
				}
			}
		}
	}

	/**
	 * Get the output file corresponding to the input, making directories as
	 * required
	 *
	 * @param f
	 *            the input file
	 * @return the output file
	 */
	public File getOutput(File f) {
		final File tmp = new File(outputBase, f.getAbsolutePath().replace(inputBase.getAbsolutePath(), ""));
		String outputName = tmp.getName();
		if (tmp.getName().contains(".")) {
			outputName = tmp.getName().substring(0, tmp.getName().lastIndexOf("."));
		}
		final File output = new File(tmp.getParent(), outputName + outputExt);

		output.getParentFile().mkdirs();

		return output;
	}

	/**
	 * Get the thread pool to use for performing operations
	 *
	 * @return the thread pool
	 */
	public ThreadPoolExecutor getThreadPool() {
		final int numThreads = this.njobs <= 0 ? Runtime.getRuntime().availableProcessors() : njobs;

		return (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads, new DaemonThreadFactory());
	}
}
