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
package org.openimaj.math.matrix.algorithm.whitening;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.matrix.algorithm.pca.SvdPrincipalComponentAnalysis;
import org.openimaj.math.statistics.normalisation.Normaliser;
import org.openimaj.math.statistics.normalisation.TrainableNormaliser;

import Jama.Matrix;

/**
 * Whitening based on PCA. The PCA basis is used to rotate the data, and then
 * the data is normalised by the variances such that it has unit variance along
 * all principal axes.
 * <p>
 * Optionally, you can also reduce the dimensionality of the data during the
 * whitening process (by throwing away components with small eignevalues).
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@Reference(
		type = ReferenceType.Book,
		author = { "Hyvrinen, Aapo", "Hurri, Jarmo", "Hoyer, Patrick O." },
		title = "Natural Image Statistics: A Probabilistic Approach to Early Computational Vision.",
		year = "2009",
		edition = "1st",
		publisher = "Springer Publishing Company, Incorporated",
		customData = {
				"isbn", "1848824904, 9781848824904"
		})
public class PCAWhitening extends WhiteningTransform {
	protected double eps;
	protected Normaliser ns;
	protected Matrix transform;
	protected int ndims = -1;

	/**
	 * Construct with the given variance regularization parameter and data
	 * normalisation strategy.
	 *
	 * @param eps
	 *            the variance normalisation regularizer (each principle
	 *            dimension is divided by sqrt(lamba + eps), where lamba is the
	 *            corresponding eigenvalue).
	 * @param ns
	 *            the normalisation to apply to each input data vector prior to
	 *            training the transform or applying the actual whitening.
	 */
	public PCAWhitening(double eps, Normaliser ns) {
		this.eps = eps;
		this.ns = ns;
	}

	/**
	 * Construct with the given variance regularization parameter, data
	 * normalisation strategy and target dimensionality.
	 *
	 * @param eps
	 *            the variance normalisation regularizer (each principle
	 *            dimension is divided by sqrt(lamba + eps), where lamba is the
	 *            corresponding eigenvalue).
	 * @param ns
	 *            the normalisation to apply to each input data vector prior to
	 *            training the transform or applying the actual whitening.
	 * @param ndims
	 *            the number of output dimensions for the whitened data
	 */
	public PCAWhitening(double eps, Normaliser ns, int ndims) {
		this.eps = eps;
		this.ns = ns;
		this.ndims = ndims;
	}

	@Override
	public double[] whiten(double[] vector) {
		final double[] normVec = ns.normalise(vector);

		final Matrix vec = new Matrix(new double[][] { normVec });
		return vec.times(transform).getColumnPackedCopy();
	}

	@Override
	public void train(double[][] data) {
		if (ns instanceof TrainableNormaliser)
			((TrainableNormaliser) ns).train(data);

		final double[][] normData = ns.normalise(data);

		final SvdPrincipalComponentAnalysis pca = new SvdPrincipalComponentAnalysis(ndims);
		pca.learnBasisNorm(new Matrix(normData));
		transform = pca.getBasis();
		final double[] weight = pca.getEigenValues();
		final double[][] td = transform.getArray();

		for (int c = 0; c < weight.length; c++)
			weight[c] = 1 / Math.sqrt(weight[c] + eps);

		for (int r = 0; r < td.length; r++)
			for (int c = 0; c < td[0].length; c++)
				td[r][c] = td[r][c] * weight[c];
	}

	/**
	 * Get the underlying whitening transform matrix.
	 *
	 * @return the matrix
	 */
	public Matrix getTransform() {
		return transform;
	}
}
