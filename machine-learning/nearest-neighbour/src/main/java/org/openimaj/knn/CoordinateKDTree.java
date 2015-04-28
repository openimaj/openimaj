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
package org.openimaj.knn;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Stack;

import org.openimaj.math.geometry.point.Coordinate;

class KDNode<T extends Coordinate> {
	int _discriminate;
	T _point;
	KDNode<T> _left, _right;

	KDNode(T point, int discriminate) {
		_point = point;
		_left = _right = null;
		_discriminate = discriminate;
	}
}

/**
 * Implementation of a simple KDTree with range search. The KDTree allows fast
 * search for points in relatively low-dimension spaces.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            the type of Coordinate.
 */
public class CoordinateKDTree<T extends Coordinate> implements CoordinateIndex<T> {
	KDNode<T> _root;

	/**
	 * Create an empty KDTree object
	 */
	public CoordinateKDTree() {
		_root = null;
	}

	/**
	 * Create a KDTree object and populate it with the given data.
	 * 
	 * @param coords
	 *            the data to populate the index with.
	 */
	public CoordinateKDTree(Collection<T> coords) {
		_root = null;
		insertAll(coords);
	}

	/**
	 * Insert all the points from the given collection into the index.
	 * 
	 * @param coords
	 *            The points to add.
	 */
	public void insertAll(Collection<T> coords) {
		for (final T c : coords)
			insert(c);
	}

	/**
	 * Inserts a point into the tree, preserving the spatial ordering.
	 * 
	 * @param point
	 *            Point to insert.
	 */
	@Override
	public void insert(T point) {

		if (_root == null)
			_root = new KDNode<T>(point, 0);
		else {
			int discriminate, dimensions;
			KDNode<T> curNode, tmpNode;
			double ordinate1, ordinate2;

			curNode = _root;

			do {
				tmpNode = curNode;
				discriminate = tmpNode._discriminate;

				ordinate1 = point.getOrdinate(discriminate).doubleValue();
				ordinate2 = tmpNode._point.getOrdinate(discriminate).doubleValue();

				if (ordinate1 > ordinate2)
					curNode = tmpNode._right;
				else
					curNode = tmpNode._left;
			} while (curNode != null);

			dimensions = point.getDimensions();

			if (++discriminate >= dimensions)
				discriminate = 0;

			if (ordinate1 > ordinate2)
				tmpNode._right = new KDNode<T>(point, discriminate);
			else
				tmpNode._left = new KDNode<T>(point, discriminate);
		}
	}

	/**
	 * Determines if a point is contained within a given k-dimensional bounding
	 * box.
	 */
	static final boolean isContained(
			Coordinate point, Coordinate lower, Coordinate upper)
	{
		int dimensions;
		double ordinate1, ordinate2, ordinate3;

		dimensions = point.getDimensions();

		for (int i = 0; i < dimensions; ++i) {
			ordinate1 = point.getOrdinate(i).doubleValue();
			ordinate2 = lower.getOrdinate(i).doubleValue();
			ordinate3 = upper.getOrdinate(i).doubleValue();

			if (ordinate1 < ordinate2 || ordinate1 > ordinate3)
				return false;
		}

		return true;
	}

	/**
	 * Searches the tree for all points contained within a given k-dimensional
	 * bounding box and stores them in a Collection.
	 * 
	 * @param results
	 * @param lowerExtreme
	 * @param upperExtreme
	 */
	@Override
	public void rangeSearch(Collection<T> results, Coordinate lowerExtreme, Coordinate upperExtreme)
	{
		KDNode<T> tmpNode;
		final Stack<KDNode<T>> stack = new Stack<KDNode<T>>();
		int discriminate;
		double ordinate1, ordinate2;

		if (_root == null)
			return;

		stack.push(_root);

		while (!stack.empty()) {
			tmpNode = stack.pop();
			discriminate = tmpNode._discriminate;

			ordinate1 = tmpNode._point.getOrdinate(discriminate).doubleValue();
			ordinate2 = lowerExtreme.getOrdinate(discriminate).doubleValue();

			if (ordinate1 > ordinate2 && tmpNode._left != null)
				stack.push(tmpNode._left);

			ordinate2 = upperExtreme.getOrdinate(discriminate).doubleValue();

			if (ordinate1 < ordinate2 && tmpNode._right != null)
				stack.push(tmpNode._right);

			if (isContained(tmpNode._point, lowerExtreme, upperExtreme))
				results.add(tmpNode._point);
		}
	}

