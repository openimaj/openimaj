package org.openimaj.storm.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.tool.topology.TopologyMode;
import org.openimaj.rdf.storm.tool.topology.TopologyModeOption;
import org.openimaj.tools.InOutToolOptions;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class StormToolOptions extends InOutToolOptions {
	private static final String KESTREL_FORMAT = "%s:%s";
	/**
	 * The topology
	 */
	@Option(
			name = "--topology-mode",
			aliases = "-tm",
			required = false,
			usage = "The kind of topology to submit to",
			handler = ProxyOptionHandler.class)
	public TopologyModeOption tm = TopologyModeOption.STORM;
	public TopologyMode tmOp = tm.getOptions();

	/**
	 * the ketrel queues for input and output
	 */
	@Option(
			name = "--kestrel-host",
			aliases = "-kh",
			required = false,
			usage = "The message queue host from which and to which triples will be written",
			metaVar = "STRING",
			multiValued = true)
	public List<String> kestrelHosts = new ArrayList<String>();

	/**
	 * number of topology workers selected
	 */
	@Option(
			name = "--topology-workers",
			aliases = "-twork",
			required = false,
			usage = "The number of workers running the executors of this topology")
	public int numberOfWorkers = 2;
	/**
	 * parsed kestrel server specs
	 */
	public List<KestrelServerSpec> kestrelSpecList = new ArrayList<KestrelServerSpec>();

	private String[] args;

	/**
	 * @param args
	 */
	public StormToolOptions(String[] args) {
		this.args = args;
	}

	/**
	 * @return provides a config based on the options
	 */
	public abstract Config prepareConfig();

	/**
	 * @return provides a {@link StormTopology} based on the options
	 */
	public abstract StormTopology constructTopology();

	/**
	 * @return the topology name
	 */
	public abstract String topologyName();

	/**
	 * called when the {@link TopologyModeOption} believes it is done
	 */
	public abstract void topologyCleanup();

	/**
	 * Parse arguments and validate
	 * 
	 * @throws IOException
	 */
	public void prepare() throws IOException {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validateInternal(parser);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar ReteStormTool.jar [options...] ");
			parser.printUsage(System.err);
			System.err.println(this.getExtractUsageInfo());
			System.exit(1);
		}
	}

	/**
	 * @return usage of the tool
	 */
	public abstract String getExtractUsageInfo();

	/**
	 * validate the parsed input, could be overwritten by sublcasses
	 * 
	 * @param parser
	 * @throws Exception
	 * @throws IOException
	 * @throws CmdLineException
	 */
	private void validateInternal(CmdLineParser parser) throws CmdLineException, IOException {
		if (this.kestrelHosts.size() == 0) {
			this.kestrelHosts.add(String.format(KESTREL_FORMAT, KestrelServerSpec.LOCALHOST, KestrelServerSpec.DEFAULT_KESTREL_THRIFT_PORT));
		}
		this.kestrelSpecList = KestrelServerSpec.parseKestrelAddressList(this.kestrelHosts);
		this.validate(parser);
	}

	/**
	 * @param parser
	 *            initialise the tool
	 * @throws IOException
	 * @throws CmdLineException
	 *             when something goes wrong
	 */
	public abstract void validate(CmdLineParser parser) throws CmdLineException, IOException;
}