package org.openimaj.rdf.storm.topology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.topology.builder.KestrelReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.NTriplesReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder;
import org.openimaj.rdf.storm.utils.JenaStromUtils;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.scheduler.Cluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a set of rules, construct a RETE topology such that filter (alpha)
 * nodes and join (beta) nodes are filtering bolts
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteTopology {
	/**
	 * The name of a debug bolt
	 */
	public static final String DEBUG_BOLT = "debugBolt";

	/**
	 * default rules
	 */
	public static final String RDFS_RULES = "/org/openimaj/rdf/rules/rdfs-fb-tgc-noresource.rules";
	/**
	 * the name of the final bolt
	 */
	public static final String FINAL_TERMINAL = "final_term";
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ReteTopology.class);

	private InputStream rulesStream;

	/**
	 * Construct a Rete topology using the default RDFS rules
	 * @param conf the {@link Config} to be sent to the {@link Cluster}. Only used to register serialisers
	 */
	public ReteTopology(Config conf) {
		JenaStromUtils.registerSerializers(conf);
		this.rulesStream = ReteTopology.class.getResourceAsStream(RDFS_RULES);
	}

	/**
	 * Construct a Rete topology using the InputStream as a source of rules
	 *
	 * @param conf
	 *
	 * @param rulesStream
	 *            the stream of rules
	 */
	public ReteTopology(Config conf, InputStream rulesStream) {
		JenaStromUtils.registerSerializers(conf);
		this.rulesStream = rulesStream;

	}

	/**
	 * Using specified rules, construct a RETE storm topology
	 *
	 * @param nTriples
	 *            A URL containing nTriples
	 *
	 * @return a storm topology
	 */
	public StormTopology buildTopology(String nTriples) {
		TopologyBuilder builder = new TopologyBuilder();
		List<Rule> rules = loadRules();
		ReteTopologyBuilder topologyBuilder = new NTriplesReteTopologyBuilder(nTriples);
		topologyBuilder.compile(builder, rules);
		return builder.createTopology();
	}

	/**
	 * Using specified rules, construct a RETE storm topology
	 *
	 * @param spec
	 *            The kestrel server to which to connect
	 * @param inputQueue
	 *            String outputQueue A kestrel queue containing triples
	 * @param outputQueue
	 *            the name of the output queue
	 *
	 * @return a storm topology
	 */
	public StormTopology buildTopology(KestrelServerSpec spec, String inputQueue, String outputQueue) {

		TopologyBuilder builder = new TopologyBuilder();
		List<Rule> rules = loadRules();
		ReteTopologyBuilder topologyBuilder = new KestrelReteTopologyBuilder(spec, inputQueue, outputQueue);
		topologyBuilder.compile(builder, rules);

		return builder.createTopology();
	}

	/**
	 * @param topologyBuilder
	 *
	 * @return given a {@link ReteTopologyBuilder} and a list of
	 *         {@link ReteTopology} instances construct a {@link StormTopology}
	 */
	public StormTopology buildTopology(ReteTopologyBuilder topologyBuilder) {
		TopologyBuilder builder = new TopologyBuilder();
		topologyBuilder.compile(builder, loadRules());
		StormTopology top = builder.createTopology();
		return top;
	}

	/**
	 * @param config
	 *            the {@link Config} instance with which the
	 *            {@link StormTopology} will be submitted to the {@link Cluster}.
	 * @param topologyBuilder the approach to constructing a {@link StormTopology}
	 * @param rules the rules to construct the rete network with
	 * @return given a {@link TopologyBuilder} and a source for {@link Rule}
	 *         instances build {@link StormTopology}
	 */
	public static StormTopology buildTopology(Config config, ReteTopologyBuilder topologyBuilder, InputStream rules) {
		ReteTopology topology = new ReteTopology(config,rules);
		return topology.buildTopology(topologyBuilder);
	}

	private List<Rule> loadRules() {
		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader(new BufferedReader(new InputStreamReader(
				this.rulesStream))));
		return rules;
	}

	/**
	 * run the rete topology
	 *
	 * @param args
	 * @throws InvalidTopologyException
	 * @throws AlreadyAliveException
	 * @throws IOException
	 * @throws TException
	 */
	public static void main(String args[]) throws AlreadyAliveException, InvalidTopologyException, TException,
			IOException
	{
		String rdfSource = "file:///Users/ss/Development/java/openimaj/trunk/storm/ReteStorm/src/test/resources/test.rdfs";
		String ruleSource = "/Users/ss/Development/java/openimaj/trunk/storm/ReteStorm/src/test/resources/test.rules";

		Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(2);
		conf.setMaxSpoutPending(1);
		conf.setFallBackOnJavaSerialization(false);
		conf.setSkipMissingKryoRegistrations(false);
		ReteTopology reteTopology = new ReteTopology(conf, new FileInputStream(ruleSource));
		LocalCluster cluster = new LocalCluster();
		StormTopology topology = reteTopology.buildTopology(rdfSource);
		// KestrelServerSpec spec = KestrelServerSpec.localThrift();
		// String inputQueue = "triples";
		// String outputQueue = "processedTriples";
		// KestrelUtils.deleteQueues(spec, inputQueue, outputQueue);
		// NTripleKestrelTupleWriter rdfWriter = new
		// NTripleKestrelTupleWriter(new URL(rdfSource));
		// rdfWriter.write(spec, inputQueue, outputQueue);
		// StormTopology topology = reteTopology.buildTopology(spec, inputQueue,
		// outputQueue);
		cluster.submitTopology("reteTopology", conf, topology);

		// Thread t = new Thread(new KestrelQueuePrinter(spec, outputQueue));
		// t.setDaemon(true);
		// t.start();

		Utils.sleep(10000);
		cluster.killTopology("reteTopology");
		cluster.shutdown();

	}
}
