package org.openimaj.picslurper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.io.FileUtils;
import org.openimaj.picslurper.output.OutputListener;
import org.openimaj.picslurper.output.OutputListenerMode;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.tools.FileToolsUtil;
import org.openimaj.tools.InOutToolOptions;

import twitter4j.Status;

/**
 * A tool for slurping images off twitter
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PicSlurper extends InOutToolOptions implements Iterable<InputStream>, Iterator<InputStream> {

	private static Logger logger = Logger.getLogger(PicSlurper.class);

	String[] args;
	boolean stdin;
	List<File> inputFiles;
	boolean stdout;
	File outputLocation;
	File globalStatus;
	Iterator<File> fileIterator;
	File inputFile;
	private static final String STATUS_FILE_NAME = "status.txt";
	/**
	 * System property name for whether twitter console login should be allowed
	 */
	public static final String ALLOW_CONSOLE_LOGIN = "twitter.console_login";

	@Option(
			name = "--encoding",
			aliases = "-e",
			required = false,
			usage = "The outputstreamwriter's text encoding",
			metaVar = "STRING")
	String encoding = "UTF-8";

	@Option(
			name = "--no-stats",
			aliases = "-ns",
			required = false,
			usage = "Don't try to keep stats of the tweets seen",
			metaVar = "STRING")
	boolean stats = true;

	@Option(
			name = "--no-threads",
			aliases = "-j",
			required = false,
			usage = "Threads used to download images, defaults to n CPUs",
			metaVar = "STRING")
	int nThreads = Runtime.getRuntime().availableProcessors();

	@Option(
			name = "--use-oauth-stream",
			aliases = "-oauth",
			required = false,
			usage = "Force the useage of twitter oauth to access the stream using the twitter4j api")
	boolean forceTwitter4J = false;

	@Option(
			name = "--database",
			aliases = "-d",
			required = false,
			usage = "Force the use of a database",
			metaVar = "URL"
	)
	String database = null;

	@Option(
			name = "--databaseTable",
			aliases = "-dt",
			required = false,
			usage = "When using a database, specify the database table",
			metaVar = "STRING" )
	String databaseTable = null;

	@Option(
			name = "--databaseUser",
			aliases = "-du",
			required = false,
			usage = "When using a database, specify the database username",
			metaVar = "STRING" )
	String databaseUser = null;

	@Option(
			name = "--databasePassword",
			aliases = "-dp",
			required = false,
			usage = "When using a database, specify the database password",
			metaVar = "STRING" )
	String databasePW = null;

	@Option(
			name = "--output-listener",
			aliases = "-ol",
			required = false,
			usage = "Add an output listener which gets told about each image downloaded, its location, tweet and url",
			handler = ProxyOptionHandler.class,
			multiValued = true)
	List<OutputListenerMode> outputListenerMode = new ArrayList<OutputListenerMode>();
	List<OutputListener> outputListenerModeOp = new ArrayList<OutputListener>();

	private StatusFeeder statusFeeder;

	/**
	 * @param args
	 *            tool arguments
	 */
	public PicSlurper(final String[] args) {
		this.args = args;
	}

	/**
	 * no args
	 */
	public PicSlurper() {
		this.args = new String[] {};
	}

	/**
	 * prepare the tool for running
	 */
	public void prepare() {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(this.args);
			this.validate();
		} catch (final CmdLineException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar PicSlurper.jar [options...] ");
			parser.printUsage(System.err);
			System.err.println(this.getExtractUsageInfo());
			System.exit(1);
		}
	}

	String getExtractUsageInfo() {
		return "Grab some images and some stats";
	}

	void validate() throws CmdLineException {
		try {
			if( this.database != null )
			{
				this.statusFeeder = new DatabaseStatusFeeder( this.database,
						this.databaseTable, this.databaseUser, this.databasePW );
			}
			else
			if (this.forceTwitter4J) {
				this.statusFeeder = new Twitter4JStreamFeeder();
			} else {
				this.statusFeeder = new InputStreamFeeder(this);
			}
			if (FileToolsUtil.isStdout(this)) {
				this.stdout = true;
			} else {
				this.outputLocation = PicSlurper.validateLocalOutput(this.getOutput(), this.isForce(), !this.isContinue());
				this.outputLocation.mkdirs();
				this.globalStatus = new File(this.outputLocation, PicSlurper.STATUS_FILE_NAME);
				// init the output file
				PicSlurperUtils.updateStats(this.globalStatus, new StatusConsumption());
			}

			for (final OutputListener listener : this.outputListenerModeOp) {
				listener.prepare();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			throw new CmdLineException(null, e.getMessage());
		}
	}

	/**
	 * Validate the (local) ouput from an String and return the corresponding
	 * file.
	 *
	 * @param out
	 *            where the file will go
	 * @param overwrite
	 *            whether to overwrite existing files
	 * @param contin
	 *            whether an existing output should be continued (i.e. ignored
	 *            if it exists)
	 * @return the output file location, deleted if it is allowed to be deleted
	 * @throws IOException
	 *             if the file exists, but can't be deleted
	 */
	public static File validateLocalOutput(final String out, final boolean overwrite, final boolean contin) throws IOException {
		if (out == null) {
			throw new IOException("No output specified");
		}
		final File output = new File(out);
		if (output.exists()) {
			if (overwrite) {
				if (!FileUtils.deleteRecursive(output))
					throw new IOException("Couldn't delete existing output");
			} else if (!contin) {
				throw new IOException("Output already exists, didn't remove");
			}
		}
		return output;
	}

	@Override
	public boolean hasNext() {
		if (!this.stdin) {
			if (this.fileIterator == null)
				return false;
			return this.fileIterator.hasNext();
		}
		return true;
	}

	@Override
	public InputStream next() {
		if (this.stdin) {
			this.stdin = false;
			return System.in;
		}
		if (this.fileIterator == null)
			return null;
		if (this.fileIterator.hasNext()) {
			this.inputFile = this.fileIterator.next();
			try {
				return new FileInputStream(this.inputFile);
			} catch (final FileNotFoundException e) {
			}
		} else
			this.inputFile = null;
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param status
	 *            handle this status
	 */
	public void handleStatus(final Status status) {
		StatusConsumer consumer;
		try {
			consumer = new StatusConsumer(this.stats, this.globalStatus, this.outputLocation, this.outputListenerModeOp);
			consumer.consume(status);

		} catch (final Exception e) {
			PicSlurper.logger.error("Some error with the statusconsumer: " + e.getMessage());
		}
	}

	@Override
	public Iterator<InputStream> iterator() {
		return this;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws TweetTokeniserException
	 * @throws InterruptedException
	 */
	public static void main(final String[] args) throws IOException, TweetTokeniserException, InterruptedException {
		// Load the config
		PicSlurper.loadConfig();
		final PicSlurper slurper = new PicSlurper(args);
		slurper.prepare();
		slurper.start();
	}

	private void start() throws IOException {
		this.statusFeeder.feedStatus(this);

	}

	/**
	 * Load the configuration file which looks for twitter usernames and
	 * passwords. If this can't be found or the values can't be found then
	 * System.in is used to get the username and password
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void loadConfig() throws FileNotFoundException, IOException {
		final File configFile = new File("config.properties");
		PicSlurper.logger.debug("Looking for config file: " + configFile.getAbsolutePath());
		if (configFile.exists()) {
			final Properties prop = System.getProperties();
			prop.load(new FileInputStream(configFile));
			System.setProperties(prop);
		} else {
			// File not found, try looking for the resource!
			final Properties prop = System.getProperties();
			final InputStream propStream = PicSlurper.class.getResourceAsStream("/config.properties");
			if (propStream != null) {
				prop.load(propStream);
			}
			System.setProperties(prop);
		}

		// System.setProperty("org.apache.commons.logging.Log",
		// "org.apache.commons.logging.impl.SimpleLog");
		// System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
		// "true");
		// System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header",
		// "debug");
		// System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
		// "debug");
		// checkTwitterCredentials();
	}
	//
	// private static void checkTwitterCredentials() throws IOException {
	// final String user = System.getProperty("twitter.user");
	// final String password = System.getProperty("twitter.password");
	// final String consoleLogin =
	// System.getProperty(PicSlurper.ALLOW_CONSOLE_LOGIN);
	// if (user != null && password != null || (consoleLogin != null &&
	// !Boolean.parseBoolean(consoleLogin)))
	// return;
	// final Console console = System.console();
	// final String credentialsMessage =
	// "Could not find twitter credentials. Taking from input. You can add these to a config.properties file to save time.\n";
	// final String usernameMessage = "Twitter username: ";
	// final String passwordMessage = "Twitter password: ";
	// if (console != null) {
	// console.printf(credentialsMessage);
	// console.printf(usernameMessage);
	// System.setProperty("twitter.user", console.readLine());
	// console.printf(passwordMessage);
	// System.setProperty("twitter.password",
	// String.copyValueOf(console.readPassword()));
	// } else {
	// logger.debug(credentialsMessage);
	// logger.debug(usernameMessage);
	// final BufferedReader reader = new BufferedReader(new
	// InputStreamReader(System.in));
	// System.setProperty("twitter.user", reader.readLine());
	// logger.debug(passwordMessage);
	// System.setProperty("twitter.password", reader.readLine());
	// }
	//
	// }

}
