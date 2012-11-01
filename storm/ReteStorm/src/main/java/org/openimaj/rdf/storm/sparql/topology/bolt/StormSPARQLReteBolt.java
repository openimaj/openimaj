package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.Set;

import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.PatternVars;

/**
 * A {@link StormReteBolt} which has some specific support for rules
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class StormSPARQLReteBolt extends StormReteBolt{

	/**
	 *
	 */
	private static final long serialVersionUID = -3708647278860206257L;
	private String queryString;
	private int variableCount;

	/**
	 * @param query The query backing this bolt
	 */
	public StormSPARQLReteBolt(Query query) {
		this.queryString = query.toString();
		this.variableCount = countVariables(query);
	}

	private int countVariables(Query query) {
		Element elm = query.getQueryPattern();
		Set<Var> vars = PatternVars.vars(elm);
		return vars.size();
	}

	@Override
	public int getVariableCount() {
		return this.variableCount;
	}

	/**
	 * Get the rule on which this {@link StormReteBolt} is built.
	 *
	 * @return Query
	 */
	public Query getQuery() {
		return QueryFactory.create(queryString);
	}

}
