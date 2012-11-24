package org.openimaj.rdf.storm.tool.lang;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.openimaj.kestrel.KestrelTupleWriter;
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
	 * @return the topology for the rules
	 */
	public StormTopology constructTopology(ReteStormOptions options);

	/**
	 * How tuples are written to the kestrel queue
	 * 
	 * @param urlList
	 * @return
	 * @throws IOException
	 */
	public KestrelTupleWriter tupleWriter(ArrayList<URL> urlList) throws IOException;

	/**
	 * Initialise the configuration if required
	 * 
	 * @param preparedConfig
	 */
	public void initConfig(Config preparedConfig);

}
