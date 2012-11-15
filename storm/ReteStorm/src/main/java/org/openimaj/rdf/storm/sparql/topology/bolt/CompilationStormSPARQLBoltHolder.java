package org.openimaj.rdf.storm.sparql.topology.bolt;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.topology.bolt.CompilationStormRuleReteBoltHolder;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.sparql.syntax.Element;

/**
 * Holds {@link StormReteBolt} and {@link Rule} instances as well as the SPARQL
 * statement which matches the query until this point
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class CompilationStormSPARQLBoltHolder extends CompilationStormRuleReteBoltHolder {
	private static Logger logger = Logger.getLogger(CompilationStormRuleReteBoltHolder.class);
	private Element element;
	private String queryString;

	/**
	 * @param bolt
	 */
	public CompilationStormSPARQLBoltHolder(StormReteBolt bolt) {
		super(bolt);
	}

	/**
	 * @param bolt
	 * @param rule
	 */
	public CompilationStormSPARQLBoltHolder(StormReteBolt bolt, Rule rule) {
		super(bolt, rule);
	}

	/**
	 * The element representing this bolt and the original query (mainly for
	 * prefix information)
	 * 
	 * @param elementPathBlock
	 * @param originalQuery
	 */
	public void setElement(Element elementPathBlock, Query originalQuery) {
		this.element = elementPathBlock;
		constructQuery(originalQuery);
	}

	/**
	 * @return the element representing the held bolt
	 */
	public Element getElement() {
		return this.element;
	}

	private void constructQuery(Query originalQuery) {
		Query query = QueryFactory.create(originalQuery);
		query.setQueryPattern(element);
		query.setQuerySelectType();
		query.addResultVar(Node.createVariable("*"));
		queryString = query.toString();
		try {
			QueryFactory.create(queryString);
		} catch (QueryParseException e) {
			throw e;
		}
		logger.debug("Query string constructed!");
	}

	/**
	 * @return the full query constructed from the element held in the bolt
	 */
	public String getQueryString() {
		return this.queryString;
	}

}
