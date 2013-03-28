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
package org.openimaj.demos.sandbox.vlad;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.random.MersenneTwister;
import org.openimaj.data.RandomData;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.filter.ByteEntropyFilter;
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
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.function.Operation;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.parallel.Parallel;

import Jama.Matrix;

public class VLADPrep {
	List<File> localFeatures;
	boolean normalise = false;

	int numVladCentroids = 64;
	int numIterations = 100;
	private int numPcaDims = 128;
	private int numPqIterations = 100;
	private int numPqAssigners = 16;

	private float sampleProp = 0.1f;

	private File outputFile;
	protected boolean entropyFilter = false;
	protected boolean hellinger = true;

	public static void main(String[] args) throws IOException {
		final VLADPrep vp = new VLADPrep();
		vp.localFeatures = Arrays.asList(new File("/Volumes/Raid/mirflickr/sift-1x/").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".sift");
			}
		}));
		vp.outputFile = new File("/Users/jsh2/vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x.dat");
		// vp.localFeatures = Arrays.asList(new
		// File("/Users/jsh2/Data/ukbench/sift/").listFiles());
		// vp.outputFile = new
		// File("/Users/jsh2/vlad-indexer-ukbench-2x-nohell.dat");
		vp.process();
	}

	void process() throws IOException {
		// Load the data and normalise
		System.out.println("Loading Data from " + localFeatures.size() + " files");
		final List<FloatKeypoint> samples = loadSample();

		// cluster
		System.out.println("Clustering " + samples.size() + " Data Points");
		final FloatCentroidsResult centroids = cluster(samples);

		// build vlads
		System.out.println("Building VLADs");
		final VLAD<float[]> vlad = new VLAD<float[]>(new ExactFloatAssigner(centroids), centroids, normalise);
		final List<MultidimensionalFloatFV> vlads = computeVLADs(vlad);

		// learn PCA basis
		System.out.println("Learning PCA basis");
		final FeatureVectorPCA pca = new FeatureVectorPCA(new ThinSvdPrincipalComponentAnalysis(numPcaDims));
		pca.learnBasis(vlads);

		// perform whitening to balance variance; roll into pca basis
		System.out.println("Apply random whitening to normalise variances");
		final Matrix whitening = createRandomWhitening(numPcaDims);
		pca.getBasis().setMatrix(0, numPcaDims - 1, 0, numPcaDims - 1, pca.getBasis().times(whitening));

		// project features
		System.out.println("Projecting with PCA");
		final float[][] pcaVlads = projectFeatures(pca, vlads);

		// learn PQs
		System.out.println("Learning Product Quantiser Parameters");
		final FloatProductQuantiser pq = FloatProductQuantiserUtilities.train(pcaVlads, numPqAssigners, numPqIterations);

		// save everything
		final VLADIndexer indexer = new VLADIndexer(vlad, pca, pq);
		indexer.save(outputFile);
	}

	private Matrix createRandomWhitening(final int ndims) {
		final Matrix m = new Matrix(ndims, ndims);
		final double[][] a = m.getArray();
		final double[] norms = new double[ndims];

		final MersenneTwister mt = new MersenneTwister();

		for (int r = 0; r < ndims; r++) {
			for (int c = 0; c < ndims; c++) {
				a[r][c] = mt.nextGaussian();
				norms[r] += (a[r][c] * a[r][c]);
			}
		}

		for (int r = 0; r < ndims; r++) {
			final double norm = Math.sqrt(norms[r]);

			for (int c = 0; c < ndims; c++) {
				a[r][c] /= norm;
			}
		}

		return m;
	}

	private float[][] projectFeatures(final FeatureVectorPCA pca, List<MultidimensionalFloatFV> vlads) {
		final List<float[]> pcaVlads = new ArrayList<float[]>();
		Parallel.forEach(vlads, new Operation<MultidimensionalFloatFV>() {
			@Override
			public void perform(MultidimensionalFloatFV vector) {
				final DoubleFV result = pca.project(vector).normaliseFV(2);
				final float[] fresult = ArrayUtils.doubleToFloat(result.values);

				synchronized (pcaVlads) {
					pcaVlads.add(fresult);
				}
			}
		});

		return pcaVlads.toArray(new float[pcaVlads.size()][]);
	}

	private List<MultidimensionalFloatFV> computeVLADs(final VLAD<float[]> vlad) {
		final List<MultidimensionalFloatFV> vlads = new ArrayList<MultidimensionalFloatFV>();

		Parallel.forEach(localFeatures, new Operation<File>() {
			@Override
			public void perform(File file) {
				try {
					List<Keypoint> keys = MemoryLocalFeatureList.read(file, Keypoint.class);
					if (entropyFilter)
						keys = FilterUtils.filter(keys, new ByteEntropyFilter());
					final List<FloatKeypoint> fkeys = FloatKeypoint.convert(keys);

					if (hellinger) {
						for (final FloatKeypoint k : fkeys) {
							HellingerNormaliser.normalise(k.vector, 0);
						}
					}

					final MultidimensionalFloatFV feature = vlad.aggregate(fkeys);

					synchronized (vlads) {
						if (feature != null)
							vlads.add(feature);
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});

		return vlads;
	}

	private List<FloatKeypoint> loadSample() {
		final List<FloatKeypoint> samples = new ArrayList<FloatKeypoint>();

		Parallel.forEach(localFeatures, new Operation<File>() {
			@Override
			public void perform(File file) {
				try {
					List<Keypoint> keys = MemoryLocalFeatureList.read(file, Keypoint.class);
					if (entropyFilter)
						keys = FilterUtils.filter(keys, new ByteEntropyFilter());
					final List<FloatKeypoint> fkeys = FloatKeypoint.convert(keys);

					final int[] indices = RandomData.getUniqueRandomInts((int) (fkeys.size() * sampleProp), 0,
							fkeys.size());
					final AcceptingListView<FloatKeypoint> filtered = new AcceptingListView<FloatKeypoint>(fkeys, indices);

					if (hellinger) {
						for (final FloatKeypoint k : filtered) {
							HellingerNormaliser.normalise(k.vector, 0);
						}
					}

					synchronized (samples) {
						samples.addAll(filtered);
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});

		return samples;
	}

	private FloatCentroidsResult cluster(List<FloatKeypoint> rawData) {
		// build full data array
		final float[][] vectors = new float[rawData.size()][];
		for (int i = 0; i < vectors.length; i++) {
			vectors[i] = rawData.get(i).vector;
		}

		// Perform clustering
		final FloatKMeans kmeans = FloatKMeans.createExact(vectors[0].length, numVladCentroids, numIterations);
		final FloatCentroidsResult centroids = kmeans.cluster(vectors);

		return centroids;
	}

}
