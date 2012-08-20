package org.openimaj.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

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
		final CmdLineParser parser = new CmdLineParser(this);

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