	protected static final float distance(Coordinate a, Coordinate b) {
		float s = 0;

		for (int i = 0; i < a.getDimensions(); i++) {
			final float fa = a.getOrdinate(i).floatValue();
			final float fb = b.getOrdinate(i).floatValue();
			s += (fa - fb) * (fa - fb);
		}
		return s;
	}

	class NNState implements Comparable<NNState> {
		T best;
		float bestDist;

		@Override
		public int compareTo(NNState o) {
			if (bestDist < o.bestDist)
				return 1;
			if (bestDist > o.bestDist)
				return -1;
			return 0;
		}

		@Override
		public String toString() {
			return bestDist + "";
		}
	}

	/**
	 * Find the nearest neighbour. Only one neighbour will be returned - if
	 * multiple neighbours share the same location, or are equidistant, then
	 * this might not be the one you expect.
	 * 
	 * @param query
	 *            query coordinate
	 * @return nearest neighbour
	 */
	@Override
	public T nearestNeighbour(Coordinate query) {
		final Stack<KDNode<T>> stack = walkdown(query);
		final NNState state = new NNState();
		state.best = stack.peek()._point;
		state.bestDist = distance(query, state.best);

		if (state.bestDist == 0)
			return state.best;

		while (!stack.isEmpty()) {
			final KDNode<T> current = stack.pop();

			checkSubtree(current, query, state);
		}

		return state.best;
	}

	@Override
	public void kNearestNeighbour(Collection<T> result, Coordinate query, int k) {
		final Stack<KDNode<T>> stack = walkdown(query);
		final PriorityQueue<NNState> state = new PriorityQueue<NNState>(k);

		final NNState initialState = new NNState();
		initialState.best = stack.peek()._point;
		initialState.bestDist = distance(query, initialState.best);
		state.add(initialState);

		while (!stack.isEmpty()) {
			final KDNode<T> current = stack.pop();

			checkSubtreeK(current, query, state, k);
		}

		@SuppressWarnings("unchecked")
		final NNState[] stateList = state.toArray((NNState[]) Array.newInstance(NNState.class, state.size()));
		Arrays.sort(stateList);
		for (int i = stateList.length - 1; i >= 0; i--)
			result.add(stateList[i].best);
	}

	/*
	 * Check a subtree for a closer match
	 */
	private void checkSubtree(KDNode<T> node, Coordinate query, NNState state) {
		if (node == null)
			return;

		final float dist = distance(query, node._point);
		if (dist < state.bestDist) {
			state.best = node._point;
			state.bestDist = dist;
		}

		if (state.bestDist == 0)
			return;

		final float d = node._point.getOrdinate(node._discriminate).floatValue() -
				query.getOrdinate(node._discriminate).floatValue();
		if (d * d > state.bestDist) {
			// check subtree
			final double ordinate1 = query.getOrdinate(node._discriminate).doubleValue();
			final double ordinate2 = node._point.getOrdinate(node._discriminate).doubleValue();

			if (ordinate1 > ordinate2)
				checkSubtree(node._right, query, state);
			else
				checkSubtree(node._left, query, state);
		} else {
			checkSubtree(node._left, query, state);
			checkSubtree(node._right, query, state);
		}
	}

