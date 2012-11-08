package org.openimaj.demos.sandbox.rete;

import java.util.HashMap;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.openimaj.storm.util.graph.StormGraphCreator.NamedNode;

import com.mxgraph.view.mxGraph;

public class mxGraphUtils {

	public static mxGraph fromJGraphT(ListenableDirectedGraph<NamedNode, DefaultEdge> graph) {
		mxGraph mxgraph = new mxGraph();
		HashMap<NamedNode, Object> cells = new HashMap<NamedNode,Object>();
		for (NamedNode vert : graph.vertexSet()) {
			cells.put(vert,mxgraph.insertVertex(mxgraph.getDefaultParent(), null, vert, 0, 0, 50, 50));

		}
		for (DefaultEdge e : graph.edgeSet()) {
			mxgraph.insertEdge(mxgraph.getDefaultParent(), null, e, cells.get(graph.getEdgeSource(e)), cells.get(graph.getEdgeTarget(e)));
		}
		return mxgraph;
	}

}
