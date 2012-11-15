package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.normalisation.HellingerNormaliser;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.feature.local.keypoints.FloatKeypoint;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.knn.pq.FloatProductQuantiser;
import org.openimaj.knn.pq.FloatProductQuantiserUtilities;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.hard.ExactFloatAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.ml.pca.FeatureVectorPCA;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;

import scala.actors.threadpool.Arrays;

public class VLADPrep {
	List<File> localFeatures;
	boolean normalise = false;

	File centroidsDataFile;
	File pcaDataFile;
	File pqDataFile;

	int numVladCentroids = 64;
	int numIterations = 100;
	private int numPcaDims = 128;
	private int numPqIterations = 100;
	private int numPqAssigners = 8;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		final VLADPrep vp = new VLADPrep();
		vp.localFeatures = Arrays.asList(new File("/Users/jsh2/Data/ukbench/sift/").listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				final String name = pathname.getName().replace("ukbench", "").replace(".jpg", "");

				return (Integer.parseInt(name) < 300);
			}
		}));

		vp.process();
	}

	void process() throws IOException {
		// Load the data and normalise
		System.out.println("Loading Data from " + localFeatures.size() + " files");
		final List<List<FloatKeypoint>> rawData = new ArrayList<List<FloatKeypoint>>();
		final int numVectors = loadData(rawData);

		// cluster
		System.out.println("Clustering " + numVectors + " Data Points");
		final FloatCentroidsResult centroids = cluster(rawData, numVectors);

		// build vlads
		System.out.println("Building VLADs");
		final VLAD<float[]> vlad = new VLAD<float[]>(new ExactFloatAssigner(centroids), centroids, normalise);
		final List<MultidimensionalFloatFV> vlads = computeVLADs(rawData, vlad);

		// learn PCA basis
		System.out.println("Learning PCA basis");
		final FeatureVectorPCA pca = new FeatureVectorPCA(new ThinSvdPrincipalComponentAnalysis(numPcaDims));
		pca.learnBasis(vlads);

		// project features
		System.out.println("Projecting with PCA");
		final float[][] pcaVlads = projectFeatures(pca, vlads);

		// learn PQs
		System.out.println("Learning Product Quantiser Parameters");
		final FloatProductQuantiser pq = FloatProductQuantiserUtilities.train(pcaVlads, numPqAssigners, numPqIterations);

		// save everything
	}

	private float[][] projectFeatures(final FeatureVectorPCA pca, List<MultidimensionalFloatFV> vlads) {
		final List<float[]> pcaVlads = new ArrayList<float[]>();
		Parallel.forEach(vlads, new Operation<MultidimensionalFloatFV>() {
			@Override
			public void perform(MultidimensionalFloatFV vector) {
				final DoubleFV result = pca.project(vector);
				final float[] fresult = ArrayUtils.doubleToFloat(result.values);

				synchronized (pcaVlads) {
					pcaVlads.add(fresult);
				}
			}
		});

		return pcaVlads.toArray(new float[pcaVlads.size()][]);
	}

	private List<MultidimensionalFloatFV> computeVLADs(List<List<FloatKeypoint>> rawData, final VLAD<float[]> vlad) {
		final List<MultidimensionalFloatFV> vlads = new ArrayList<MultidimensionalFloatFV>();

		Parallel.forEach(rawData, new Operation<List<FloatKeypoint>>() {
			@Override
			public void perform(List<FloatKeypoint> keys) {
				final MultidimensionalFloatFV feature = vlad.aggregate(keys);

				synchronized (vlads) {
					vlads.add(feature);
				}
			}
		});

		return vlads;
	}

	private int loadData(final List<List<FloatKeypoint>> rawData) {
		final AtomicInteger numVectors = new AtomicInteger();
		Parallel.forEach(localFeatures, new Operation<File>() {
			@Override
			public void perform(File file) {
				try {
					final MemoryLocalFeatureList<Keypoint> keys = MemoryLocalFeatureList.read(file, Keypoint.class);
					final MemoryLocalFeatureList<FloatKeypoint> fkeys = FloatKeypoint.convert(keys);

					for (final FloatKeypoint k : fkeys) {
						HellingerNormaliser.normalise(k.vector, 0);
					}

					synchronized (rawData) {
						rawData.add(fkeys);
						numVectors.addAndGet(fkeys.size());
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});
		return numVectors.intValue();
	}

	private FloatCentroidsResult cluster(List<List<FloatKeypoint>> rawData, int numVectors) {
		// build full data array
		final float[][] vectors = new float[numVectors][];
		int i = 0;
		for (final List<FloatKeypoint> keys : rawData) {
			for (final FloatKeypoint k : keys) {
				vectors[i] = k.vector;
				i++;
			}
		}

		// Perform clustering
		final FloatKMeans kmeans = FloatKMeans.createExact(vectors[0].length, numVladCentroids, numIterations);
		final FloatCentroidsResult centroids = kmeans.cluster(vectors);

		return centroids;
	}

}
