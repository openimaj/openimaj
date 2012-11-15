package org.openimaj.rdf.storm.sparql.topology.builder.datasets;

import java.io.Serializable;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Static RDF dataset
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface StaticRDFDataset extends Serializable {
	/**
	 * Given the query, return a results set
	 * @param query
	 * @return the results - assume the query is a select
	 */
	public ResultSet performQuery(Query query);

	/**
	 * Whether a given query return results. This query may be a SELECT or an ASK
	 * @param query
	 * @return whether the query will return
	 */
	public boolean potentialResults(Query query);

	/**
	 *
	 */
	public void prepare();

	/**
	 * Given the query and a partial set of solutions for some if its bindings, return a results set
	 * @param siblingQuery
	 * @param solution
	 * @return the results - assumes the query is a select
	 */
	public ResultSet performQuery(Query siblingQuery, QuerySolution solution);


}
