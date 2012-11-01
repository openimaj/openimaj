package org.openimaj.rdf.storm.sparql.topology.builder;

import java.util.HashMap;
import java.util.Set;

import org.openimaj.rdf.storm.utils.CsparqlUtils.CSparqlComponentHolder;

import backtype.storm.topology.TopologyBuilder;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.PatternVars;

/**
 * A {@link SPARQLReteTopologyBuilderContext} holds variables needed by the
 * various stages of a {@link SPARQLReteTopologyBuilder}
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
 *         (ss@ecs.soton.ac.uk)
 *
 */
public class SPARQLReteTopologyBuilderContext {

	/**
	 * The query being compiled
	 */
	public CSparqlComponentHolder query;

	/**
	 * the builder
	 */
	public TopologyBuilder builder;
	/**
	 * the initial source of tuples
	 */
	public String source;

	/**
	 * the current element
	 */
	public ElementPathBlock filterClause;

	/**
	 * The binding vars and their positions
	 */
	public HashMap<String, Integer> bindingVector;

	private int currentCount;

	SPARQLReteTopologyBuilderContext() {
	}

	/**
	 * @param builder
	 *            the Storm {@link TopologyBuilder}
	 * @param source
	 *            the Storm tuples
	 * @param query
	 *            the entire query being worked on
	 */
	public SPARQLReteTopologyBuilderContext(TopologyBuilder builder, String source,
			CSparqlComponentHolder query) {
		this.query = query;
		this.builder = builder;
		this.source = source;

		this.initBindingVectorMap();
	}

	private void initBindingVectorMap() {
		this.bindingVector = new HashMap<String, Integer>();
		this.currentCount = 0;
		Element elm = this.query.simpleQuery.getQueryPattern();
		Set<Var> vars = PatternVars.vars(elm);
		for (Var var : vars) {
			bindingVector.put(var.getName(), this.currentCount++);
		}
	}
}