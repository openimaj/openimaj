package org.openimaj.rdf.storm.sparql.topology.bolt;

import org.openimaj.rdf.storm.topology.bolt.StormReteFilterBolt;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class QueryHoldingReteFilterBolt extends StormReteFilterBolt implements QueryHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6720026054833609009L;

	/**
	 * @param rule
	 */
	public QueryHoldingReteFilterBolt(Rule rule) {
		super(rule);
	}

	/**
	 * @param queryString
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	private String queryString;
	private Query query;

	@Override
	public Query getQuery() {
		if (this.query == null) {
			this.query = QueryFactory.create(queryString);
		}
		return query;
	}

}
