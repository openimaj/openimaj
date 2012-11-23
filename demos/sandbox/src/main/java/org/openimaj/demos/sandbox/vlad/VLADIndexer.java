package org.openimaj.demos.sandbox.vlad;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.feature.local.keypoints.FloatKeypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.FloatProductQuantiser;
import org.openimaj.ml.pca.FeatureVectorPCA;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntObjectPair;

public class VLADIndexer {
	public VLAD<float[]> vlad;
	public FeatureVectorPCA pca;
	public FloatProductQuantiser pq;

	public VLADIndexer(VLAD<float[]> vlad, FeatureVectorPCA pca, FloatProductQuantiser pq) {
		this.vlad = vlad;
		this.pca = pca;
		this.pq = pq;
	}

	public void save(File file) throws IOException {
		IOUtils.writeToFile(this, file);
	}

	public static VLADIndexer read(File file) throws IOException {
		return IOUtils.readFromFile(file);
	}

	public float[] extract(List<FloatKeypoint> normalisedKeypoints) {
		final MultidimensionalFloatFV keys = vlad.aggregate(normalisedKeypoints);

		if (keys == null)
			return null;

		final DoubleFV subspaceVector = pca.project(keys).normaliseFV(2);
		return ArrayUtils.doubleToFloat(subspaceVector.values);
	}

	public List<IntObjectPair<float[]>> index(List<FloatKeypoint> normalisedKeypoints, int id,
			List<IntObjectPair<float[]>> index)
	{
		if (index == null)
			index = new ArrayList<IntObjectPair<float[]>>();

		index.add(new IntObjectPair<float[]>(id, extract(normalisedKeypoints)));

		return index;
	}

	public static VLADIndexer read(InputStream is) throws IOException {
		return IOUtils.read(new DataInputStream(is));
	}
}
