package org.openimaj.rdf.storm.tool.lang;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openimaj.kestrel.KestrelTupleWriter;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.openimaj.rdf.storm.topology.RuleReteStormTopologyFactory;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a string which can be compiled as Jena {@link Rule} instances construct
 * a storm topology using {@link RuleReteStormTopologyFactory}
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JenaRuleLanguageHandler implements RuleLanguageHandler {
	Logger logger = Logger.getLogger(JenaRuleLanguageHandler.class);

	@Override
	public StormTopology constructTopology(ReteStormOptions options, Config config) {
		RuleReteStormTopologyFactory factory = new RuleReteStormTopologyFactory(config, options.getRules());
		String inputQueue = "triples";
		String outputQueue = "processedTriples";
		StormTopology topology = null;
		try {
			KestrelTupleWriter rdfWriter = options.triplesKestrelWriter();
			rdfWriter.write(options.kestrelSpecList, inputQueue, outputQueue);
			topology = factory.buildTopology(options.kestrelSpecList.get(0), inputQueue, outputQueue); // FIXME
		} catch (Exception e) {
			logger.error("Couldn't construct topology: " + e.getMessage());
			e.printStackTrace();
		}
		return topology;
	}

	@Override
	public KestrelTupleWriter tupleWriter(ArrayList<URL> urlList) throws IOException {
		return new NTripleKestrelTupleWriter(urlList);
	}

}
