package org.openimaj.picslurper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

/**
 * A tool for slurping images off twitter
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StormPicSlurper extends InOutToolOptions {

	private static Logger logger = Logger.getLogger(StormPicSlurper.class);
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
			name = "--output-listener",
			aliases = "-ol",
			required = false,
			usage = "Add an output listener which gets told about each image downloaded, its location, tweet and url",
			handler = ProxyOptionHandler.class,
			multiValued = true)
	List<OutputListenerMode> outputListenerMode = new ArrayList<OutputListenerMode>();
	List<OutputListener> outputListenerModeOp = new ArrayList<OutputListener>();

	/**
	 * @param args
	 *            tool arguments
	 */
	public StormPicSlurper(String[] args) {
		this.args = args;
	}

	/**
	 * no args
	 */
	public StormPicSlurper() {
		this.args = new String[] {};
	}

	/**
	 * prepare the tool for running
	 */
	public void prepare() {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar JClusterQuantiser.jar [options...] [files...]");
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
			if (FileToolsUtil.isStdin(this)) {
				this.stdin = true;
			}
			else {
				this.inputFiles = FileToolsUtil.validateLocalInput(this);
				this.fileIterator = this.inputFiles.iterator();
			}
			if (FileToolsUtil.isStdout(this)) {
				this.stdout = true;
			}
			else
			{
				this.outputLocation = validateLocalOutput(this.getOutput(), this.isForce(), !this.isContinue());
				this.outputLocation.mkdirs();
				this.globalStatus = new File(outputLocation, STATUS_FILE_NAME);
				// init the output file
				PicSlurperUtils.updateStats(this.globalStatus, new StatusConsumption());
			}
		} catch (final Exception e) {
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
	public static File validateLocalOutput(String out, boolean overwrite, boolean contin) throws IOException {
		if (out == null) {
			throw new IOException("No output specified");
		}
		final File output = new File(out);
		if (output.exists()) {
			if (overwrite) {
				if (!FileUtils.deleteRecursive(output))
					throw new IOException("Couldn't delete existing output");
			}
			else if (!contin) {
				throw new IOException("Output already exists, didn't remove");
			}
		}
		return output;
	}

	void start() throws IOException, TweetTokeniserException, InterruptedException {
		final LocalCluster cluster = new LocalCluster();
		LocalTweetSpout spout = null;
		if (this.stdin) {
			spout = new StdinSpout();
		}
		else {
			spout = new LocalFileTweetSpout(this.getAllInputs());
		}
		final TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("stream_spout", spout);
		// builder.setBolt("print", new
		// PrintBolt()).shuffleGrouping("stream_spout");
		builder.setBolt("download",
				new DownloadBolt(this.stats, this.globalStatus, this.outputLocation, this.outputListenerModeOp),
				this.nThreads).shuffleGrouping("stream_spout");

		final Config conf = new Config();
		conf.setDebug(false);
		cluster.submitTopology("urltop", conf, builder.createTopology());
		while (!LocalTweetSpout.isFinished()) {
			Thread.sleep(10000);
		}
		logger.debug("TweetSpout says it is finished, shutting down cluster");
		cluster.shutdown();

	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws TweetTokeniserException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, TweetTokeniserException, InterruptedException {
		// Load the config
		PicSlurper.loadConfig();
		final StormPicSlurper slurper = new StormPicSlurper(args);
		slurper.prepare();
		slurper.start();
	}

}
