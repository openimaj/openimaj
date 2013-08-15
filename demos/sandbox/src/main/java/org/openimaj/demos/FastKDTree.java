package org.openimaj.demos;

import jal.objects.BinaryPredicate;
import jal.objects.Sorting;

import org.openimaj.util.array.IntArrayView;
import org.openimaj.util.pair.DoubleIntPair;
import org.openimaj.util.pair.IntDoublePair;

import cern.jet.random.Uniform;

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

	public static class BestBinFirstMeanSplit implements SplitChooser {
		/**
		 * Maximum number of items in a leaf
		 */
		private static final int maxLeafSize = 14;

		/**
		 * Maximum number of points of variance estimation; all points used if
		 * <=0
		 */
		private static final int varianceMaxPoints = 128;

		private static final int varest_max_randsz = 5;

		private Uniform rng;

		@Override
		public IntDoublePair chooseSplit(final double[][] pnts, final IntArrayView inds, int depth) {
			if (inds.size() < maxLeafSize)
				return null;

			final int D = pnts[0].length;

			// Find mean & variance of each dimension.
			final double[] sum_x = new double[D];
			final double[] sum_xx = new double[D];

			final int count = Math.min(inds.size(), varianceMaxPoints);
			for (int n = 0; n < count; ++n) {
				for (int d = 0; d < D; ++d) {
					sum_x[d] += pnts[inds.getFast(n)][d];
					sum_xx[d] += (pnts[inds.getFast(n)][d] * pnts[inds.getFast(n)][d]);
				}
			}

			final DoubleIntPair[] var_dim = new DoubleIntPair[D];
			for (int d = 0; d < D; ++d) {
				var_dim[d] = new DoubleIntPair();
				var_dim[d].second = d;

				if (count <= 1)
					var_dim[d].first = 0;
				else
					var_dim[d].first = (sum_xx[d] - ((double) 1 / count) * sum_x[d] * sum_x[d]) / (count - 1);
			}

			// Partial sort makes a BIG difference to the build time.
			final int nrand = Math.min(varest_max_randsz, D);
			Sorting.partial_sort(var_dim, 0, nrand, var_dim.length, new BinaryPredicate() {
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

			final int randd = var_dim[rng.nextIntFromTo(0, nrand - 1)].second;

			return new IntDoublePair(randd, sum_x[randd] / count);
		}
	}

	/**
	 * An internal node of the KDTree
	 */
	private static class KDTreeNode {
		/**
		 * Node to the left
		 */
		private KDTreeNode left;

		/**
		 * Node to the right
		 */
		KDTreeNode right;

		/**
		 * Splitting value
		 */
		double discriminant;

		/**
		 * Splitting dimension
		 */
		int discriminantDimension;

		/**
		 * The minimum bounds of this node
		 */
		double[] minBounds;

		/**
		 * The maximum bounds of this node
		 */
		double[] maxBounds;

		/**
		 * The leaf only holds the indices of the original data
		 */
		int[] indices;

		/**
		 * Construct a new node with the given data
		 * 
		 * @param pnts
		 *            the data for the node and its children
		 * @param inds
		 *            a list of indices that point to the relevant parts of the
		 *            pnts array that should be used
		 */
		public KDTreeNode(final double[][] pnts, IntArrayView inds, SplitChooser split) {
			this(pnts, inds, split, 0);
		}

		private KDTreeNode(final double[][] pnts, IntArrayView inds, SplitChooser split, int depth) {
			final IntDoublePair spl = split.chooseSplit(pnts, inds, depth);

			discriminantDimension = spl.first;
			discriminant = spl.second;

			// shift the inds so that all the data with
			// data[discriminantDimension] < discriminant
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

			left = new KDTreeNode(pnts, inds.subView(0, l), split, depth + 1);
			right = new KDTreeNode(pnts, inds.subView(l, N), split, depth + 1);
		}

		private boolean isLeaf() {
			return indices == null;
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
}
