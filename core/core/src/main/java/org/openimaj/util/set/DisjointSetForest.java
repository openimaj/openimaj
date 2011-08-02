package org.openimaj.util.set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisjointSetForest<T> {
	class Node {
		int rank;
		T parent;

		Node(T parent, int rank) {
			this.parent = parent;
			this.rank = rank;
		}
	}

	private final Map<T, Node> data = new HashMap<T, Node>();

	public T find(T x) {
		Node xNode = data.get(x);

		if (xNode == null)
			return null;

		if (x == xNode.parent)
			return x;
			
		xNode.parent = find(xNode.parent);

		return xNode.parent;
	}

	public T makeSet(T o) {
		if (data.containsKey(o)) return null;
		
		data.put(o, new Node(o, 0));
		
		return o;
	}

	public void union(T x, T y) {
		T xRoot = find(x);
		T yRoot = find(y);

		if (xRoot == yRoot || xRoot == null || yRoot == null)
			return;

		Node xNode = data.get(xRoot);
		Node yNode = data.get(yRoot);

		// x and y are not already in same set. Merge them.
		if (xNode.rank < yNode.rank) {
			xNode.parent = yRoot;
		} else if (xNode.rank > yNode.rank) {
			yNode.parent = xRoot;
		} else {
			yNode.parent = xRoot;
			xNode.rank++;
		}		
	}

	public List<T> asList() {
		return new ArrayList<T>(data.keySet());
	}
}