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
package org.openimaj.util.set;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a Disjoint Set Forest data structure.
 * <p>
 * A disjoint set holds a set of elements partitioned into a number of
 * non-overlapping subsets. The disjoint set forest holds each subset as a tree
 * structure, with the representative of each subset being the root of the
 * respective tree.
 * <p>
 * See <a href="http://en.wikipedia.org/wiki/Disjoint-set_data_structure">
 * http://en.wikipedia.org/wiki/Disjoint-set_data_structure</a> for more
 * information.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the type of elements maintained by this set
 */
public class DisjointSetForest<T> implements Set<T> {
	class Node {
		int rank;
		T parent;

		Node(T parent, int rank) {
			this.parent = parent;
			this.rank = rank;
		}
	}

	private Map<T, Node> data;
	private TObjectIntHashMap<T> counts = new TObjectIntHashMap<T>();

	/**
	 * Construct a new Disjoint Set Forest.
	 */
	public DisjointSetForest() {
		data = new HashMap<T, Node>();
	}

	/**
	 * Constructs a new Disjoint Set Forest, with the specified initial
	 * capacity.
	 * 
	 * @param initialCapacity
	 *            The initial capacity
	 */
	public DisjointSetForest(int initialCapacity) {
		data = new HashMap<T, Node>(initialCapacity);
	}

	/**
	 * Search for the representative of the subset containing the element x.
	 * 
	 * This implementation uses path compression to flatten the trees during the
	 * find operation. This will lead to performance improvements on subsequent
	 * find operations.
	 * 
	 * @param x
	 *            the element
	 * @return the representative element or null if x isn't in the forest.
	 */
	public T find(T x) {
		final Node xNode = data.get(x);

		if (xNode == null)
			return null;

		if (x == xNode.parent)
			return x;

		xNode.parent = find(xNode.parent);

		return xNode.parent;
	}

	/**
	 * Make a new subset from the given item
	 * 
	 * @param o
	 *            the item
	 * @return the item if the subset was created successfully; null otherwise.
	 */
	public T makeSet(T o) {
		if (data.containsKey(o))
			return null;

		data.put(o, new Node(o, 0));
		counts.put(o, 1);

		return o;
	}

	/**
	 * Joint the subsets belonging to the objects x and y.
	 * 
	 * @param x
	 *            the x object.
	 * @param y
	 *            the y object.
	 * @return the new root, or null if x or y are not in the disjoint set
	 *         forest, or x and y are already in the same set.
	 */
	public T union(T x, T y) {
		final T xRoot = find(x);
		final T yRoot = find(y);

		if (xRoot == yRoot || xRoot == null || yRoot == null)
			return null;

		final Node xNode = data.get(xRoot);
		final Node yNode = data.get(yRoot);

		// x and y are not already in same set. Merge them.
		if (xNode.rank < yNode.rank) {
			xNode.parent = yRoot;
			counts.adjustValue(yRoot, counts.remove(xRoot));

			return yRoot;
		} else if (xNode.rank > yNode.rank) {
			yNode.parent = xRoot;
			counts.adjustValue(xRoot, counts.remove(yRoot));

			return xRoot;
		} else {
			yNode.parent = xRoot;
			xNode.rank++;
			counts.adjustValue(xRoot, counts.remove(yRoot));
			return xRoot;
		}
	}

