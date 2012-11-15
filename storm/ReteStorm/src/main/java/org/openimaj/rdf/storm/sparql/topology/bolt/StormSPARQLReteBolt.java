package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.PatternVars;

/**
 * A {@link StormReteBolt} which has some specific support for rules
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class StormSPARQLReteBolt extends StormReteBolt implements QueryHolder {

	/**
	 *
	 */
	private static Logger logger = Logger.getLogger(StormSPARQLReteBolt.class);
	private static final long serialVersionUID = -3708647278860206257L;
	private String queryString;
	private int variableCount;
	protected Map<String, Map<String, Integer>> sourceVariableMap;
	private Query query;

	/**
	 * @param query
	 *            The query backing this bolt
	 */
	public StormSPARQLReteBolt(Query query) {
		sourceVariableMap = new HashMap<String, Map<String, Integer>>();
		this.queryString = query.toString();
		this.variableCount = countVariables(query);
	}

	/**
	 * The SPARQL query is null and the variable count is unset
	 */
	public StormSPARQLReteBolt() {
		sourceVariableMap = new HashMap<String, Map<String, Integer>>();
		this.queryString = null;
		this.variableCount = -1;
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
	 * For a given source, register its name and the associated order of
	 * variables to expect from the source
	 *
	 * @param source
	 *            the name of the source
	 * @param variables
	 *            the variables (in order) which the source returns
	 */
	public void registerSourceVariables(String source, String[] variables) {
		Map<String, Integer> varMap = new HashMap<String, Integer>();
		for (int i = 0; i < variables.length; i++) {
			varMap.put(variables[i], i);
		}
		sourceVariableMap.put(source, varMap);
	}

	/**
	 * Given a Tuple and its source, assign a {@link Binding} instance
	 * with the variables from the binding. If the tuple's source has no
	 * set variable order, the DEFAULT_SOURCE is attempted. If the default
	 * variable order is unset, this function will throw a RuntimeException
	 *
	 *
	 * @param t
	 * @return a new {@link Binding}
	 */
	public Binding tupleToBinding(Tuple t) {
		BindingMap binding = new BindingMap();
		Map<String, Integer> vars = null;
		vars = sourceVariableMap.get(t.getSourceComponent());
		if (vars == null) {
			logger.error("Can't create binding for tuple, source variables unset!");
			throw new RuntimeException("Variable order unknown.");
		}
		for (Entry<String, Integer> var : vars.entrySet()) {
			Node n = (Node) t.getValue(var.getValue());
			binding.add(Var.alloc(var.getKey()), n);
		}
		return binding;
	}

	/**
	 * Get the rule on which this {@link StormReteBolt} is built.
	 *
	 * @return Query
	 */
	@Override
	public Query getQuery() {
		if (this.query == null) {
			this.query = QueryFactory.create(queryString);
		}
		return this.query;
	}

	/**
	 * sets the query string an sets query to null (getQuery() constructs a new
	 * Query)
	 *
	 * @param queryString
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
		this.query = null;
	}

	public static Object extractBindings(Tuple env) {
		// TODO Auto-generated method stub
		return null;
	}

}
