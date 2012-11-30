package org.openimaj.rdf.storm.tool.lang;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;
import org.openimaj.kestrel.GraphKestrelTupleWriter;
import org.openimaj.kestrel.KestrelTupleWriter;
import org.openimaj.rdf.storm.sparql.topology.StormSPARQLReteTopologyOrchestrator;
import org.openimaj.rdf.storm.sparql.topology.bolt.sink.QuerySolutionSerializer;
import org.openimaj.rdf.storm.sparql.topology.builder.group.KestrelStaticDataSPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.sparql.topology.builder.group.StaticDataSPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.tool.ReteStormOptions;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * Instantiates a {@link StaticDataSPARQLReteTopologyBuilder}, preparing the
 * static data sources,
 * the streaming data sources.
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SPARQLRuleLanguageHandler implements RuleLanguageHandler {

	private Logger logger = Logger.getLogger(SPARQLRuleLanguageHandler.class);

	/**
	 *
	 */
	@Option(
			name = "--query-solution-serialization",
			aliases = "-qss",
			required = false,
			usage = "How output bindings should be serialized if the query")
	public QuerySolutionSerializer qss = QuerySolutionSerializer.JSON;

	/**
	 *
	 */
	@Option(
			name = "--unreliable-kestrel-spout",
			aliases = "-unrelk",
			required = false,
			usage = "Force an unreliable kestrel spout to be used")
	public boolean unreliableKestrelSpout = false;

	/**
	 *
	 */
	@Option(
			name = "--ack-stats-queue",
			aliases = "-ackqueue",
			required = false,
			usage = "Force an unreliable kestrel spout to be used")
	public String ackQueue = null;

	/**
	 *
	 */
	@Option(
			name = "--kestrel-intput-plain-ntriples",
			aliases = "-kintriples",
			required = false,
			usage = "The kestrel queue contains plain ntriples")
	public boolean plainNTriples = false;

	@Override
	public void initConfig(Config config) {
		config.put(KestrelStaticDataSPARQLReteTopologyBuilder.RETE_TOPOLOGY_KESTREL_UNRELIABLE, unreliableKestrelSpout);
		if (unreliableKestrelSpout && ackQueue == null) {
			ackQueue = KestrelStaticDataSPARQLReteTopologyBuilder.RETE_TOPOLOGY_KESTREL_ACK_QUEUE_DEFAULT;
		}
		config.put(KestrelStaticDataSPARQLReteTopologyBuilder.RETE_TOPOLOGY_KESTREL_ACK_QUEUE, ackQueue);
		config.put(KestrelStaticDataSPARQLReteTopologyBuilder.RETE_TOPOLOGY_KESTREL_PLAIN_TRIPLES, plainNTriples);
	}

	@Override
	public StormTopology constructTopology(ReteStormOptions options) {
		Config config = options.prepareConfig();

		KestrelStaticDataSPARQLReteTopologyBuilder topologyBuilder = new KestrelStaticDataSPARQLReteTopologyBuilder(
				options.getKestrelSpecList(),
				options.inputQueue, options.outputQueue,
				options.staticDataSources(),
				config
				);

		topologyBuilder.setConfig(config);
		topologyBuilder.setQuerySolutionSerializerMode(qss);
		StormSPARQLReteTopologyOrchestrator orchestrator = null;
		try {
			orchestrator = StormSPARQLReteTopologyOrchestrator.createTopologyBuilder(
					config,
					topologyBuilder,
					options.getRules());
		} catch (Exception e) {
			logger.error("Failed to create topology orchestrator", e);
		}
		StormTopology topology = null;
		try {

			topology = orchestrator.buildTopology();
		} catch (Exception e) {
			logger.error("Couldn't construct topology: " + e.getMessage());
			e.printStackTrace();
		}
		return topology;
	}

	@Override
	public KestrelTupleWriter tupleWriter(ArrayList<URL> urlList) throws IOException {
		return new GraphKestrelTupleWriter(urlList);
	}
}