	/**
	 * @return the contents of the forest as a list
	 */
	public List<T> asList() {
		return new ArrayList<T>(data.keySet());
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return data.containsKey(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator<T> iterator = data.keySet().iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("not supported");
			}

		};
	}

	@Override
	public Object[] toArray() {
		return data.keySet().toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return data.keySet().toArray(a);
	}

	@Override
	public boolean add(T e) {
		return makeSet(e) == e;
	}

	/**
	 * Add an element to the given set.
	 * 
	 * @param elem
	 *            Element to add
	 * @param repr
	 *            A representative of the set to add to
	 * @return The representative of the set containing the added element, or
	 *         null if elem is already contained in this set.
	 * @throws IllegalArgumentException
	 *             If repr is not an element of this forest.
	 */
	public T add(T elem, T repr) {
		if (repr == null)
			return makeSet(elem);

		if (data.containsKey(elem))
			return null;
		if (!data.containsKey(repr))
			throw new IllegalArgumentException("Set containing representative not found");

		add(elem);

		return union(repr, elem);
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		for (final Object o : collection)
			if (!data.containsKey(o))
				return false;

		return true;
	}

	/**
	 * Add all the elements in the collection to this disjoint set forest. Each
	 * element will be added to its own set.
	 * 
	 * @param collection
	 *            The collection to add
	 * @return true is any elements were added; false otherwise.
	 */
	@Override
	public boolean addAll(Collection<? extends T> collection) {
		boolean changed = false;
		for (final T c : collection) {
			if (makeSet(c) == c) {
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Add all the elements in the collection to this disjoint set forest.
	 * 
	 * @param collection
	 *            The collection to add
	 * @param sameSet
	 *            Are the elements in the collection to be added to the same
	 *            set?
	 * 
	 * @return true is any elements were added; false otherwise.
	 */
	public boolean addAll(Collection<? extends T> collection, boolean sameSet) {
		if (collection == null || collection.isEmpty())
			return false;

		if (!sameSet)
			return addAll(collection);

		T root = null;
		for (final T c : collection) {
			if (root == null)
				root = makeSet(c);
			else
				add(c, root);
		}
		return root != null;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void clear() {
		data.clear();
	}

	/**
	 * Get the size of a subset
	 * 
	 * @param o
	 *            an object in the subset
	 * @return the number of objects in the subset
	 */
	public int size(T o) {
		return counts.get(find(o));
	}

	/**
	 * Get the number of subsets
	 * 
	 * @return the number of subset
	 */
	public int numSets() {
		return counts.size();
	}

	/**
	 * Get all the subsets stored in this forest.
	 * 
	 * @return subsets
	 */
	public Set<Set<T>> getSubsets() {
		final Map<T, Set<T>> set = new HashMap<T, Set<T>>();

		for (final T t : this) {
			final T repr = find(t);

			Set<T> reprSet = set.get(repr);
			if (reprSet == null)
				set.put(repr, reprSet = new HashSet<T>());
			reprSet.add(t);
		}

		return new HashSet<Set<T>>(set.values());
	}

	/**
	 * Construct a {@link DisjointSetForest} by partitioning the given values
	 * using the given {@link Comparator} to determine equality of pairs of
	 * values.
	 * 
	 * @param <T>
	 *            the type of elements maintained by this set
	 * @param values
	 *            the values to partition
	 * @param comparator
	 *            the comparator to determine equality of values
	 * @return the new {@link DisjointSetForest} representing the partitioning
	 */
	public static <T> DisjointSetForest<T> partition(List<T> values, Comparator<T> comparator) {
		final DisjointSetForest<T> forest = new DisjointSetForest<T>(values.size());
		forest.addAll(values);

		final int size = values.size();
		for (int i = 0; i < size; i++) {
			final T vi = values.get(i);

			for (int j = i + 1; j < size; j++) {
				final T vj = values.get(j);

				if (comparator.compare(vi, vj) == 0)
					forest.union(vi, vj);
			}
		}

		return forest;
	}

	/**
	 * Extract the subsets formed by constructing a {@link DisjointSetForest}
	 * which partitions the given values using the given {@link Comparator} to
	 * determine equality of pairs of values.
	 * 
	 * @param <T>
	 *            the type of elements maintained by this set
	 * @param values
	 *            the values to partition
	 * @param comparator
	 *            the comparator to determine equality of values
	 * @return the subsets representing the partitioning
	 */
	public static <T> Set<Set<T>> partitionSubsets(List<T> values, Comparator<T> comparator) {
		return partition(values, comparator).getSubsets();
	}
}
