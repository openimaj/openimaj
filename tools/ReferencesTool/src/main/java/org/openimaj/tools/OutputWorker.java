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
