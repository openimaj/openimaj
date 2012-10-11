package org.openimaj.rdf.storm.tool.lang;

import org.apache.log4j.Logger;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.openimaj.rdf.storm.topology.RuleReteStormTopologyFactory;
import org.openimaj.rdf.storm.topology.utils.KestrelUtils;

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
		KestrelServerSpec spec = new KestrelServerSpec(options.kestrelHost, options.kestrelPort);
		String inputQueue = "triples";
		String outputQueue = "processedTriples";
		StormTopology topology = null;
		try {
			KestrelUtils.deleteQueues(spec, inputQueue, outputQueue);
			NTripleKestrelTupleWriter rdfWriter = options.triplesKestrelWriter();
			rdfWriter.write(spec, inputQueue, outputQueue);
			topology = factory.buildTopology(spec, inputQueue,outputQueue);
		} catch (Exception e) {
			logger.error("Couldn't construct topology: " + e.getMessage());
			e.printStackTrace();
		}
		return topology;
	}

}
