package org.openimaj.rdf.storm.tool;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.io.FileUtils;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;
import org.openimaj.rdf.storm.tool.lang.RuleLanguageHandler;
import org.openimaj.rdf.storm.tool.lang.RuleLanguageMode;
import org.openimaj.rdf.storm.tool.source.TriplesInputMode;
import org.openimaj.rdf.storm.tool.source.TriplesInputModeOption;
import org.openimaj.rdf.storm.tool.topology.TopologyMode;
import org.openimaj.rdf.storm.tool.topology.TopologyModeOption;
import org.openimaj.tools.InOutToolOptions;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * The options for preparing, configuring and running a {@link ReteStorm}
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteStormOptions extends InOutToolOptions {

	/**
	 * The name of the topology to submit
	 */
	@Option(
			name = "--topology-name",
			aliases = "-tn",
			required = false,
			usage = "The name of the topology being submitted. If not provided defaults to <ruleLanguage>_topology_<launchTimeInMillis>",
			metaVar = "STRING")
	public String topologyName = null;

	/**
	 * The topology language mode
	 */
	@Option(
			name = "--rule-language",
			aliases = "-rl",
			required = false,
			usage = "The language to decipher rules and construct a Rete network as a Storm Topology",
			handler = ProxyOptionHandler.class,
			metaVar = "STRING")
	public RuleLanguageMode ruleLanguageMode = RuleLanguageMode.JENA;
	/**
	 * The actual {@link RuleLanguageHandler}
	 */
	public RuleLanguageHandler ruleLanguageModeOp = null;

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
	/**
	 * The topology mode options
	 */
	public TopologyMode tmOp = tm.getOptions();

	/**
	 * The triples input mode
	 */
	@Option(
			name = "--triples-input",
			aliases = "-ti",
			required = false,
			usage = "The source of triples",
			handler = ProxyOptionHandler.class)
	public TriplesInputModeOption triplesInputMode = TriplesInputModeOption.URI;
	/**
	 * The actual options of the triples input mode
	 */
	public TriplesInputMode triplesInputModeOp = triplesInputMode.getOptions();

	private String[] args;

	private String rules;

	/**
	 *
	 */
	@Option(
			name = "--kestrel-host",
			aliases = "-kh",
			required = false,
			usage = "The message queue host from which and to which triples will be written",
			metaVar = "STRING")
	public String kestrelHost = KestrelServerSpec.LOCALHOST;
	/**
	 *
	 */
	@Option(
			name = "--kestrel-port",
			aliases = "-kp",
			required = false,
			usage = "The message queue port from which and to which triples will be written",
			metaVar = "STRING")
	public int kestrelPort = KestrelServerSpec.DEFAULT_KESTREL_THRIFT_PORT;

	/**
	 * @param args
	 */
	public ReteStormOptions(String[] args) {
		this.args = args;
	}

	/**
	 * Parse arguments and validate
	 *
	 * @throws IOException
	 */
	public void prepare() throws IOException {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate(parser);
		} catch (final CmdLineException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar ReteStormTool.jar [options...] ");
			parser.printUsage(System.err);
			System.err.println(this.getExtractUsageInfo());
			System.exit(1);
		}
	}

	private String getExtractUsageInfo() {
		return "";
	}

	private void validate(CmdLineParser parser) throws CmdLineException, IOException {
		if (this.topologyName == null) {
			this.topologyName = ruleLanguageMode.toString() + "_topology_" + System.currentTimeMillis();
		}
		if(this.getInput() == null){
			throw new CmdLineException(parser, "No input rules provided.");
		}
		File rulesFile = new File(this.getInput());
		if (!rulesFile.exists()) {
			throw new CmdLineException(parser, "Input rules file does not exist!");
		}
		this.rules = FileUtils.readall(rulesFile);
	}

	/**
	 * Given a storm configuration construct a Storm topology using the
	 * specified ruleLanguageMode
	 *
	 * @param conf
	 * @return the constructed storm topology
	 */
	public StormTopology constructTopology(Config conf) {
		return this.ruleLanguageModeOp.constructTopology(this, conf);
	}

	/**
	 * @return the rules
	 */
	public String getRules() {
		return this.rules;
	}

	/**
	 * @return the triples as an input stream
	 * @throws IOException
	 */
	public NTripleKestrelTupleWriter triplesKestrelWriter() throws IOException {
		return this.triplesInputModeOp.asKestrelWriter();
	}

}
