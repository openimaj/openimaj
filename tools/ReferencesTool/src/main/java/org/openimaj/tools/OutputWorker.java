package org.openimaj.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.openimaj.citation.agent.ReferenceListener;
import org.openimaj.citation.annotation.output.StandardFormatters;

public class OutputWorker implements Runnable {
	private static void writeReferencesFile(String filename, StandardFormatters type) {
		if (filename == null)
			return;

		final File file = new File(filename);

		final String data =
				type.formatReferences(ReferenceListener.getReferences());

		Writer writer = null;
		try {
			writer = new FileWriter(file);
			writer.append(data);
		} catch (final IOException e) {
			System.err.println("Error writing references file: " + filename);
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
				type.formatReferences(ReferenceListener.getReferences());

		System.out.println(data);
	}

	@Override
	public void run() {
		printReferences(StandardFormatters.STRING);
	}
}
