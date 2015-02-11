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

import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;
import org.openimaj.ml.gmm.GaussianMixtureModelEM;
import org.openimaj.ml.gmm.GaussianMixtureModelEM.CovarianceType;

/**
 * Implementation of the Fisher Vector (FV) encoding scheme. FV provides a way
 * of encoding a set of vectors (e.g. local features) as a single vector that
 * encapsulates the first and second order residuals of the vectors from a
 * gaussian mixture model.
 * <p>
 * The dimensionality of the output vector is 2*K*D where K is the number of
 * Gaussians in the mixture, and D is the descriptor dimensionality. Note that
 * only the diagonal values of the gaussian covariance matrices are used, and
 * thus you probably want to learn a {@link CovarianceType#Diagonal} or
 * {@link CovarianceType#Spherical} type gaussian with the
 * {@link GaussianMixtureModelEM} class.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            Primitive array type of the {@link ArrayFeatureVector}s used by
 *            the {@link LocalFeature}s that will be processed.
 */
@References(
		references = {
				@Reference(
						type = ReferenceType.Inproceedings,
						author = { "Perronnin, F.", "Dance, C." },
						title = "Fisher Kernels on Visual Vocabularies for Image Categorization",
						year = "2007",
						booktitle = "Computer Vision and Pattern Recognition, 2007. CVPR '07. IEEE Conference on",
						pages = { "1", "8" },
						customData = {
								"keywords",
								"Gaussian processes;gradient methods;image classification;Fisher kernels;Gaussian mixture model;generative probability model;gradient vector;image categorization;pattern classification;visual vocabularies;Character generation;Feeds;Image databases;Kernel;Pattern classification;Power generation;Signal generators;Spatial databases;Visual databases;Vocabulary",
								"doi", "10.1109/CVPR.2007.383266",
								"ISSN", "1063-6919"
						}
						),
						@Reference(
								type = ReferenceType.Inproceedings,
								author = { "Perronnin, Florent", "S\'{a}nchez, Jorge", "Mensink, Thomas" },
								title = "Improving the Fisher Kernel for Large-scale Image Classification",
								year = "2010",
								booktitle = "Proceedings of the 11th European Conference on Computer Vision: Part IV",
								pages = { "143", "", "156" },
								url = "http://dl.acm.org/citation.cfm?id=1888089.1888101",
								publisher = "Springer-Verlag",
								series = "ECCV'10",
								customData = {
										"isbn", "3-642-15560-X, 978-3-642-15560-4",
										"location", "Heraklion, Crete, Greece",
										"numpages", "14",
										"acmid", "1888101",
										"address", "Berlin, Heidelberg"
								}
								)
		})
public class FisherVector<T> implements VectorAggregator<ArrayFeatureVector<T>, FloatFV> {
	private MixtureOfGaussians gmm;
	private boolean hellinger;
	private boolean l2normalise;

	/**
	 * Construct with the given mixture of Gaussians and optional improvement
	 * steps. The covariance matrices of the gaussians are all assumed to be
	 * diagonal, and will be treated as such; any non-zero off-diagonal values
	 * will be completely ignored.
	 *
	 * @param gmm
	 *            the mixture of gaussians
	 * @param hellinger
	 *            if true then use Hellinger's kernel rather than the linear one
	 *            by signed square rooting the values in the final vector
	 * @param l2normalise
	 *            if true then apply l2 normalisation to the final vector. This
	 *            occurs after the Hellinger step if it is used.
	 */
	public FisherVector(MixtureOfGaussians gmm, boolean hellinger, boolean l2normalise) {
		this.gmm = gmm;
		this.hellinger = hellinger;
		this.l2normalise = l2normalise;
	}

	/**
	 * Construct the standard Fisher Vector encoder with the given mixture of
	 * Gaussians. The covariance matrices of the gaussians are all assumed to be
	 * diagonal, and will be treated as such; any non-zero off-diagonal values
	 * will be completely ignored.
	 *
	 * @param gmm
	 *            the mixture of gaussians
	 */
	public FisherVector(MixtureOfGaussians gmm) {
		this(gmm, false);
	}

