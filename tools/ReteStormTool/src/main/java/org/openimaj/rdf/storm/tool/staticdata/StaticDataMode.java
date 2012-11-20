package org.openimaj.rdf.storm.tool.staticdata;

import java.util.Map;

import org.openimaj.rdf.storm.sparql.topology.builder.datasets.StaticRDFDataset;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public interface StaticDataMode {
	/**
	 * Using the datasetNameLocations, initialises and prepares the datasets
	 * before
	 * providing instances of datasets per name for the streaming system
	 * 
	 * @param datasetNameLocations
	 * @return the {@link StaticRDFDataset} instance now ready to be queried
	 */
	public Map<String, StaticRDFDataset> datasets(Map<String, String> datasetNameLocations);
}
