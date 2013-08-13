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
package org.openimaj.image.indexing.vlad;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.random.MersenneTwister;
import org.openimaj.data.RandomData;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.FloatLocalFeatureAdaptor;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureExtractor;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.normalisation.HellingerNormaliser;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.knn.pq.FloatProductQuantiser;
import org.openimaj.knn.pq.FloatProductQuantiserUtilities;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.hard.ExactFloatAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.ml.pca.FeatureVectorPCA;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.parallel.Parallel;

import Jama.Matrix;

/**
 * Class for learning the data required to efficiently index images using VLAD
 * with PCA and product quantisers.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class VLADIndexerDataBuilder {
	/**
	 * Feature post-processing options
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum StandardPostProcesses
			implements
			Function<List<? extends LocalFeature<?, ?>>, List<FloatLocalFeatureAdaptor<?>>>
	{
		/**
		 * Do nothing, other than convert to float is required
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 * 
		 */
		NONE {
			@Override
			public List<FloatLocalFeatureAdaptor<?>> apply(List<? extends LocalFeature<?, ?>> in) {
				return FloatLocalFeatureAdaptor.wrapUntyped(in);
			}
		},
		/**
		 * Apply Hellinger normalisation to the converted float features
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 * 
		 */
		HELLINGER {
			private HellingerNormaliser hell = new HellingerNormaliser(0);

			@Override
			public List<FloatLocalFeatureAdaptor<?>> apply(List<? extends LocalFeature<?, ?>> in) {
				return FloatLocalFeatureAdaptor.wrapUntyped(in, hell);
			}
		}
	}

	private LocalFeatureExtractor<LocalFeature<?, ?>, MBFImage> extractor;
	private List<File> localFeatures;
	private boolean normalise = false;
	private int numVladCentroids = 64;
	private int numIterations = 100;
	private int numPcaDims = 128;
	private int numPqIterations = 100;
	private int numPqAssigners = 16;
	private float sampleProp = 0.1f;
	private float pcaSampleProp;
	private Function<List<? extends LocalFeature<?, ?>>, List<FloatLocalFeatureAdaptor<?>>> postProcess = StandardPostProcesses.NONE;

	/**
	 * Construct a {@link VLADIndexerDataBuilder} with the given parameters
	 * 
	 * @param extractor
	 *            the local feature extractor used to generate the input
	 *            features
	 * @param localFeatures
	 *            a list of file locations of the files containing the input
	 *            local features (one per image)
	 * @param normalise
	 *            should the resultant VLAD features be l2 normalised?
	 * @param numVladCentroids
	 *            the number of centroids for VLAD (~64)
	 * @param numIterations
	 *            the number of clustering iterations (~100)
	 * @param numPcaDims
	 *            the number of dimensions to project down to using PCA (~128
	 *            for normal SIFT)
	 * @param numPqIterations
	 *            the number of iterations for clustering the product quantisers
	 *            (~100)
	 * @param numPqAssigners
	 *            the number of product quantiser assigners (~16)
	 * @param sampleProp
	 *            the proportion of features to sample for the clustering the
	 *            VLAD centroids
	 * @param pcaSampleProp
	 *            the proportion of images to sample for computing the PCA basis
	 * @param postProcess
	 *            the post-processing to apply to the raw features before input
	 *            to VLAD
	 */
	public VLADIndexerDataBuilder(LocalFeatureExtractor<LocalFeature<?, ?>, MBFImage> extractor,
			List<File> localFeatures, boolean normalise, int numVladCentroids, int numIterations, int numPcaDims,
			int numPqIterations, int numPqAssigners, float sampleProp, float pcaSampleProp,
			Function<List<? extends LocalFeature<?, ?>>, List<FloatLocalFeatureAdaptor<?>>> postProcess)
	{
		super();
		this.extractor = extractor;
		this.localFeatures = localFeatures;
		this.normalise = normalise;
		this.numVladCentroids = numVladCentroids;
		this.numIterations = numIterations;
		this.numPcaDims = numPcaDims;
		this.numPqIterations = numPqIterations;
		this.numPqAssigners = numPqAssigners;
		this.sampleProp = sampleProp;
		this.pcaSampleProp = pcaSampleProp;
		this.postProcess = postProcess == null ? StandardPostProcesses.NONE : postProcess;
	}

	/**
	 * Build the {@link VLADIndexerData} using the information provided at
	 * construction time. The following steps are taken:
	 * <p>
	 * <ol>
	 * <li>A sample of the features is loaded
	 * <li>The sample is clustered using k-means
	 * <li>VLAD representations are then built for all the input images
	 * <li>PCA is performed on the VLAD features
	 * <li>Whitening is applied to the PCA basis
	 * <li>The VLAD features are projected by the basis
	 * <li>Product quantisers are learned
	 * <li>The final {@link VLADIndexerData} object is created
	 * </ol>
	 * 
	 * @return a newly learned {@link VLADIndexerData} object
	 * @throws IOException
	 */
	public VLADIndexerData buildIndexerData() throws IOException {
		final VLAD<float[]> vlad = buildVLAD();

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

		return new VLADIndexerData(vlad, pca, pq, extractor, postProcess);
	}

	/**
	 * Build a {@link VLAD} using the information provided at construction time.
	 * The following steps are taken:
	 * <p>
	 * <ol>
	 * <li>A sample of the features is loaded
	 * <li>The sample is clustered using k-means
	 * </ol>
	 * 
	 * @return the {@link VLAD}
	 */
	public VLAD<float[]> buildVLAD() {
		// Load the data and normalise
		System.out.println("Loading Data from " + localFeatures.size() + " files");
		final List<FloatLocalFeatureAdaptor<?>> samples = loadSample();

		// cluster
		System.out.println("Clustering " + samples.size() + " Data Points");
		final FloatCentroidsResult centroids = cluster(samples);

		// build vlads
		System.out.println("Building VLADs");
		return new VLAD<float[]>(new ExactFloatAssigner(centroids), centroids, normalise);
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
				final float[] fresult = ArrayUtils.convertToFloat(result.values);

				synchronized (pcaVlads) {
					pcaVlads.add(fresult);
				}
			}
		});

		return pcaVlads.toArray(new float[pcaVlads.size()][]);
	}

	private List<MultidimensionalFloatFV> computeVLADs(final VLAD<float[]> vlad) {
		final List<MultidimensionalFloatFV> vlads = new ArrayList<MultidimensionalFloatFV>();

		final int[] indices = RandomData.getUniqueRandomInts((int) (localFeatures.size() * pcaSampleProp), 0,
				localFeatures.size());
		final List<File> selectedLocalFeatures = new AcceptingListView<File>(localFeatures, indices);

		Parallel.forEach(selectedLocalFeatures, new Operation<File>() {
			@Override
			public void perform(File file) {
				try {
					final List<FloatLocalFeatureAdaptor<?>> fkeys = readFeatures(file);
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

	private List<FloatLocalFeatureAdaptor<?>> readFeatures(File file) throws IOException {
		final List<? extends LocalFeature<?, ?>> keys = MemoryLocalFeatureList.read(file, extractor.getFeatureClass());

		return postProcess.apply(keys);
	}

	private List<FloatLocalFeatureAdaptor<?>> loadSample() {
		final List<FloatLocalFeatureAdaptor<?>> samples = new ArrayList<FloatLocalFeatureAdaptor<?>>();

		Parallel.forEach(localFeatures, new Operation<File>() {
			@Override
			public void perform(File file) {
				try {
					final List<FloatLocalFeatureAdaptor<?>> fkeys = readFeatures(file);

					final int[] indices = RandomData.getUniqueRandomInts((int) (fkeys.size() * sampleProp), 0,
							fkeys.size());
					final AcceptingListView<FloatLocalFeatureAdaptor<?>> filtered = new AcceptingListView<FloatLocalFeatureAdaptor<?>>(
							fkeys, indices);

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

	private FloatCentroidsResult cluster(List<FloatLocalFeatureAdaptor<?>> rawData) {
		// build full data array
		final float[][] vectors = new float[rawData.size()][];
		for (int i = 0; i < vectors.length; i++) {
			vectors[i] = rawData.get(i).getFeatureVector().values;
		}

		// Perform clustering
		final FloatKMeans kmeans = FloatKMeans.createExact(numVladCentroids, numIterations);
		final FloatCentroidsResult centroids = kmeans.cluster(vectors);

		return centroids;
	}
}
