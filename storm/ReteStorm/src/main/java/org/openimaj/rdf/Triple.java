package org.openimaj.rdf;

import org.semanticweb.yars.nx.Node;

/**
 * An N3 Triple.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Triple {

	private Node[] ns;

	/**
	 * Construct a triple
	 * @param ns
	 */
	public Triple(Node[] ns) {
		this.ns = ns;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (Node n : ns) {
			buf.append(" ");
			buf.append(n.toN3());
		}
		return buf.toString();
	}

}
