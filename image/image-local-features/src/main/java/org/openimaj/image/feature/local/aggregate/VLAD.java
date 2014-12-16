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
package org.openimaj.image.feature.local.aggregate;

import java.lang.reflect.Array;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.image.FImage;
import org.openimaj.ml.clustering.CentroidsProvider;
import org.openimaj.ml.clustering.assignment.HardAssigner;

/**
 * Implementation of VLAD, the "Vector of Locally Aggregated Descriptors"
 * algorithm. VLAD aggregates descriptors into a vector form based on locality
 * in feature space. Like in the {@link BagOfVisualWords}, features are assigned
 * to centroids in a codebook (usually learnt through k-means), but instead of
 * creating a vector of codeword occurances, VLAD accumulates the residual
 * vector between each input vector and its assigned centroid. Fundamentally,
 * VLAD describes the distribution of the image vectors with respect to their
 * assigned centroids. VLAD is closely related to the more complex
 * {@link FisherVector}.
 * <p>
 * For a given number of centroids, K, and vector length, D, the VLAD descriptor
 * has dimension K*D. This is obviously longer than the K-dimensional vector
 * produced by {@link BagOfVisualWords}. However, the VLAD descriptor is can be
 * useful with a much smaller K (i.e. of the order of 16-64 dimensions versus up
 * to 1 million (or more) for {@link BagOfVisualWords}).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Primitive array type of the {@link ArrayFeatureVector}s used by
 *            the {@link LocalFeature}s that will be processed.
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jegou, H.", "Douze, M.", "Schmid, C.", "Perez, P." },
		title = "Aggregating local descriptors into a compact image representation",
		year = "2010",
		booktitle = "Computer Vision and Pattern Recognition (CVPR), 2010 IEEE Conference on",
		pages = { "3304 ", "3311" },
		month = "june",
		customData = {
				"doi", "10.1109/CVPR.2010.5540039",
				"ISSN", "1063-6919"
		})
public class VLAD<T> implements VectorAggregator<ArrayFeatureVector<T>, MultidimensionalFloatFV> {
	private HardAssigner<T, ?, ?> assigner;
	private T[] centroids;
	private boolean normalise;

	/**
	 * Construct with the given assigner and the centroids associated with the
	 * assigner.
	 * 
	 * @param assigner
	 *            the assigner
	 * @param centroids
	 *            the centroids associated with the assigner
	 * @param normalise
	 *            if true then output feature is l2 normalised
	 */
	public VLAD(HardAssigner<T, ?, ?> assigner, T[] centroids, boolean normalise) {
		this.assigner = assigner;
		this.centroids = centroids;
		this.normalise = normalise;
	}

	/**
	 * Construct with the given assigner and the centroids associated with the
	 * assigner.
	 * 
	 * @param assigner
	 *            the assigner
	 * @param centroids
	 *            the centroids associated with the assigner
	 * @param normalise
	 *            if true then output feature is l2 normalised
	 */
	public VLAD(HardAssigner<T, ?, ?> assigner, CentroidsProvider<T> centroids, boolean normalise) {
		this(assigner, centroids.getCentroids(), normalise);
	}

	@Override
	public MultidimensionalFloatFV aggregate(List<? extends LocalFeature<?, ? extends ArrayFeatureVector<T>>> features) {
		if (features == null || features.size() <= 0)
			return null;

		final int K = this.centroids.length;
		final int D = features.get(0).getFeatureVector().length();

		final float[][] vector = new float[K][D];

		for (final LocalFeature<?, ? extends ArrayFeatureVector<T>> f : features) {
			final T x = f.getFeatureVector().values;
			final int i = assigner.assign(x);

			for (int j = 0; j < D; j++) {
				vector[i][j] += (float) (Array.getDouble(x, j) - Array.getDouble(centroids[i], j));
			}
		}

		return prepareOutput(vector);
	}

	@Override
	public MultidimensionalFloatFV aggregateVectors(List<? extends ArrayFeatureVector<T>> features) {
		if (features == null || features.size() <= 0)
			return null;

		final int K = this.centroids.length;
		final int D = features.get(0).length();

		final float[][] vector = new float[K][D];

		for (final ArrayFeatureVector<T> f : features) {
			final T x = f.values;
			final int i = assigner.assign(x);

			for (int j = 0; j < D; j++) {
				vector[i][j] += (float) (Array.getDouble(x, j) - Array.getDouble(centroids[i], j));
			}
		}

		return prepareOutput(vector);
	}

	private MultidimensionalFloatFV prepareOutput(final float[][] vector) {
		final MultidimensionalFloatFV out = new MultidimensionalFloatFV(vector);

		if (normalise) {
			// l2 norm
			double sumsq = 0;
			for (int i = 0; i < out.values.length; i++) {
				sumsq += (out.values[i] * out.values[i]);
			}
			final float norm = (float) (1.0 / Math.sqrt(sumsq));
			for (int i = 0; i < out.values.length; i++) {
				out.values[i] *= norm;
			}
		}

		return out;
	}

	/**
	 * Generate a visualisation of the feature. This is primarily designed for
	 * descriptors that originated from the aggregation of SIFT.
	 * 
	 * @param descr
	 *            the feature
	 * @param nterms
	 *            the number of centroids used to create the feature
	 * @param nSpatialBins
	 *            the number of spatial bins used in the feature
	 * @param nOriBins
	 *            the number of orientation bins
	 * @return an image depicting the feature
	 */
	public static FImage drawDescriptor(float[] descr, int nterms, int nSpatialBins, int nOriBins) {
		final FImage image = new FImage(40 * nterms, 40);

		for (int z = 0; z < nterms; z++) {
			for (int y = 0; y < nSpatialBins; y++) {
				for (int x = 0; x < nSpatialBins; x++) {
					for (int i = 0; i < nOriBins; i++) {
						final int xx = 5 + (10 * x);
						final int yy = 5 + (10 * y);
						final int length = (int) (descr[(4 * 4 * 8 * z) + (4 * 4 * y) + (4 * x) + i] * 100);
						final double theta = i * 2 * Math.PI / 8;
						image.drawLine(xx + 40 * z, yy, theta, length, 1F);
					}
				}
			}
		}

		return image;
	}
}
