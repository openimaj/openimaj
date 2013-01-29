package org.openimaj.rdf.storm.tool.lang;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openimaj.kestrel.KestrelTupleWriter;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.openimaj.rdf.storm.topology.RuleReteStormTopologyFactory;

import backtype.storm.generated.StormTopology;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a string which can be compiled as Jena {@link Rule} instances construct
 * a storm topology using {@link RuleReteStormTopologyFactory}
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JenaRuleLanguageHandler extends BaseRuleLanguageHandler {
	Logger logger = Logger.getLogger(JenaRuleLanguageHandler.class);

	@Override
	public StormTopology constructTopology(ReteStormOptions options) {
		RuleReteStormTopologyFactory factory = new RuleReteStormTopologyFactory(options.prepareConfig(), options.getRules());
		String inputQueue = options.inputQueue;
		String outputQueue = options.outputQueue;
		StormTopology topology = null;
		try {
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
