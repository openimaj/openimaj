package org.openimaj.rdf.storm.topology;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.rdf.storm.topology.builder.NTriplesSPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.SPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.utils.JenaStromUtils;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.scheduler.Cluster;
import backtype.storm.topology.TopologyBuilder;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a set of rules, construct a RETE topology such that filter (alpha)
 * nodes and join (beta) nodes are filtering bolts
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SPARQLReteStormTopologyBuilder {
	/**
	 * The name of a debug bolt
	 */
	public static final String DEBUG_BOLT = "debugBolt";

	/**
	 * default rules
	 */
	public static final String SELECT_ALL = "SELECT ?a ?b ?c WHERE {?a ?b ?c.}";
	/**
	 * the name of the final bolt
	 */
	public static final String FINAL_TERMINAL = "final_term";
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SPARQLReteStormTopologyBuilder.class);

	private Query query;

	private Config conf;

	/**
	 * Construct a Rete topology using the default RDFS rules
	 * @param conf the {@link Config} to be sent to the {@link Cluster}. Only used to register serialisers
	 */
	public SPARQLReteStormTopologyBuilder(Config conf) {
		JenaStromUtils.registerSerializers(conf);
		this.query = QueryFactory.create(SELECT_ALL);
	}

	/**
	 * Construct a Rete topology using the InputStream as a source of rules
	 *
	 * @param conf
	 * @param query the SPARQL query
	 */
	public SPARQLReteStormTopologyBuilder(Config conf, String query) {
		this.conf = conf;
		JenaStromUtils.registerSerializers(conf);
		this.query = QueryFactory.create(query);

	}



	/**
	 * Using an {@link NTriplesSPARQLReteTopologyBuilder}, load the nTriples from
	 * the given resource and compile a storm topology for the sparql query
	 * used to construct this {@link SPARQLReteStormTopologyBuilder}
	 *
	 * @param nTriples
	 *            A URL containing nTriples
	 *
	 * @return a storm topology
	 */
	public StormTopology buildTopology(String nTriples) {
		TopologyBuilder builder = new TopologyBuilder();
		SPARQLReteTopologyBuilder topologyBuilder = new NTriplesSPARQLReteTopologyBuilder(nTriples);
		topologyBuilder.compile(builder, this.query);
		return builder.createTopology();
	}

	/**
	 * @param topologyBuilder
	 *
	 * @return given a {@link ReteTopologyBuilder} and a list of
	 *         {@link SPARQLReteStormTopologyBuilder} instances construct a {@link StormTopology}
	 */
	public StormTopology buildTopology(SPARQLReteTopologyBuilder topologyBuilder) {
		TopologyBuilder builder = new TopologyBuilder();
		topologyBuilder.compile(builder, this.query);
		StormTopology top = builder.createTopology();
		return top;
	}

	/**
	 * @param config
	 *            the {@link Config} instance with which the
	 *            {@link StormTopology} will be submitted to the {@link Cluster}.
	 * @param topologyBuilder the approach to constructing a {@link StormTopology}
	 * @param query the query from which to construct the network
	 * @return given a {@link TopologyBuilder} and a source for {@link Rule}
	 *         instances build {@link StormTopology}
	 */
	public static StormTopology buildTopology(Config config, SPARQLReteTopologyBuilder topologyBuilder, String query) {
		SPARQLReteStormTopologyBuilder topology = new SPARQLReteStormTopologyBuilder(config,query);
		return topology.buildTopology(topologyBuilder);
	}

	/**
	 * A {@link SPARQLReteStormTopologyBuilder} with a default configuration
	 * @param query
	 * @return
	 */
	public static SPARQLReteStormTopologyBuilder buildDefaultTopology(String query) {
		Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(2);
		conf.setMaxSpoutPending(1);
		conf.setFallBackOnJavaSerialization(false);
		conf.setSkipMissingKryoRegistrations(false);
		SPARQLReteStormTopologyBuilder fact = new SPARQLReteStormTopologyBuilder(conf,query);
		return fact;
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
		SPARQLReteStormTopologyBuilder fact = SPARQLReteStormTopologyBuilder.buildDefaultTopology(
				"BASE  <http://example.org/ns#>" +
				"CONSTRUCT {?a ?b ?c}" +
				"" +
				"" +
				" WHERE {" +
				"?a ?b ?c,?d. " +
				"{?a ?c ?b}" +
				"FILTER regex(?a, \"thing\",\"i\")}"
		);

		LocalCluster cluster = new LocalCluster();
		StormTopology topology = fact.buildTopology(rdfSource);
//		cluster.submitTopology("reteTopology", fact.conf, topology);
//		Utils.sleep(10000);
//		cluster.killTopology("reteTopology");
//		cluster.shutdown();

	}


}
