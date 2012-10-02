package org.openimaj.rdf.storm.tool;

import org.openimaj.rdf.storm.topology.RuleReteStormTopologyFactory;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a string which can be compiled as Jena {@link Rule} instances
 * construct a storm topology using {@link RuleReteStormTopologyFactory}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JenaRuleLanguageHandler implements RuleLanguageHandler {

	@Override
	public StormTopology constructTopology(String rules, Config config) {
		return null;
	}

}
