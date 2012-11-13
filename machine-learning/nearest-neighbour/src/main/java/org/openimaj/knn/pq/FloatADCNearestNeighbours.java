package org.openimaj.knn.pq;

import jal.objects.BinaryPredicate;
import jal.objects.Sorting;

import java.util.Arrays;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.knn.FloatNearestNeighbours;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.FloatIntPair;

/**
 * Nearest-neighbours using Asymmetric Distance Computation (ADC) on Product
 * Quantised vectors. In ADC, only the database points are quantised. The
 * queries themselves are not quantised. The overall distance is computed as the
 * summed distance of each subvector of the query to each corresponding
 * centroids of each database vector. For efficiency, the distance of each
 * sub-vector of a query is computed to every centroid (for the sub-vector under
 * consideration) only once, and is then cached for the lookup during the
 * computation of the distance to each database vector.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Jegou, Herve", "Douze, Matthijs", "Schmid, Cordelia" },
		title = "Product Quantization for Nearest Neighbor Search",
		year = "2011",
		journal = "IEEE Trans. Pattern Anal. Mach. Intell.",
		pages = { "117", "", "128" },
		url = "http://dx.doi.org/10.1109/TPAMI.2010.57",
		month = "January",
		number = "1",
		publisher = "IEEE Computer Society",
		volume = "33",
		customData = {
				"issn", "0162-8828",
				"numpages", "12",
				"doi", "10.1109/TPAMI.2010.57",
				"acmid", "1916695",
				"address", "Washington, DC, USA",
				"keywords", "High-dimensional indexing, High-dimensional indexing, image indexing, very large databases, approximate search., approximate search., image indexing, very large databases"
		})
public class FloatADCNearestNeighbours extends FloatNearestNeighbours {
	protected final FloatProductQuantiser pq;
	protected final int ndims;
	protected final byte[][] data;

	public FloatADCNearestNeighbours(FloatProductQuantiser pq, float[][] dataPoints) {
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
		// for (int i = 0; i < data.length; i++) {
		// dsqout[i] = 0;
		//
		// for (int j = 0, from = 0; j < this.pq.assigners.length; j++) {
		// final FloatNearestNeighboursExact nn = this.pq.assigners[j].getNN();
		// final int to = pq.assigners[j].numDimensions();
		//
		// final float[] centroid = nn.getPoints()[this.data[i][j] + 128];
		// final float[] query = Arrays.copyOfRange(fullQuery, from, from + to);
		//
		// dsqout[i] += FloatFVComparison.SUM_SQUARE.compare(query, centroid);
		//
		// from += to;
		// }
		// }

		final float[][] distances = new float[pq.assigners.length][];
		for (int j = 0, from = 0; j < this.pq.assigners.length; j++) {
			final FloatNearestNeighbours nn = this.pq.assigners[j];
			final int to = nn.numDimensions();
			final int K = nn.size();

			final float[][] qus = { Arrays.copyOfRange(fullQuery, from, from + to) };
			final int[][] idx = new int[1][K];
			final float[][] dst = new float[1][K];
			nn.searchKNN(qus, K, idx, dst);

			distances[j] = new float[K];
			for (int k = 0; k < K; k++) {
				distances[j][idx[0][k]] = dst[0][k];
			}
		}

		for (int i = 0; i < data.length; i++) {
			dsqout[i] = 0;

			for (int j = 0; j < this.pq.assigners.length; j++) {
				final int centroid = this.data[i][j] + 128;
				dsqout[i] += distances[i][centroid];
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
