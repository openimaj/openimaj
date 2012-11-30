package org.openimaj.storm.tools.twitter;

import java.io.IOException;

import org.apache.thrift7.TException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.storm.scheme.StringScheme;
import org.openimaj.storm.tool.StormToolOptions;
import org.openimaj.storm.tools.twitter.bolts.TweetPreprocessingBolt;
import org.openimaj.storm.utils.KestrelUtils;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.UnreliableKestrelThriftSpout;
import backtype.storm.topology.TopologyBuilder;

final class TwitterStormToolOptions extends StormToolOptions {
	@Option(
			name = "--topology-name",
			aliases = "-tn",
			required = false,
			usage = "The name of the topology being submitted. If not provided defaults to <ruleLanguage>_topology_<launchTimeInMillis>",
			metaVar = "STRING")
	public String topologyName = "twitterTopology";

	@Option(
			name = "--output-expire-time",
			aliases = "-expire",
			required = false,
			usage = "The name of the topology being submitted. If not provided defaults to <ruleLanguage>_topology_<launchTimeInMillis>",
			metaVar = "STRING")
	public int expire = 0;
	private Config preparedConfig;

	@Option(name = "--twitter-tool-options", aliases = "-twitter", required = false, usage = "Arguments to hand to each twitter preprocessing bolt", metaVar = "STRING")
	private String twitterOptions = null;
	private String inputQueue;
	private String outputQueue;

	TwitterStormToolOptions(String[] args) {
		super(args);
	}

	@Override
	public Config prepareConfig() {
		if (preparedConfig == null) {
			preparedConfig = new Config();
			preparedConfig.setMaxSpoutPending(500);
			preparedConfig.setNumWorkers(numberOfWorkers);
			preparedConfig.setFallBackOnJavaSerialization(false);
			preparedConfig.setSkipMissingKryoRegistrations(false);
			JenaStormUtils.registerSerializers(preparedConfig);
		}

		return preparedConfig;
	}

	@Override
	public StormTopology constructTopology() {
		TopologyBuilder b = new TopologyBuilder();
		b.setSpout("twitterKestrelSpout", new UnreliableKestrelThriftSpout(kestrelSpecList, new StringScheme("tweet"), inputQueue));
		TweetPreprocessingBolt bolt = new TweetPreprocessingBolt(outputQueue, kestrelHosts, twitterOptions.split(" "));
		bolt.setExpireTime(this.expire);
		b.setBolt("preprocessing", bolt).shuffleGrouping("twitterKestrelSpout");
		return b.createTopology();
	}

	@Override
	public String topologyName() {
		return topologyName;
	}

	@Override
	public void topologyCleanup() {

	}

	@Override
	public String getExtractUsageInfo() {
		return "";
	}

	@Override
	public void validate(CmdLineParser parser) throws CmdLineException, IOException {
		this.inputQueue = this.getInput();
		if (inputQueue == null)
			inputQueue = "twitterInputQueue";
		this.outputQueue = this.getOutput();
		if (outputQueue == null)
			outputQueue = "twitterOutputQueue";
		//
		//		try {
		//			prepareQueues();
		//		} catch (TException e) {
		//			// couldn't delete queues
		//		}
	}

	/**
	 * @throws TException
	 */
	public void prepareQueues() throws TException {
		for (KestrelServerSpec ks : this.kestrelSpecList) {
			KestrelUtils.deleteQueues(ks, inputQueue, outputQueue);
		}
	}
}