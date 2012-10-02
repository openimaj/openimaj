package org.openimaj.rdf.storm.tool;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.io.FileUtils;
import org.openimaj.tools.InOutToolOptions;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * The options for preparing, configuring and running a {@link ReteStorm}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteStormOptions extends InOutToolOptions{


	/**
	 * The name of the topology to submit
	 */
	@Option(name = "--topology-name", aliases = "-tn", required = false, usage = "The name of the topology being submitted. If not provided defaults to <ruleLanguage>_topology_<launchTimeInMillis>", metaVar = "STRING")
	public String topologyName = null;

	/**
	 * The topology language mode
	 */
	@Option(name = "--rule-language", aliases = "-rl", required = false, usage = "The language to decipher rules and construct a Rete network as a Storm Topology", metaVar = "STRING")
	public RuleLanguageMode ruleLanguageMode = RuleLanguageMode.JENA;
	/**
	 * The actual {@link RuleLanguageHandler}
	 */
	public RuleLanguageHandler ruleLanguageModeOp = null;

	private String[] args;

	private String rules;
	/**
	 * @param args
	 */
	public ReteStormOptions(String[] args) {
		this.args = args;
	}

	/**
	 * Parse arguments and validate
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
		if(this.topologyName == null){
			this.topologyName = ruleLanguageMode.toString() + "_topology_" + System.currentTimeMillis();
		}
		File rulesFile = new File(this.getInput());
		if(!rulesFile.exists()){
			throw new CmdLineException(parser,"Input rules file does not exist!");
		}
		this.rules = FileUtils.readall(rulesFile);
	}

	/**
	 * Given a storm configuration construct a Storm topology using the specified ruleLanguageMode
	 * @param conf
	 * @return the constructed storm topology
	 */
	public StormTopology constructTopology(Config conf) {
		return this.ruleLanguageModeOp.constructTopology(getRules(), conf);
	}

	private String getRules() {
		return this.rules;
	}

}
