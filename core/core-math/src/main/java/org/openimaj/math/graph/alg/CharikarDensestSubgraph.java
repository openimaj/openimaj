package org.openimaj.math.graph.alg;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.UndirectedSubgraph;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

/**
 * Implementation of Charikar's greedy Densest-subgraph algorithm for
 * unweighted, undirected graphs.
 *  
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <V> type of vertex
 * @param <E> type of edge
 */
public class CharikarDensestSubgraph<V, E> {
	protected UndirectedGraph<V, E> graph;
	protected UndirectedSubgraph<V,E> bestSubGraph;
	protected FibonacciHeap<V> heap = new FibonacciHeap<V>();
	
	public CharikarDensestSubgraph(UndirectedGraph<V, E> graph) {
		this.graph = graph;
		
		for (V vertex : graph.vertexSet())
			heap.insert(new FibonacciHeapNode<V>(vertex), graph.degreeOf(vertex));
		
		calculateDensestSubgraph();
	}
	
	protected V getMinDegreeVertexBruteForce(UndirectedGraph<V, E> graph) {
		int minDegree = Integer.MAX_VALUE;
		V minDegreeVertex = null;
		
		for (V vertex : graph.vertexSet()) {
			int degree = graph.degreeOf(vertex);
			if (degree < minDegree) {
				minDegreeVertex = vertex;
				break;
			}
		}
		
		return minDegreeVertex;
	}
	
	protected V getMinDegreeVertex(UndirectedGraph<V, E> graph) {		
		return heap.removeMin().getData();
	}
	
	public void calculateDensestSubgraph() {
		UndirectedSubgraph<V,E> currentSubGraph = new UndirectedSubgraph<V,E>(graph, graph.vertexSet(), null);
		double bestDensity = calculateDensity(graph);
		
		while (currentSubGraph.vertexSet().size() > 0) {
			currentSubGraph = new UndirectedSubgraph<V,E>(graph, currentSubGraph.vertexSet(), null);
			currentSubGraph.removeVertex(getMinDegreeVertex(currentSubGraph));
			double density = calculateDensity(currentSubGraph);
			
			if (density > bestDensity) {
				bestDensity = density;
				bestSubGraph = currentSubGraph;
			}
		}
	}
	
	/**
	 * Calculate the density of the graph as the
	 * number of edges divided by the number of vertices
	 * @param g the graph
	 * @return the density
	 */
	public static double calculateDensity(Graph<?,?> g) {
		return (double)g.edgeSet().size() / (double)g.vertexSet().size();
	}
	
	public UndirectedSubgraph<V, E> getDensestSubgraph() {
		return bestSubGraph;
	}	
	
	public static void main(String[] args) {
		UndirectedGraph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		
		g.addVertex("V1");
		g.addVertex("V2");
		g.addVertex("V3");
		g.addVertex("V4");
		g.addVertex("V5");
		g.addVertex("V6");
		
		g.addEdge("V1", "V2");
		g.addEdge("V1", "V3");
		g.addEdge("V3", "V4");
		g.addEdge("V3", "V5");
		g.addEdge("V4", "V5");
		g.addEdge("V4", "V6");
		g.addEdge("V5", "V6");
		
		System.out.println(g);
		CharikarDensestSubgraph<String, DefaultEdge> d = new CharikarDensestSubgraph<String, DefaultEdge>(g);
		System.out.println(d.getDensestSubgraph());
	}
}
