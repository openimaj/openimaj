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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.citation.ReferenceListener;
import org.openimaj.citation.annotation.output.StandardFormatters;

/**
 * Helper class for outputting the bibliography information requested in the
 * {@link ReferencesToolOpts}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class OutputWorker implements Runnable {
	ReferencesToolOpts options;

	/**
	 * Construct with arguments string
	 * 
	 * @param args
	 *            the arguments
	 * @throws CmdLineException
	 */
	public OutputWorker(String[] args) throws CmdLineException {
		options = new ReferencesToolOpts(args);
		options.validate();
	}

	private static void writeReferences(File file, StandardFormatters type) {
		final String data =
				type.format(ReferenceListener.getReferences());

		Writer writer = null;
		try {
			writer = new FileWriter(file);
			writer.append(data);
		} catch (final IOException e) {
			System.err.println("Error writing references file: " + file);
			e.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (final IOException e) {
				}
		}
	}

	private static void printReferences(StandardFormatters type) {
		final String data =
				type.format(ReferenceListener.getReferences());

		System.out.println(data);
	}

	@Override
	public void run() {
		if (options.printBibtex)
			printReferences(StandardFormatters.BIBTEX);
		if (options.printHTML)
			printReferences(StandardFormatters.HTML);
		if (options.printText)
			printReferences(StandardFormatters.STRING);

		if (options.bibtexFile != null)
			writeReferences(options.bibtexFile, StandardFormatters.BIBTEX);
		if (options.htmlFile != null)
			writeReferences(options.htmlFile, StandardFormatters.HTML);
		if (options.textFile != null)
			writeReferences(options.textFile, StandardFormatters.STRING);
	}
}
