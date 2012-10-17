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
package org.openimaj.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.openimaj.aop.classloader.ClassLoaderTransform;

/**
 * Options for the References Tool
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ReferencesToolOpts {
	@Option(
			name = "--writeBibtex",
			aliases = "-wb",
			usage = "Write BibTeX formatted references to a file.",
			required = false)
	File bibtexFile;

	@Option(
			name = "--printBibtex",
			aliases = "-pb",
			usage = "Print BibTeX formatted references to STDOUT.",
			required = false)
	boolean printBibtex = false;

	@Option(
			name = "--writeText",
			aliases = "-wt",
			usage = "Write text formatted references to a file.",
			required = false)
	File textFile;

	@Option(
			name = "--printText",
			aliases = "-pt",
			usage = "Print text formatted references to STDOUT.",
			required = false)
	boolean printText = false;

	@Option(
			name = "--writeHTML",
			aliases = "-wh",
			usage = "Write HTML formatted references to a file.",
			required = false)
	File htmlFile;

	@Option(
			name = "--printHTML",
			aliases = "-ph",
			usage = "Print HTML formatted references to STDOUT.",
			required = false)
	boolean printHTML = false;

	@Option(
			name = "-jar",
			usage = "Runnable jar containing the application to run",
			required = false)
	File jarFile;

	@Option(
			name = "-classpath",
			aliases = "-cp",
			usage = "Additional classpath string (separated by a colon on OSX/Linux [:] or semicolon [;] on windows)",
			required = false)
	String classpath;

	@Argument(required = false)
	List<String> arguments = new ArrayList<String>();

	String mainMethod;

	protected ExtendedCmdLineParser parser;

	/**
	 * Load the options from the arguments string
	 * 
	 * @param args
	 *            the arguments string
	 * 
	 * @throws CmdLineException
	 *             if an error occurs
	 */
	public ReferencesToolOpts(String[] args) throws CmdLineException {
		parser = new ExtendedCmdLineParser(this);
		parser.parseArgument(args);
	}

	/**
	 * Validate the options, throwing a {@link CmdLineException} if the
	 * arguments are not valid.
	 * 
	 * @throws CmdLineException
	 */
	public void validate() throws CmdLineException {
		if (this.jarFile != null && this.classpath != null) {
			throw new CmdLineException(null, "-jar and -cp are independent and cannot both be set");
		}

		try {
			if (this.jarFile != null && ClassLoaderTransform.getMainClass(this.jarFile) == null) {
				throw new CmdLineException(null, "Failed to load Main-Class manifest attribute from " + jarFile);
			}
		} catch (final IOException e) {
			throw new CmdLineException(null, "Unable to read jar file " + this.jarFile, e);
		}

		if (this.jarFile == null && this.classpath == null) {
			this.classpath = ".";
		}

		if (this.classpath != null) {
			if (this.arguments == null || this.arguments.size() < 1)
				throw new CmdLineException(null, "A main class must be specified.");

			this.mainMethod = this.arguments.remove(0);
		}
	}

	/**
	 * @return true if jar mode; false if classpath mode
	 */
	public boolean isJar() {
		return this.jarFile != null;
	}
}
