package org.openimaj.rdf.storm.sparql.topology.bolt;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.query.Query;

/**
 * Given a SELECT SPARQL query, output the bindings as name,value pairs.
 * Values may be blank if parts of the query are OPTIONAL.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormSPARQLReteSelectConflictSetBolt extends StormSPARQLReteConflictSetBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = -4996437363510534715L;

	/**
	 * @param query
	 */
	public StormSPARQLReteSelectConflictSetBolt(Query query) {
		super(query);
	}

	@Override
	public void execute(Tuple input) {
		System.out.println(input);
	}

}
