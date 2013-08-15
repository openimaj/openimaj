package org.openimaj.demos;

import gnu.trove.procedure.TIntObjectProcedure;
import jal.objects.BinaryPredicate;
import jal.objects.Sorting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.openimaj.util.array.IntArrayView;
import org.openimaj.util.pair.DoubleIntPair;
import org.openimaj.util.pair.IntDoublePair;

import scala.actors.threadpool.Arrays;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

public class FastKDTree {
	/**
	 * Interface for describing how a branch in the KD-Tree should be created
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static interface SplitChooser {
		/**
		 * Choose the dimension and discriminant on which to split the data.
		 * 
		 * @param pnts
		 *            the raw data
		 * @param inds
		 *            the indices of the data under consideration
		 * @param depth
		 *            the depth of the current data in the tree
		 * @return the dimension and discriminant, or null iff this is a leaf
		 *         (containing all the points in inds).
		 */
		public IntDoublePair chooseSplit(final double[][] pnts, final IntArrayView inds, int depth);
	}

	/**
	 * Randomised best-bin-first splitting strategy:
	 * <ul>
	 * <li>Nodes with less than a set number of items become leafs.
	 * <li>Otherwise:
	 * <ul>
	 * <li>a sample of the data is taken and the variance across each dimension
	 * is computed.
	 * <li>a dimension is chosen randomly from the dimensions with the higest
	 * variance.
	 * <li>the mean (computed from the variance sample) is taken as the split
	 * point.
	 * </ul>
	 * </ul>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static class RandomisedBestBinFirstMeanSplit implements SplitChooser {
		/**
		 * Maximum number of items in a leaf.
		 */
		private static final int maxLeafSize = 14;

		/**
		 * Maximum number of points of variance estimation; all points used if
		 * <=0.
		 */
		private static final int varianceMaxPoints = 128;

		/**
		 * Number of dimensions to consider when randomly selecting one with a
		 * big variance.
		 */
		private static final int randomMaxDims = 5;

		/**
		 * The random source
		 */
		private Uniform rng;

		/**
		 * Construct with the default values of 14 points per leaf (max), 128
		 * samples for computing variance, and the 5 most varying dimensions
		 * randomly selected. A new {@link MersenneTwister} is created as the
		 * source for random numbers.
		 */
		public RandomisedBestBinFirstMeanSplit() {
			this.rng = new Uniform(new MersenneTwister());
		}

		/**
		 * Construct with the default values of 14 points per leaf (max), 128
		 * samples for computing variance, and the 5 most varying dimensions
		 * randomly selected. A new {@link MersenneTwister} is created as the
		 * source for random numbers.
		 * 
		 * @param uniform
		 *            the random number source
		 */
		public RandomisedBestBinFirstMeanSplit(Uniform uniform) {
			this.rng = uniform;
		}

		/**
		 * Construct with the given values.
		 * 
		 * @param maxLeafSize
		 *            Maximum number of items in a leaf.
		 * @param varianceMaxPoints
		 *            Maximum number of points of variance estimation; all
		 *            points used if <=0.
		 * @param randomMaxDims
		 *            Number of dimensions to consider when randomly selecting
		 *            one with a big variance.
		 * @param uniform
		 *            the random number source
		 */
		public RandomisedBestBinFirstMeanSplit(int maxLeafSize, int varianceMaxPoints, int randomMaxDims, Uniform uniform)
		{
			this.rng = uniform;
		}

		@Override
		public IntDoublePair chooseSplit(final double[][] pnts, final IntArrayView inds, int depth) {
			if (inds.size() < maxLeafSize)
				return null;

			final int D = pnts[0].length;

			// Find mean & variance of each dimension.
			final double[] sumX = new double[D];
			final double[] sumXX = new double[D];

			final int count = Math.min(inds.size(), varianceMaxPoints);
			for (int n = 0; n < count; ++n) {
				for (int d = 0; d < D; ++d) {
					final int i = inds.getFast(n);

					sumX[d] += pnts[i][d];
					sumXX[d] += (pnts[i][d] * pnts[i][d]);
				}
			}

			final DoubleIntPair[] varPerDim = new DoubleIntPair[D];
			for (int d = 0; d < D; ++d) {
				varPerDim[d] = new DoubleIntPair();
				varPerDim[d].second = d;

				if (count <= 1)
					varPerDim[d].first = 0;
				else
					varPerDim[d].first = (sumXX[d] - ((double) 1 / count) * sumX[d] * sumX[d]) / (count - 1);
			}

			// Partial sort makes a BIG difference to the build time.
			final int nrand = Math.min(randomMaxDims, D);
			Sorting.partial_sort(varPerDim, 0, nrand, varPerDim.length, new BinaryPredicate() {
				@Override
				public boolean apply(Object arg0, Object arg1) {
					final DoubleIntPair p1 = (DoubleIntPair) arg0;
					final DoubleIntPair p2 = (DoubleIntPair) arg1;

					if (p1.first > p2.first)
						return true;
					if (p2.first > p1.first)
						return false;
					return (p1.second > p2.second);
				}
			});

			final int randd = varPerDim[rng.nextIntFromTo(0, nrand - 1)].second;

			return new IntDoublePair(randd, sumX[randd] / count);
		}
	}

	/**
	 * An internal node of the KDTree
	 */
	public static class KDTreeNode {
		/**
		 * Node to the left
		 */
		public KDTreeNode left;

		/**
		 * Node to the right
		 */
		public KDTreeNode right;

		/**
		 * Splitting value
		 */
		public double discriminant;

		/**
		 * Splitting dimension
		 */
		public int discriminantDimension;

		/**
		 * The minimum bounds of this node
		 */
		public double[] minBounds;

		/**
		 * The maximum bounds of this node
		 */
		public double[] maxBounds;

		/**
		 * The leaf only holds the indices of the original data
		 */
		public int[] indices;

		/**
		 * Construct a new node with the given data
		 * 
		 * @param pnts
		 *            the data for the node and its children
		 * @param inds
		 *            a list of indices that point to the relevant parts of the
		 *            pnts array that should be used
		 * @param split
		 *            the {@link SplitChooser} to use when constructing the tree
		 */
		public KDTreeNode(final double[][] pnts, IntArrayView inds, SplitChooser split) {
			this(pnts, inds, split, 0, null, true);
		}

		private KDTreeNode(final double[][] pnts, IntArrayView inds, SplitChooser split, int depth, KDTreeNode parent,
				boolean isLeft)
		{
			final IntDoublePair spl = split.chooseSplit(pnts, inds, depth);

			discriminantDimension = spl.first;
			discriminant = spl.second;

			// partially sort the inds so that all the data with
			// data[discriminantDimension] < discriminant is on one side
			final int N = inds.size();
			int l = 0;
			int r = N;
			while (l != r) {
				if (pnts[inds.getFast(l)][discriminantDimension] < discriminant)
					l++;
				else {
					r--;
					final int t = inds.getFast(l);
					inds.setFast(l, inds.getFast(r));
					inds.setFast(r, t);
				}
			}

			// If either partition is empty then the are vectors identical.
			// Choose the midpoint to keep the O(nlog(n)) performance.
			if (l == 0 || l == N) {
				l = N / 2;
			}

			// set the bounds of this node
			if (parent == null) {
				this.minBounds = new double[pnts[0].length];
				this.maxBounds = new double[pnts[0].length];
				Arrays.fill(minBounds, -Double.MAX_VALUE);
				Arrays.fill(maxBounds, Double.MAX_VALUE);
			} else {
				this.minBounds = parent.minBounds.clone();
				this.maxBounds = parent.maxBounds.clone();

				if (isLeft) {
					maxBounds[parent.discriminantDimension] = parent.discriminant;
				} else {
					minBounds[parent.discriminantDimension] = parent.discriminant;
				}
			}

			// create the child nodes
			left = new KDTreeNode(pnts, inds.subView(0, l), split, depth + 1, this, true);
			right = new KDTreeNode(pnts, inds.subView(l, N), split, depth + 1, this, false);
		}

		/**
		 * Test to see if this node is a leaf node (i.e.
		 * <code>{@link #indices} != null</code>)
		 * 
		 * @return true if this is a leaf node; false otherwise
		 */
		public boolean isLeaf() {
			return indices == null;
		}

		private final boolean inRange(double value, double min, double max) {
			return (value >= min) && (value <= max);
		}

		/**
		 * Test whether the bounds of this node are disjoint from the
		 * hyperrectangle described by the given bounds.
		 * 
		 * @param lowerExtreme
		 *            the lower bounds of the hyperrectangle
		 * @param upperExtreme
		 *            the upper bounds of the hyperrectangle
		 * @return true if disjoint; false otherwise
		 */
		public boolean isDisjointFrom(double[] lowerExtreme, double[] upperExtreme) {
			for (int i = 0; i < lowerExtreme.length; i++) {
				if (!(inRange(minBounds[i], lowerExtreme[i], upperExtreme[i]) || inRange(lowerExtreme[i], minBounds[i],
						maxBounds[i])))
					return true;
			}

			return false;
		}

		/**
		 * Test whether the bounds of this node are fully contained by the
		 * hyperrectangle described by the given bounds.
		 * 
		 * @param lowerExtreme
		 *            the lower bounds of the hyperrectangle
		 * @param upperExtreme
		 *            the upper bounds of the hyperrectangle
		 * @return true if fully contained; false otherwise
		 */
		public boolean isContainedBy(double[] lowerExtreme, double[] upperExtreme) {
			for (int i = 0; i < lowerExtreme.length; i++) {
				if (minBounds[i] < lowerExtreme[i] || maxBounds[i] > upperExtreme[i])
					return false;
			}
			return true;
		}

		// static void search(final double[] qu,
		// PriorityQueue<DoubleObjectPair<KDTreeNode>> pri_branch,
		// List<IntDoublePair> nns, boolean[] seen, double[][] pnts, double
		// mindsq)
		// {
		// KDTreeNode cur = this;
		// KDTreeNode other = null;
		//
		// while (!cur.isLeaf()) { // Follow best bin first until we hit a
		// // leaf
		// final double diff = qu[cur.discriminantDimension]
		// - cur.discriminant;
		//
		// if (diff < 0) {
		// other = cur.right;
		// cur = cur.left;
		// }
		// else {
		// other = cur.left;
		// cur = cur.right;
		// }
		//
		// pri_branch.add(new DoubleObjectPair<KDTreeNode>(mindsq + diff * diff,
		// other));
		// }
		//
		// final int[] cur_inds = cur.indices;
		// final int ncur_inds = cur_inds.length;
		//
		// int i;
		// final double[] dsq = new double[1];
		// for (i = 0; i < ncur_inds; ++i) {
		// final int ci = cur_inds[i];
		// if (!seen[ci]) {
		// DoubleNearestNeighbours.distanceFunc(qu, new double[][] { pnts[ci] },
		// dsq);
		//
		// nns.add(new IntDoublePair(ci, dsq[0]));
		//
		// seen[ci] = true;
		// }
		// }
		// }
	}

	/** The tree roots */
	public final KDTreeNode root;

	/** The underlying data array */
	public final double[][] data;

	public FastKDTree(double[][] data, SplitChooser split) {
		this.data = data;
		this.root = new KDTreeNode(data, new IntArrayView(data.length), split);
	}

	/**
	 * Search the tree for all points contained within the bounding box defined
	 * by the given upper and lower extremes
	 * 
	 * @param lowerExtreme
	 * @param upperExtreme
	 * @return the points within the given bounds
	 */
	public List<double[]> coordinateRangeSearch(double[] lowerExtreme, double[] upperExtreme) {
		final List<double[]> results = new ArrayList<double[]>();

		rangeSearch(lowerExtreme, upperExtreme, new TIntObjectProcedure<double[]>() {
			@Override
			public boolean execute(int a, double[] b) {
				results.add(b);

				return true;
			}
		});

		return results;
	}

	public void rangeSearch(double[] lowerExtreme, double[] upperExtreme, TIntObjectProcedure<double[]> proc) {
		final Deque<KDTreeNode> stack = new ArrayDeque<KDTreeNode>();

		if (root == null)
			return;

		stack.push(root);

		while (!stack.isEmpty()) {
			final KDTreeNode tmpNode = stack.pop();

			if (tmpNode.isLeaf()) {
				for (int i = 0; i < tmpNode.indices.length; i++) {
					final int idx = tmpNode.indices[i];
					final double[] vec = data[idx];
					if (isContained(vec, lowerExtreme, upperExtreme))
						if (!proc.execute(idx, vec))
							return;
				}
			} else {

				if (tmpNode.isDisjointFrom(lowerExtreme, upperExtreme)) {
					continue;
				}

				if (tmpNode.isContainedBy(lowerExtreme, upperExtreme)) {
					reportSubtree(tmpNode, proc);
				} else {
					if (tmpNode.left != null)
						stack.push(tmpNode.left);
					if (tmpNode.right != null)
						stack.push(tmpNode.right);
				}
			}
		}
	}

	/**
	 * Determines if a point is contained within a given k-dimensional bounding
	 * box.
	 */
	private final boolean isContained(double[] point, double[] lower, double[] upper)
	{
		for (int i = 0; i < point.length; i++) {
			if (point[i] < lower[i] || point[i] > upper[i])
				return false;
		}

		return true;
	}

	/**
	 * Report all the child items of the given subtree to the process
	 * 
	 * @param root
	 *            the root of the subtree
	 * @param proc
	 *            the process to apply
	 */
	private void reportSubtree(KDTreeNode root, TIntObjectProcedure<double[]> proc) {
		final Deque<KDTreeNode> stack = new ArrayDeque<KDTreeNode>();
		stack.push(root);

		while (!stack.isEmpty()) {
			final KDTreeNode tmpNode = stack.pop();

			if (tmpNode.isLeaf()) {
				for (int i = 0; i < tmpNode.indices.length; i++) {
					final int idx = tmpNode.indices[i];
					if (!proc.execute(idx, data[idx]))
						return;
				}
			} else {
				if (tmpNode.left != null)
					stack.push(tmpNode.left);
				if (tmpNode.right != null)
					stack.push(tmpNode.right);
			}
		}
	}
}
