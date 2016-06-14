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
package no.uib.cipr.matrix;

import org.netlib.util.intW;

import com.github.fommil.netlib.LAPACK;

/**
 * Computes economy singular value decompositions. Uses DGESDD internally.
 */
public class EconomySVD {

	/**
	 * Work array
	 */
	private final double[] work;

	/**
	 * Work array
	 */
	private final int[] iwork;

	/**
	 * Matrix dimension
	 */
	private final int m, n;

	/**
	 * The singular values
	 */
	private final double[] S;

	/**
	 * Singular vectors
	 */
	private final DenseMatrix U, Vt;

	/**
	 * Creates an empty SVD
	 *
	 * @param m
	 *            Number of rows
	 * @param n
	 *            Number of columns
	 */
	public EconomySVD(int m, int n) {
		this.m = m;
		this.n = n;

		// Allocate space for the decomposition
		S = new double[Math.min(m, n)];
		U = new DenseMatrix(Matrices.ld(m), Math.min(m, n));
		Vt = new DenseMatrix(Matrices.ld(Math.min(m, n)), n);

		// Find workspace requirements
		iwork = new int[8 * Math.min(m, n)];

		// Query optimal workspace
		final double[] worksize = new double[1];
		final intW info = new intW(0);
		LAPACK.getInstance().dgesdd(JobSVD.Part.netlib(), m, n, new double[0],
				Matrices.ld(m), new double[0], new double[0], U.numRows,
				new double[0], Vt.numRows, worksize, -1, iwork, info);

		// Allocate workspace
		int lwork = -1;
		if (info.val != 0) {
			// 'S' => LWORK >= min(M,N)*(6+4*min(M,N))+max(M,N)
			lwork = Math.min(m, n) * (6 + 4 * Math.min(m, n)) + Math.max(m, n);
		} else
			lwork = (int) worksize[0];

		lwork = Math.max(lwork, 1);
		work = new double[lwork];
	}

	/**
	 * Convenience method for computing a full SVD
	 *
	 * @param A
	 *            Matrix to decompose, not modified
	 * @return Newly allocated factorization
	 * @throws NotConvergedException
	 */
	public static EconomySVD factorize(Matrix A) throws NotConvergedException {
		return new EconomySVD(A.numRows(), A.numColumns()).factor(new DenseMatrix(A));
	}

	/**
	 * Computes an SVD
	 *
	 * @param A
	 *            Matrix to decompose. Size must conform, and it will be
	 *            overwritten on return. Pass a copy to avoid this
	 * @return The current decomposition
	 * @throws NotConvergedException
	 */
	public EconomySVD factor(DenseMatrix A) throws NotConvergedException {
		if (A.numRows() != m)
			throw new IllegalArgumentException("A.numRows() != m");
		else if (A.numColumns() != n)
			throw new IllegalArgumentException("A.numColumns() != n");

		final intW info = new intW(0);

		LAPACK.getInstance().dgesdd(JobSVD.Part.netlib(), m, n,
				A.getData(), A.numRows,
				S,
				U.getData(), U.numRows,
				Vt.getData(), Vt.numRows,
				work, work.length, iwork, info);

		if (info.val > 0)
			throw new NotConvergedException(
					NotConvergedException.Reason.Iterations);
		else if (info.val < 0)
			throw new IllegalArgumentException();

		return this;
	}

	/**
	 * Returns the left singular vectors, column-wise. Not available for partial
	 * decompositions
	 *
	 * @return Matrix of size m*m
	 */
	public DenseMatrix getU() {
		return U;
	}

	/**
	 * Returns the right singular vectors, row-wise. Not available for partial
	 * decompositions
	 *
	 * @return Matrix of size n*n
	 */
	public DenseMatrix getVt() {
		return Vt;
	}

	/**
	 * Returns the singular values (stored in descending order)
	 *
	 * @return Array of size min(m,n)
	 */
	public double[] getS() {
		return S;
	}

}
