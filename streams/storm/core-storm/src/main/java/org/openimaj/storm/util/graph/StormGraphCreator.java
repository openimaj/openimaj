/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.storm.util.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.openimaj.storm.util.graph.StormGraphCreator.NamingStrategy.DefaultNamingStrategy;

import backtype.storm.generated.Bolt;
import backtype.storm.generated.ComponentCommon;
import backtype.storm.generated.GlobalStreamId;
import backtype.storm.generated.Grouping;
import backtype.storm.generated.SpoutSpec;
import backtype.storm.generated.StormTopology;

/**
 * Create {@link DirectedGraph} instances from {@link StormTopology} instances
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormGraphCreator {
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public interface NamingStrategy {
		/**
		 * @param name
		 *            the id of a {@link ComponentCommon}
		 * @return the name to render
		 */
		public String name(String name);

		/**
		 * Uses id as name
		 *
		 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
		 *
		 */
		public static class DefaultNamingStrategy implements NamingStrategy {

			@Override
			public String name(String name) {
				return name;
			}

		}

		/**
		 * Names {@link ComponentCommon} as A, B, C... and provides a lookup map
		 *
		 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
		 *
		 */
		public static class AlphabeticNamingStrategy implements NamingStrategy {
			char currentLetter = 'A';
			/**
			 * maps names to their original name
			 */
			public Map<String, String> lookup = new HashMap<String, String>();
			private Map<String, String> seen = new HashMap<String, String>();

			@Override
			public String name(String name) {
				if (!seen.containsKey(name)) {
					final String newName = new String(new char[] { currentLetter++ });
					seen.put(name, newName);
					lookup.put(newName, name);
				}
				return seen.get(name);
			}

		}
	}

	enum Type {
		SPOUT, BOLT;
	}

	private HashMap<String, NamedNode> nns;
	private NamingStrategy namingStrategy;

	private StormGraphCreator() {
		nns = new HashMap<String, NamedNode>();
		namingStrategy = new DefaultNamingStrategy();
	}

	private StormGraphCreator(NamingStrategy strat) {
		nns = new HashMap<String, NamedNode>();
		namingStrategy = strat;
	}

	/**
	 * A name and type node
	 *
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public class NamedNode {
		/**
		 * name of the node
		 */
		public String name;
		/**
		 * the node's type
		 */
		public Type node;

		/**
		 * @param name
		 * @param node
		 */
		public NamedNode(String name, Type node) {
			this.name = namingStrategy.name(name);
			this.node = node;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof NamedNode))
				return false;
			final NamedNode that = (NamedNode) other;
			if (that.node != this.node)
				return false;
			return this.hashCode() == that.hashCode();
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public String toString() {
			return "<" + name + ">";
		}
	}

	private ListenableDirectedGraph<NamedNode, DefaultEdge> _asGraph(StormTopology t) {
		final Map<String, Bolt> bolts = t.get_bolts();
		final Map<String, SpoutSpec> spouts = t.get_spouts();
		final ListenableDirectedGraph<NamedNode, DefaultEdge> ret = new ListenableDirectedGraph<NamedNode, DefaultEdge>(
				DefaultEdge.class);

		createSpouts(spouts, ret);
		createBolts(bolts, ret);
		createConnections(bolts, ret);
		return ret;
	}

	private void createConnections(Map<String, Bolt> bolts, ListenableDirectedGraph<NamedNode, DefaultEdge> ret) {
		for (final Entry<String, Bolt> boltspec : bolts.entrySet()) {
			final Bolt bolt = boltspec.getValue();
			final String id = boltspec.getKey();
			final Map<GlobalStreamId, Grouping> inputs = bolt.get_common().get_inputs();
			for (final Entry<GlobalStreamId, Grouping> input : inputs.entrySet()) {
				final GlobalStreamId from = input.getKey();
				// Grouping grouping = input.getValue();
				final String fromId = from.get_componentId();
				// String streamId = from.get_streamId();
				ret.addEdge(nns.get(fromId), nns.get(id));
			}

		}
	}

	private void createSpouts(Map<String, SpoutSpec> spouts, ListenableDirectedGraph<NamedNode, DefaultEdge> ret) {
		for (final Entry<String, SpoutSpec> spoutEntries : spouts.entrySet()) {
			final String name = spoutEntries.getKey();
			if (!nns.containsKey(name))
				nns.put(name, new NamedNode(name, Type.SPOUT));
			ret.addVertex(nns.get(name));
		}
	}

	private void createBolts(Map<String, Bolt> bolts, ListenableDirectedGraph<NamedNode, DefaultEdge> ret) {
		for (final Entry<String, Bolt> boltEntries : bolts.entrySet()) {
			final String name = boltEntries.getKey();
			if (!nns.containsKey(name))
				nns.put(name, new NamedNode(name, Type.BOLT));
			ret.addVertex(nns.get(name));
		}
	}

	/**
	 * @param t
	 *            {@link StormTopology} to graph
	 * @return a {@link ListenableDirectedGraph} usable with JGraph
	 */
	public static ListenableDirectedGraph<NamedNode, DefaultEdge> asGraph(StormTopology t) {
		final StormGraphCreator creator = new StormGraphCreator();
		return creator._asGraph(t);
	}

	/**
	 * @param t
	 *            {@link StormTopology} to graph
	 * @param strat
	 *            the naming strategy
	 * @return a {@link ListenableDirectedGraph} usable with JGraph
	 */
	public static ListenableDirectedGraph<NamedNode, DefaultEdge> asGraph(StormTopology t, NamingStrategy strat) {
		final StormGraphCreator creator = new StormGraphCreator(strat);
		return creator._asGraph(t);
	}
}
