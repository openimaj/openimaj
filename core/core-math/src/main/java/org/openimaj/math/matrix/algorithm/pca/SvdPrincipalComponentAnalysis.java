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
package org.openimaj.math.matrix.algorithm.pca;

import java.util.Arrays;

import no.uib.cipr.matrix.NotConvergedException;
import Jama.Matrix;

/**
 * Compute the PCA using an SVD without actually constructing the covariance
 * matrix. This class performs a full SVD extracting all singular values and
 * vectors. If you know apriori how many principle components (or have an upper
 * bound on the number), then use a {@link ThinSvdPrincipalComponentAnalysis}
 * instead.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SvdPrincipalComponentAnalysis extends PrincipalComponentAnalysis {
	int ndims;

	/**
	 * Construct a {@link SvdPrincipalComponentAnalysis} that will extract all
	 * the eigenvectors.
	 */
	public SvdPrincipalComponentAnalysis() {
		this(-1);
	}

	/**
	 * Construct a {@link SvdPrincipalComponentAnalysis} that will extract the n
	 * best eigenvectors.
	 * 
	 * @param ndims
	 *            the number of eigenvectors to select.
	 */
	public SvdPrincipalComponentAnalysis(int ndims) {
		this.ndims = ndims;
	}

	@Override
	public void learnBasisNorm(Matrix norm) {
		try {
			final no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(norm.getArray());
			final no.uib.cipr.matrix.EconomySVD svd = no.uib.cipr.matrix.EconomySVD.factorize(mjtA);

			final no.uib.cipr.matrix.DenseMatrix output = svd.getVt();

			final int dims = ndims < 0 ? svd.getS().length : ndims;

			basis = new Matrix(output.numColumns(), dims);
			eigenvalues = Arrays.copyOf(svd.getS(), dims);

			final double normEig = 1.0 / (norm.getRowDimension() - 1);
			for (int i = 0; i < eigenvalues.length; i++)
				eigenvalues[i] = eigenvalues[i] * eigenvalues[i] * normEig;

			final double[][] basisData = basis.getArray();
			for (int j = 0; j < output.numColumns(); j++)
				for (int i = 0; i < dims; i++)
					basisData[j][i] = output.get(i, j);

		} catch (final NotConvergedException e) {
			throw new RuntimeException(e);
		}
	}
}
