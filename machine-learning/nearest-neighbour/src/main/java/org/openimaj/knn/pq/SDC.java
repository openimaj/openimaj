package org.openimaj.knn.pq;

import org.openimaj.feature.FloatFVComparison;
import org.openimaj.knn.FloatNearestNeighbours;

public class SDC extends FloatADCNearestNeighbours {
	float[][][] distances;

	public SDC(FloatProductQuantiser pq, float[][] dataPoints) {
		super(pq, dataPoints);

		this.distances = new float[pq.assigners.length][][];

		for (int i = 0; i < pq.assigners.length; i++) {
			final FloatNearestNeighbours nn = pq.assigners[i];
			final float[][] centroids = nn.getPoints();

			distances[i] = new float[centroids.length][centroids.length];

			for (int j = 0; j < centroids.length; j++) {
				for (int k = j; k < centroids.length; k++) {
					distances[i][j][k] = (float) FloatFVComparison.SUM_SQUARE.compare(centroids[j], centroids[k]);
					distances[i][k][j] = distances[i][j][k];
				}
			}
		}
	}

	@Override
	protected void computeDistances(float[] floatQuery, float[] dsqout) {
		final byte[] query = pq.quantise(floatQuery);

		for (int i = 0; i < data.length; i++) {
			dsqout[i] = 0;
			for (int j = 0; j < query.length; j++) {
				dsqout[i] += distances[j][query[j] + 128][data[i][j] + 128];
			}
		}
	}
}