	private void checkSubtreeK(KDNode<T> node, Coordinate query, PriorityQueue<NNState> state, int k) {
		if (node == null)
			return;

		final float dist = distance(query, node._point);

		boolean cont = false;
		for (final NNState s : state)
			if (s.best.equals(node._point)) {
				cont = true;
				break;
			}

		if (!cont) {
			if (state.size() < k) {
				// collect this node
				final NNState s = new NNState();
				s.best = node._point;
				s.bestDist = dist;
				state.add(s);
			} else if (dist < state.peek().bestDist) {
				// replace last node
				final NNState s = state.poll();
				s.best = node._point;
				s.bestDist = dist;
				state.add(s);
			}
		}

		final float d = node._point.getOrdinate(node._discriminate).floatValue() -
				query.getOrdinate(node._discriminate).floatValue();
		if (d * d > state.peek().bestDist) {
			// check subtree
			final double ordinate1 = query.getOrdinate(node._discriminate).doubleValue();
			final double ordinate2 = node._point.getOrdinate(node._discriminate).doubleValue();

			if (ordinate1 > ordinate2)
				checkSubtreeK(node._right, query, state, k);
			else
				checkSubtreeK(node._left, query, state, k);
		} else {
			checkSubtreeK(node._left, query, state, k);
			checkSubtreeK(node._right, query, state, k);
		}
	}

	/*
	 * walk down the tree until we hit a leaf, and return the path taken
	 */
	private Stack<KDNode<T>> walkdown(Coordinate point) {
		if (_root == null)
			return null;
		else {
			final Stack<KDNode<T>> stack = new Stack<KDNode<T>>();
			int discriminate, dimensions;
			KDNode<T> curNode, tmpNode;
			double ordinate1, ordinate2;

			curNode = _root;

			do {
				tmpNode = curNode;
				stack.push(tmpNode);
				if (tmpNode._point == point)
					return stack;
				discriminate = tmpNode._discriminate;

				ordinate1 = point.getOrdinate(discriminate).doubleValue();
				ordinate2 = tmpNode._point.getOrdinate(discriminate).doubleValue();

				if (ordinate1 > ordinate2)
					curNode = tmpNode._right;
				else
					curNode = tmpNode._left;
			} while (curNode != null);

			dimensions = point.getDimensions();

			if (++discriminate >= dimensions)
				discriminate = 0;

			return stack;
		}
	}

	class Coord implements Coordinate {
		float[] coords;

		public Coord(int i) {
			coords = new float[i];
		}

		public Coord(Coordinate c) {
			this(c.getDimensions());
			for (int i = 0; i < coords.length; i++)
				coords[i] = c.getOrdinate(i).floatValue();
		}

		@Override
		public int getDimensions() {
			return coords.length;
		}

		@Override
		public Float getOrdinate(int dimension) {
			return coords[dimension];
		}

		@Override
		public void readASCII(Scanner in) throws IOException {
			throw new RuntimeException("not implemented");
		}

		@Override
		public String asciiHeader() {
			throw new RuntimeException("not implemented");
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			throw new RuntimeException("not implemented");
		}

		@Override
		public byte[] binaryHeader() {
			throw new RuntimeException("not implemented");
		}

		@Override
		public void writeASCII(PrintWriter out) throws IOException {
			throw new RuntimeException("not implemented");
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			throw new RuntimeException("not implemented");
		}

		@Override
		public void setOrdinate(int dimension, Number value) {
			coords[dimension] = value.floatValue();
		}
	}

	/**
	 * Faster implementation of K-nearest-neighbours.
	 *
	 * @param result
	 *            Collection to hold the found coordinates.
	 * @param query
	 *            The query coordinate.
	 * @param k
	 *            The number of neighbours to find.
	 */
	public void fastKNN(Collection<T> result, Coordinate query, int k) {
		final List<T> tmp = new ArrayList<T>();
		final Coord lowerExtreme = new Coord(query);
		final Coord upperExtreme = new Coord(query);

		while (tmp.size() < k) {
			tmp.clear();
			for (int i = 0; i < lowerExtreme.getDimensions(); i++)
				lowerExtreme.coords[i] -= k;
			for (int i = 0; i < upperExtreme.getDimensions(); i++)
				upperExtreme.coords[i] += k;
			rangeSearch(tmp, lowerExtreme, upperExtreme);
		}

		final CoordinateBruteForce<T> bf = new CoordinateBruteForce<T>(tmp);
		bf.kNearestNeighbour(result, query, k);
	}
}
