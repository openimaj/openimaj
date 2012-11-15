package org.openimaj.rdf.storm.sparql.topology.bolt;

import com.hp.hpl.jena.query.Query;

/**
 * This class can hold a query
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public interface QueryHolder {
	/**
	 * @return a query
	 */
	public Query getQuery();
}
