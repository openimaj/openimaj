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
import org.openimaj.rdf.storm.sparql.topology.builder.group.IdentityKestrelStaticDataSPARQLReteTopologyBuilder;
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
public class IdentitySPARQLRuleLanguageHandler implements RuleLanguageHandler {

	private Logger logger = Logger.getLogger(IdentitySPARQLRuleLanguageHandler.class);

	/**
	 *
	 */
	@Option(
			name = "--query-solution-serialization",
			aliases = "-qss",
			required = false,
			usage = "How output bindings should be serialized if the query")
	public QuerySolutionSerializer qss = QuerySolutionSerializer.JSON;

	@Override
	public StormTopology constructTopology(ReteStormOptions options, Config config) {

		IdentityKestrelStaticDataSPARQLReteTopologyBuilder topologyBuilder = new IdentityKestrelStaticDataSPARQLReteTopologyBuilder(
				options.getKestrelSpecList(),
				options.inputQueue, options.outputQueue,
				options.staticDataSources());
		topologyBuilder.setConfig(config);
		topologyBuilder.setQuerySolutionSerializerMode(qss);
		StormSPARQLReteTopologyOrchestrator orchestrator = null;
		try {
			orchestrator = StormSPARQLReteTopologyOrchestrator.createTopologyBuilder(
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
