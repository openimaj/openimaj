/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.math.graph.algorithm;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.UndirectedSubgraph;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

/**
 * Implementation of Charikar's greedy Densest-subgraph algorithm for
 * unweighted, undirected graphs.
 *  
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <V> type of vertex
 * @param <E> type of edge
 */
public class CharikarDensestSubgraph<V, E> {
	protected UndirectedGraph<V, E> graph;
	protected UndirectedSubgraph<V,E> bestSubGraph;
	protected FibonacciHeap<V> heap = new FibonacciHeap<V>();
	
	/**
	 * Compute the densest subgraph of a graph.
	 * @param graph the graph.
	 */
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
	
	protected void calculateDensestSubgraph() {
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
	
	/**
	 * @return The densest subgraph
	 */
	public UndirectedSubgraph<V, E> getDensestSubgraph() {
		return bestSubGraph;
	}
}
