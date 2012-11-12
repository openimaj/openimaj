package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * handles SPARQL queries that are
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StormSPARQLReteConstructConflictSetBolt extends StormSPARQLReteConflictSetBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = -1337401778771700226L;
	private Template template;

	/**
	 * @param query
	 */
	public StormSPARQLReteConstructConflictSetBolt(Query query) {
		super(query);
	}

	@Override
	public void prepare() {
		super.prepare();
		this.template = this.getQuery().getConstructTemplate();
	}

	@Override
	public void handleBinding(QueryIterator bindingsIter) {
		for (; bindingsIter.hasNext();) {
			Binding binding = bindingsIter.next();
			ArrayList<Triple> triples = new ArrayList<Triple>();
			Map<Node, Node> bNodeMap = new HashMap<Node, Node>();
			this.template.subst(triples, bNodeMap, binding);
			for (Triple triple : triples) {
				super.emitTriple(triple);
			}
		}
	}

}