	/**
	 * Construct the Fisher Vector encoder with the given mixture of Gaussians
	 * and the optional improvement steps (in the sense of the VLFeat
	 * documentation). The covariance matrices of the gaussians are all assumed
	 * to be diagonal, and will be treated as such; any non-zero off-diagonal
	 * values will be completely ignored. For the improved version, the final
	 * vector is projected into Hellinger's kernel and then l2 normalised.
	 *
	 * @param gmm
	 *            the mixture of gaussians
	 * @param improved
	 *            if true then Hellinger's kernel is used, and the vector is l2
	 *            normalised.
	 */
	public FisherVector(MixtureOfGaussians gmm, boolean improved) {
		this(gmm, improved, improved);
	}

	@Override
	public FloatFV aggregate(List<? extends LocalFeature<?, ? extends ArrayFeatureVector<T>>> features) {
		if (features == null || features.size() <= 0)
			return null;

		final int K = this.gmm.gaussians.length;
		final int D = features.get(0).getFeatureVector().length();

		final float[] vector = new float[2 * K * D];

		// cache all the features in an array
		final double[][] X = new double[features.size()][];
		for (int i = 0; i < X.length; i++) {
			final LocalFeature<?, ? extends ArrayFeatureVector<T>> f = features.get(i);
			X[i] = f.getFeatureVector().asDoubleVector();
		}

		return computeFisherVector(features.size(), K, D, vector, X);
	}

	@Override
	public FloatFV aggregateVectors(List<? extends ArrayFeatureVector<T>> features) {
		if (features == null || features.size() <= 0)
			return null;

		final int K = this.gmm.gaussians.length;
		final int D = features.get(0).length();

		final float[] vector = new float[2 * K * D];

		// cache all the features in an array
		final double[][] X = new double[features.size()][];
		for (int i = 0; i < X.length; i++) {
			final ArrayFeatureVector<T> f = features.get(i);
			X[i] = f.asDoubleVector();
		}

		return computeFisherVector(features.size(), K, D, vector, X);
	}

	private FloatFV computeFisherVector(int nFeatures, final int K,
			final int D, final float[] vector, final double[][] X)
	{
		// compute posterior probabilities of all features at once (more
		// efficient than
		// doing it for each one at a time)
		final double[][] posteriors = gmm.scoreSamples(X).secondObject();

		for (int p = 0; p < X.length; p++) {
			final double[] xp = X[p];

			for (int k = 0; k < K; k++) {
				final double apk = posteriors[p][k];

				if (apk < 1e-6)
					continue; // speed-up: ignore really small terms...

				final MultivariateGaussian gauss = gmm.gaussians[k];
				final double[] mean = gauss.getMean().getArray()[0];

				for (int j = 0; j < D; j++) {
					final double var = gauss.getCovariance(j, j);
					final double diff = (xp[j] - mean[j]) / Math.sqrt(var);

					vector[k * 2 * D + j] += apk * diff;
					vector[k * 2 * D + j + D] += apk * ((diff * diff) - 1);
				}
			}
		}

		for (int k = 0; k < K; k++) {
			final double wt1 = 1.0 / (nFeatures * Math.sqrt(gmm.weights[k]));
			final double wt2 = 1.0 / (nFeatures * Math.sqrt(2 * gmm.weights[k]));

			for (int j = 0; j < D; j++) {
				vector[k * 2 * D + j] *= wt1;
				vector[k * 2 * D + j + D] *= wt2;
			}
		}

		final FloatFV out = new FloatFV(vector);

		if (hellinger) {
			for (int i = 0; i < out.values.length; i++) {
				out.values[i] = (float) (out.values[i] > 0 ? Math.sqrt(out.values[i]) :
					-1 * Math.sqrt(-1 * out.values[i]));
			}
		}

		if (l2normalise) {
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
}
