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
package org.openimaj.rdf.storm.topology;

import java.io.Serializable;

import com.esotericsoftware.kryo.Kryo;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * Given a list of {@link Node} instances, hold them in a way which is
 * serializable by {@link Kryo} and
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SerialisableNodes implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 623768130999279028L;

	/**
	 * Something which wraps a node in a form that can be serialised. This default
	 * assumes the Node is a {@link Node_URI}
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class SerialisableNode implements Serializable{

		/**
		 *
		 */
		private static final long serialVersionUID = 870843958445630763L;
		private String node;
		private SerialisableNode() {}
		/**
		 * @param node the {@link Node} to serialise
		 */
		public SerialisableNode(Node node) {
			this.node = node.toString();
		}
		/**
		 * @return reconstruct the node
		 */
		public Node getNode(){
			return Node.createURI(node);
		}
	}



	/**
	 * Wraps a {@link Node_RuleVariable} instance as something which {@link Kryo} can
	 * serialise.
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class SerialisableNode_RuleVariable extends SerialisableNode{
		/**
		 *
		 */
		private static final long serialVersionUID = -6540464808569472116L;
		private String label;
		private int index;

		/**
		 * @param node the node to serialise
		 */
		public SerialisableNode_RuleVariable(Node_RuleVariable node) {
			label = node.toString(null,true);
			index = node.getIndex();
		}


		@Override
		public Node getNode(){
			if(label.equals("*")){
				return Node_RuleVariable.WILD;
			}
			else{
				return new Node_RuleVariable(label, index);
			}
		}
	}

	private SerialisableNode[] inner;

	/**
	 * @param nodes make this list of nodes serialisable
	 */
	public SerialisableNodes(Node... nodes) {
		inner = new SerialisableNode[nodes.length];
		int i = 0;
		for (Node node : nodes) {
			if(node instanceof Node_RuleVariable){
				inner[i++] = new SerialisableNode_RuleVariable((Node_RuleVariable)node);
			}
			else{
				inner[i++] = new SerialisableNode(node);
			}
		}
	}

	/**
	 * @return from the {@link SerialisableNode} list return a {@link Node} array
	 */
	public Node[] getNodes(){
		Node[] retNodes = new Node[inner.length];
		int i = 0;
		for (SerialisableNode node : this.inner) {
			retNodes[i++] = node.getNode();
		}
		return retNodes;
	}
}
