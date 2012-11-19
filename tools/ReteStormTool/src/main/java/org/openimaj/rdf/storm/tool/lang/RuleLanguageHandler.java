package org.openimaj.rdf.storm.tool.lang;

import org.openimaj.rdf.storm.tool.ReteStormOptions;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * A rule language handler knows how to be given a set of rules as a string and
 * return a storm topology. This is usually backed by a ReteStormTopologyFactory
 * of one kind or other
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface RuleLanguageHandler {
	/**
	 * Given rules, a storm {@link Config} construct a {@link StormTopology}
	 *
	 * @param options
	 * @param rules
	 * @param config
	 * @return the topology for the rules
	 */
	public StormTopology constructTopology(ReteStormOptions options, Config config);

}
