package org.openimaj.ml.clustering.meanshift;

import gnu.trove.procedure.TIntObjectProcedure;

import java.util.List;
import java.util.Set;

import org.openimaj.math.statistics.distribution.MultivariateKernelDensityEstimate;
import org.openimaj.util.pair.ObjectDoublePair;
import org.openimaj.util.set.DisjointSetForest;
import org.openimaj.util.tree.DoubleKDTree;

/**
 * Exact mean shift implementation. The mean shift procedure is applied to every
 * underlying point. This can be quite slow, especially with many points.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ExactMeanShift {
	private int maxIter = 300;

	private MultivariateKernelDensityEstimate kde;
	private int[] assignments;

	private double[][] modes;
	public int[] counts;

	/**
	 * Perform the ExactMeanShift operation on the given KDE.
	 * 
	 * @param kde
	 */
	public ExactMeanShift(MultivariateKernelDensityEstimate kde) {
		this.kde = kde;

		performMeanShift();
	}

	private void performMeanShift() {
		final double[][] data = kde.getData();
		final double[][] modePerPoint = new double[data.length][];

		// perform the MS procedure on each point
		for (int i = 0; i < data.length; i++) {
			final double[] point = data[i].clone();

			for (int iter = 0; iter < maxIter; iter++) {
				if (computeMeanShift(point))
					break;
			}
			modePerPoint[i] = point;
		}

		// now need to merge modes that are <bandwidth away
		mergeModes(modePerPoint);
	}

	/**
	 * Get the modes
	 * 
	 * @return the modes
	 */
	public double[][] getModes() {
		return modes;
	}

	/**
	 * Get the assignments
	 * 
	 * @return the assignments
	 */
	public int[] getAssignments() {
		return assignments;
	}

	protected void mergeModes(double[][] modePerPoint) {
		final DisjointSetForest<double[]> forest = new DisjointSetForest<double[]>();

		for (int i = 0; i < modePerPoint.length; i++)
			forest.makeSet(modePerPoint[i]);

		final DoubleKDTree tree = new DoubleKDTree(modePerPoint);
		for (int i = 0; i < modePerPoint.length; i++) {
			final double[] point = modePerPoint[i];

			tree.radiusSearch(modePerPoint[i], kde.getScaledBandwidth(), new TIntObjectProcedure<double[]>() {
				@Override
				public boolean execute(int a, double[] b) {
					forest.union(point, b);
					return true;
				}
			});
		}

		final Set<Set<double[]>> subsets = forest.getSubsets();
		this.assignments = new int[modePerPoint.length];
		this.modes = new double[subsets.size()][];
		this.counts = new int[subsets.size()];
		int current = 0;
		for (final Set<double[]> s : subsets) {
			this.modes[current] = new double[modePerPoint[0].length];

			for (int i = 0; i < modePerPoint.length; i++) {
				if (s.contains(modePerPoint[i])) {
					assignments[i] = current;
					for (int j = 0; j < modes[current].length; j++) {
						modes[current][j] = modePerPoint[i][j];
					}
				}
			}
			this.counts[current] = s.size();
			for (int j = 0; j < modes[current].length; j++) {
				modes[current][j] /= counts[current];
			}
			current++;
		}
	}

	protected boolean computeMeanShift(double[] pt) {
		final List<ObjectDoublePair<double[]>> support = kde.getSupport(pt);

		if (support.size() == 1) {
			return true;
		}

		double sum = 0;
		final double[] out = new double[pt.length];
		for (final ObjectDoublePair<double[]> p : support) {
			sum += p.second;

			for (int j = 0; j < out.length; j++) {
				out[j] += p.second * p.first[j];
			}
		}

		double dist = 0;
		for (int j = 0; j < out.length; j++) {
			out[j] /= sum;
			dist += (pt[j] - out[j]) * (pt[j] - out[j]);
		}

		System.arraycopy(out, 0, pt, 0, out.length);

		return dist < 1e-3 * kde.getBandwidth();
	}
}
