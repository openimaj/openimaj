package org.openimaj.rdf.storm.tool.staticdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.rdf.storm.sparql.topology.builder.datasets.InMemoryDataset;
import org.openimaj.rdf.storm.sparql.topology.builder.datasets.StaticRDFDataset;

/**
 * Creates an {@link InMemoryDataset} instance and optionally copies the data to
 * a
 * location from which the servers can see the RDF to load into memory
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class InMemoryStaticDataMode implements StaticDataMode {

	@Override
	public Map<String, StaticRDFDataset> datasets(Map<String, String> datasetNameLocations) {

		Map<String, StaticRDFDataset> ret = new HashMap<String, StaticRDFDataset>();
		for (Entry<String, String> dsNameLoc : datasetNameLocations.entrySet()) {
			ret.put(dsNameLoc.getKey(), new InMemoryDataset(dsNameLoc.getValue()));
		}
		return ret;
	}

}
