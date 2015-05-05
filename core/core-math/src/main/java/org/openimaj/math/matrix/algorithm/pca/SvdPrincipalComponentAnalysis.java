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

import org.jblas.DoubleMatrix;
import org.jblas.NativeBlas;
import org.jblas.exceptions.LapackConvergenceException;

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
		final DoubleMatrix dm = new DoubleMatrix(norm.getRowDimension(), norm.getColumnDimension(),
				norm.getColumnPackedCopy());

		final DoubleMatrix[] result = econSVD(dm);

		final DoubleMatrix S = result[1];
		final DoubleMatrix Vt = result[2];

		final int dims = ndims < 0 ? S.rows : ndims;

		basis = new Matrix(Vt.columns, dims);
		eigenvalues = new double[dims];

		final double normEig = 1.0 / (norm.getRowDimension() - 1);
		for (int i = 0; i < eigenvalues.length; i++) {
			eigenvalues[i] = S.data[i] * S.data[i] * normEig;
		}

		final double[][] basisData = basis.getArray();
		for (int j = 0; j < Vt.columns; j++)
			for (int i = 0; i < dims; i++)
				basisData[j][i] = Vt.data[i + Vt.rows * j];
	}

	/**
	 * Compute a singular-value decomposition of A.
	 *
	 * @return A DoubleMatrix[3] array of U, S, Vt such that A = U * diag(S) *
	 *         Vt
	 */
	private static DoubleMatrix[] econSVD(DoubleMatrix A) {
		final int m = A.rows;
		final int n = A.columns;

		final DoubleMatrix U = new DoubleMatrix(m, Math.min(m, n));
		final DoubleMatrix S = new DoubleMatrix(Math.min(m, n));
		final DoubleMatrix V = new DoubleMatrix(Math.min(n, m), n);

		final int info = NativeBlas.dgesvd('S', 'S', m, n, A.dup().data, 0, m, S.data, 0, U.data, 0, m, V.data, 0, n);

		if (info > 0) {
			throw new LapackConvergenceException("GESVD", info
					+ " superdiagonals of an intermediate bidiagonal form failed to converge.");
		}

		return new DoubleMatrix[] { U, S, V };
	}
}
