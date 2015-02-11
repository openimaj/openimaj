/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kohsuke.args4j.Option;
import org.openimaj.io.FileUtils;

/**
 * A file tool reads and writes files and knows whether existing outputs should
 * be deleted
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class InOutToolOptions {

	@Option(name = "--input", aliases = "-i", required = false, usage = "Input location", metaVar = "STRING")
	String input = null;

	@Option(name = "--output", aliases = "-o", required = false, usage = "output location", metaVar = "STRING")
	protected String output = null;

	@Option(
			name = "--remove-existing-output",
			aliases = "-rm",
			required = false,
			usage = "If existing output exists, remove it")
	boolean force = false;

	@Option(name = "--input-file", aliases = "-if", required = false, usage = "Get a set of inputs as listed in a file")
	private String inputFile = null;

	@Option(name = "--no-continue", aliases = "-nc", required = false, usage = "Do not continue an existing output")
	boolean contin = false;

	/**
	 * @return the input string option
	 */
	public String getInput() {
		return this.input;
	}

	/**
	 * @return the input string option
	 */
	public String getOutput() {
		return this.output;
	}

	/**
	 * When the input file is set, any existing input file LIST file is removed
	 * (anything from -if)
	 *
	 * @param input
	 *            new input location
	 */
	public void setInput(String input) {
		this.inputFile = null;
		this.input = input;
	}

	/**
	 * @param output
	 *            new input location
	 */
	public void setOutput(String output) {
		this.output = output;
	}

	/**
	 * @return the force option, whether the output should be overwritten if it
	 *         exists
	 */
	public boolean overwriteOutput() {
		return this.isForce();
	}

	/**
	 * Fixes a problem with args4j with multivalued arguments being preserved
	 * within the same JVM
	 *
	 * @param <T>
	 * @param modeOptions
	 * @param defaults
	 *            optional default values if the modeoptions is empty
	 */
	@SafeVarargs
	public static <T> void prepareMultivaluedArgument(List<T> modeOptions, T... defaults) {
		final Set<T> modes = new HashSet<T>();
		for (final T mode : modeOptions) {
			modes.add(mode);
		}
		modeOptions.clear();
		modeOptions.addAll(modes);
		if (modeOptions.isEmpty()) {
			for (final T t : defaults) {
				modeOptions.add(t);
			}
		}
	}

	/**
	 * @return should files be forcefully removed
	 */
	public boolean isForce() {
		return force;
	}

	/**
	 * @return should existing files be continued from
	 */
	public boolean isContinue() {
		return this.contin;
	}

	/**
	 * @return all the inputs from the -if options file, or the single input if
	 *         -i was not defined
	 */
	public String[] getAllInputs() {
		boolean multifiles = this.getInputFile() != null;
		File inputFileF = null;
		if (multifiles) {
			inputFileF = new File(this.getInputFile());
			multifiles = inputFileF.exists() && inputFileF.canRead();
		}
		if (!multifiles) {
			if (this.input == null)
				return null;
			return new String[] { this.input };
		} else {
			try {
				final String[] lines = FileUtils.readlines(inputFileF);
				return lines;
			} catch (final IOException e) {
				if (this.input == null)
					return null;
				return new String[] { this.input };
			}
		}
	}

	/**
	 * @return the input list file, each line is an input
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * @param inputFile
	 *            the new input file
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
}
