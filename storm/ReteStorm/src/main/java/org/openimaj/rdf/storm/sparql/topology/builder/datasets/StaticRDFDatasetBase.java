package org.openimaj.rdf.storm.sparql.topology.builder.datasets;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Performs queries based on constructed {@link QueryExecution}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class StaticRDFDatasetBase implements StaticRDFDataset{

	/**
	 *
	 * @param query
	 * @return a {@link QueryExecution} for the {@link Query} provided
	 */
	public abstract QueryExecution createExecution(Query query) ;
	@Override
	public ResultSet performQuery(Query query) {
		QueryExecution qex = createExecution(query);
		return qex.execSelect();
	}


	@Override
	public boolean potentialResults(Query query) {
		QueryExecution qex = createExecution(query);
		return qex.execAsk();
	}

	@Override
	public ResultSet performQuery(Query query,QuerySolution sol) {
		QueryExecution qex = createExecution(query,sol);
		return qex.execSelect();
	}

	/**
	 *
	 * @param query
	 * @param sol
	 * @return a {@link QueryExecution} for the {@link Query} provided
	 */
	public abstract QueryExecution createExecution(Query query, QuerySolution sol) ;

}
