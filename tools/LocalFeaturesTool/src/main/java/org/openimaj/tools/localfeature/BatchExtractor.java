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
package org.openimaj.tools.localfeature;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.io.IOUtils;
import org.openimaj.time.Timer;
import org.openimaj.tools.localfeature.options.BatchExtractorOptions;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

/**
 * Tool for extracting local features
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BatchExtractor {
	/**
	 * Run the tool
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final BatchExtractorOptions options = new BatchExtractorOptions();
		final CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar LocalFeaturesTool.jar BatchExtractor [options]");
			parser.printUsage(System.err);
			return;
		}

		final ThreadPoolExecutor pool = options.getThreadPool();

		Parallel.forEach(options.getInputs(), new Operation<File>() {
			@Override
			public void perform(File input) {
				try {
					final byte[] img = options.getInputImage(input);

					final Timer timing = Timer.timer();
					final LocalFeatureList<? extends LocalFeature<?, ?>> kpl = options.getMode().extract(img);
					timing.stop();

					if (options.printTiming()) {
						System.out.println("Took: " + timing.duration());
					}

					if (options.isAsciiMode()) {
						IOUtils.writeASCII(options.getOutput(input), kpl);
					} else {
						IOUtils.writeBinary(options.getOutput(input), kpl);
					}
				} catch (final IOException e) {
					System.err.println(e);
				}
			}
		}, pool);

		options.serialiseExtractor();
	}
}
