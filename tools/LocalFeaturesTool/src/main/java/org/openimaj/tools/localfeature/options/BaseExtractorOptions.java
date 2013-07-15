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
import java.io.FileInputStream;
import java.io.IOException;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.io.IOUtils;
import org.openimaj.tools.localfeature.options.LocalFeatureMode.LocalFeatureModeOp;

/**
 * Options for the Extractor tool
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BaseExtractorOptions extends SharedOptions {
	@Option(
			name = "--mode",
			aliases = "-m",
			required = false,
			usage = "SIFT keypoint mode.",
			handler = ProxyOptionHandler.class)
	private LocalFeatureMode mode = LocalFeatureMode.SIFT;
	private LocalFeatureModeOp modeOp = LocalFeatureMode.SIFT.getOptions();

	@Option(
			name = "--print-time-taken",
			aliases = "-ptt",
			required = false,
			usage = "Print to the standard output the time taken to extract features")
	boolean printTime = false;

	@Option(
			name = "--extractor-file",
			aliases = "-ef",
			usage = "Serialise the configured feature extractor to the given file")
	File extractorFile;

	/**
	 * @return the mode
	 */
	public LocalFeatureModeOp getMode() {
		return modeOp;
	}

	/**
	 * Read the input image as bytes
	 * 
	 * @param input
	 *            the input location
	 * 
	 * @return the input image as bytes
	 * @throws IOException
	 */
	public byte[] getInputImage(String input) throws IOException {
		return getInputImage(new File(input));
	}

	/**
	 * Read the input image as bytes
	 * 
	 * @param file
	 *            the input location
	 * 
	 * @return the input image as bytes
	 * @throws IOException
	 */
	public byte[] getInputImage(File file) throws IOException {

		if (file.isDirectory())
			throw new RuntimeException("Unsupported operation, file "
					+ file.getAbsolutePath() + " is a directory");
		if (file.length() > Integer.MAX_VALUE)
			throw new RuntimeException("Unsupported operation, file "
					+ file.getAbsolutePath() + " is too big");

		Throwable pending = null;
		FileInputStream in = null;
		final byte buffer[] = new byte[(int) file.length()];
		try {
			in = new FileInputStream(file);
			in.read(buffer);
		} catch (final Exception e) {
			pending = new RuntimeException("Exception occured on reading file "
					+ file.getAbsolutePath(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final Exception e) {
					if (pending == null) {
						pending = new RuntimeException(
								"Exception occured on closing file"
										+ file.getAbsolutePath(), e);
					}
				}
			}
			if (pending != null) {
				throw new RuntimeException(pending);
			}
		}
		return buffer;
	}

	/**
	 * @return true if timing information should be printed
	 */
	public boolean printTiming() {
		return this.printTime;
	}

	/**
	 * Serialise the feature extractor to a file so it can be reused by other
	 * tools or code.
	 * 
	 * @throws IOException
	 */
	public void serialiseExtractor() throws IOException {
		if (extractorFile != null) {
			IOUtils.writeToFile(this.modeOp, extractorFile);
		}
	}
}
