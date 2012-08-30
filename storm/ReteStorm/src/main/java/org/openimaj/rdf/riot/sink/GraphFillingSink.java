package org.openimaj.rdf.riot.sink;

import org.openjena.atlas.lib.Sink;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;

/**
 * A graph is filled with triples from the sink
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GraphFillingSink implements Sink<Triple> {
	private final Graph graph;

	/**
	 * @param graph the grpah to fill
	 */
	public GraphFillingSink(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void close() {}

	@Override
	public void send(Triple item) {
		graph.add(item);
	}

	@Override
	public void flush() {}
}