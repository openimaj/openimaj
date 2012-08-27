package org.openimaj.rdf.storm.topology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;
import org.openimaj.kestrel.writing.NTripleWritingScheme;
import org.openimaj.rdf.storm.spout.NTriplesSpout;
import org.openimaj.rdf.storm.topology.builder.KestrelReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.SimpleReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.utils.KestrelUtils;
import org.openimaj.rdf.storm.utils.JenaStromUtils;
import org.openimaj.storm.bolt.CountingEmittingBolt;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.KestrelThriftSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
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
	 * The name of the spout outputting triples
	 */
	public static final String TRIPLE_SPOUT = "tripleSpout";
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
	 */
	public ReteTopology() {
		this.rulesStream = ReteTopology.class.getResourceAsStream(RDFS_RULES);
	}

	/**
	 * Construct a Rete topology using the InputStream as a source of rules
	 * 
	 * @param rulesStream
	 *            the stream of rules
	 */
	public ReteTopology(InputStream rulesStream) {
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
		NTriplesSpout tripleSpout = new NTriplesSpout(nTriples);
		builder.setSpout(TRIPLE_SPOUT, tripleSpout, 1);
		// builder.setBolt(DEBUG_BOLT, new PrintingBolt(),
		// 1).shuffleGrouping(TRIPLE_SPOUT);
		// compileCountingEmittingBolt(builder,tripleSpout.getFields());

		List<Rule> rules = loadRules();
		ReteTopologyBuilder topologyBuilder = new SimpleReteTopologyBuilder();
		topologyBuilder.compile(builder, TRIPLE_SPOUT, rules);
		// compileBolts(builder, TRIPLE_SPOUT, rules);

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
		// NTriplesSpout tripleSpout = new NTriplesSpout(kestrelQueue);
		KestrelThriftSpout tripleSpout = new KestrelThriftSpout(spec.host, spec.port, inputQueue, new NTripleWritingScheme());

		builder.setSpout(TRIPLE_SPOUT, tripleSpout, 1);
		// builder.setBolt(DEBUG_BOLT, new PrintingBolt(),
		// 1).shuffleGrouping(TRIPLE_SPOUT);

		List<Rule> rules = loadRules();
		ReteTopologyBuilder topologyBuilder = new KestrelReteTopologyBuilder(spec, inputQueue, outputQueue);
		topologyBuilder.compile(builder, TRIPLE_SPOUT, rules);

		return builder.createTopology();
	}

	@SuppressWarnings("unused")
	private void compileCountingEmittingBolt(TopologyBuilder builder, Fields fields) {
		CountingEmittingBolt countingEmittingBolt1 = new CountingEmittingBolt(fields);
		builder.setBolt("blah1", countingEmittingBolt1, 1).shuffleGrouping(TRIPLE_SPOUT).shuffleGrouping("blah1");
	}

	private List<Rule> loadRules() {
		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader(new BufferedReader(new InputStreamReader(this.rulesStream))));
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
	public static void main(String args[]) throws AlreadyAliveException, InvalidTopologyException, TException, IOException {
		String rdfSource = "file:///Users/ss/Development/java/openimaj/trunk/storm/ReteStorm/src/test/resources/test.rdfs";
		String ruleSource = "/Users/ss/Development/java/openimaj/trunk/storm/ReteStorm/src/test/resources/test.rules";
		ReteTopology reteTopology = new ReteTopology(new FileInputStream(ruleSource));
		//
		Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(2);
		conf.setMaxSpoutPending(1);
		conf.setFallBackOnJavaSerialization(false);
		conf.setSkipMissingKryoRegistrations(false);
		JenaStromUtils.registerSerializers(conf);
		LocalCluster cluster = new LocalCluster();
		// StormTopology topology =
		// reteTopology.buildTopology("file:///Users/ss/Development/java/openimaj/trunk/storm/ReteStorm/src/test/resources/test.rdfs");
		KestrelServerSpec spec = KestrelServerSpec.localThrift();
		String inputQueue = "triples";
		String outputQueue = "processedTriples";
		KestrelUtils.deleteQueues(spec, inputQueue, outputQueue);
		NTripleKestrelTupleWriter rdfWriter = new NTripleKestrelTupleWriter(new URL(rdfSource));
		rdfWriter.write(spec, inputQueue, outputQueue);
		StormTopology topology = reteTopology.buildTopology(spec, inputQueue, outputQueue);
		cluster.submitTopology("reteTopology", conf, topology);

		Thread t = new Thread(new KestrelQueuePrinter(spec, outputQueue));
		t.setDaemon(true);
		t.start();

		Utils.sleep(20000);
		cluster.killTopology("reteTopology");
		cluster.shutdown();

	}
}
