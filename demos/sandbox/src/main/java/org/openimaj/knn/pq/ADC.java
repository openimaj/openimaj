package org.openimaj.knn.pq;

import jal.objects.BinaryPredicate;
import jal.objects.Sorting;

import java.util.Arrays;
import java.util.List;

import org.openimaj.feature.FloatFVComparison;
import org.openimaj.knn.FloatNearestNeighbours;
import org.openimaj.knn.FloatNearestNeighboursExact;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.FloatIntPair;

public class ADC extends FloatNearestNeighbours {
	protected final FloatProductQuantiser pq;
	protected final int ndims;
	protected final byte[][] data;

	public ADC(FloatProductQuantiser pq, float[][] dataPoints) {
		this.pq = pq;
		this.ndims = dataPoints[0].length;

		this.data = new byte[dataPoints.length][];
		for (int i = 0; i < dataPoints.length; i++) {
			data[i] = pq.quantise(dataPoints[i]);
		}
	}

	@Override
	public void searchNN(float[][] qus, int[] argmins, float[] mins) {
		final int N = qus.length;
		final float[] dsqout = new float[data.length];

		for (int n = 0; n < N; ++n) {
			computeDistances(qus[n], dsqout);

			argmins[n] = ArrayUtils.minIndex(dsqout);

			mins[n] = dsqout[argmins[n]];
		}
	}

	protected void computeDistances(float[] fullQuery, float[] dsqout) {
		for (int i = 0; i < data.length; i++) {
			dsqout[i] = 0;

			for (int j = 0, from = 0; j < this.pq.assigners.length; j++) {
				final FloatNearestNeighboursExact nn = this.pq.assigners[j].getNN();
				final int to = pq.assigners[j].numDimensions();

				final float[] centroid = nn.getPoints()[this.data[i][j] + 128];
				final float[] query = Arrays.copyOfRange(fullQuery, to, from);

				dsqout[i] += FloatFVComparison.SUM_SQUARE.compare(query, centroid);

				from += to;
			}
		}
	}

	@Override
	public void searchKNN(float[][] qus, int K, int[][] argmins, float[][] mins) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, data.length);

		final float[] dsqout = new float[data.length];
		final int N = qus.length;

		final FloatIntPair[] knn_prs = new FloatIntPair[data.length];

		for (int n = 0; n < N; ++n) {
			computeDistances(qus[n], dsqout);

			for (int p = 0; p < data.length; ++p)
				knn_prs[p] = new FloatIntPair(dsqout[p], p);

			Sorting.partial_sort(knn_prs, 0, K, knn_prs.length, new BinaryPredicate() {
				@Override
				public boolean apply(Object arg0, Object arg1) {
					final FloatIntPair p1 = (FloatIntPair) arg0;
					final FloatIntPair p2 = (FloatIntPair) arg1;

					if (p1.first < p2.first)
						return true;
					if (p2.first < p1.first)
						return false;
					return (p1.second < p2.second);
				}
			});

			for (int k = 0; k < K; ++k) {
				argmins[n][k] = knn_prs[k].second;
				mins[n][k] = knn_prs[k].first;
			}
		}
	}

	@Override
	public void searchNN(final List<float[]> qus, int[] argmins, float[] mins) {
		final int N = qus.size();
		final float[] dsqout = new float[data.length];

		for (int n = 0; n < N; ++n) {
			computeDistances(qus.get(n), dsqout);

			argmins[n] = ArrayUtils.minIndex(dsqout);

			mins[n] = dsqout[argmins[n]];
		}
	}

	@Override
	public void searchKNN(final List<float[]> qus, int K, int[][] argmins, float[][] mins) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, data.length);

		final float[] dsqout = new float[data.length];
		final int N = qus.size();

		final FloatIntPair[] knn_prs = new FloatIntPair[data.length];

		for (int n = 0; n < N; ++n) {
			computeDistances(qus.get(n), dsqout);

			for (int p = 0; p < data.length; ++p)
				knn_prs[p] = new FloatIntPair(dsqout[p], p);

			Sorting.partial_sort(knn_prs, 0, K, knn_prs.length, new BinaryPredicate() {
				@Override
				public boolean apply(Object arg0, Object arg1) {
					final FloatIntPair p1 = (FloatIntPair) arg0;
					final FloatIntPair p2 = (FloatIntPair) arg1;

					if (p1.first < p2.first)
						return true;
					if (p2.first < p1.first)
						return false;
					return (p1.second < p2.second);
				}
			});

			for (int k = 0; k < K; ++k) {
				argmins[n][k] = knn_prs[k].second;
				mins[n][k] = knn_prs[k].first;
			}
		}
	}

	@Override
	public int numDimensions() {
		return ndims;
	}

	@Override
	public int size() {
		return data.length;
	}
}
