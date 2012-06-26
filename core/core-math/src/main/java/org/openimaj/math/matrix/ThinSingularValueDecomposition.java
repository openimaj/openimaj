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
package org.openimaj.math.matrix;

import Jama.Matrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.SingularValues;

/**
 * Thin SVD based on Adrian Kuhn's wrapper around ARPACK. 
 * This can scale to really large matrices (bigger than RAM), given
 * an implementation of {@link ch.akuhn.matrix.Matrix} that
 * is backed by disk. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ThinSingularValueDecomposition {
	/** The U matrix */
	public Matrix U;
	/** The singular values */
	public double [] S;
	/** The transpose of the V matrix */
	public Matrix Vt;

	/**
	 * Perform thin SVD on matrix, calculating at most
	 * ndims dimensions.
	 * 
	 * @param matrix the matrix
	 * @param ndims the number of singular values/vectors to calculate; actual number may be less.
	 */
	public ThinSingularValueDecomposition(Matrix matrix, int ndims) {
		this(new JamaDenseMatrix(matrix), ndims);
	}

	/**
	 * Perform thin SVD on matrix, calculating at most
	 * ndims dimensions.
	 * 
	 * @param matrix the matrix
	 * @param ndims the number of singular values/vectors to calculate; actual number may be less.
	 */
	public ThinSingularValueDecomposition(ch.akuhn.matrix.Matrix matrix, int ndims) {
		SingularValues sv = new SingularValues(matrix, ndims);
		sv.decompose();
		
		S = reverse(sv.value);
		U = vectorArrayToMatrix(sv.vectorLeft, false);
		Vt = vectorArrayToMatrix(sv.vectorRight, true);
	}

	protected double[] reverse(double [] vector) {
		for (int i=0; i<vector.length/2; i++) {
			double tmp = vector[i];
			vector[i] = vector[vector.length - i - 1];
			vector[vector.length - i - 1] = tmp;
		}
		return vector;
	}
	
	protected Matrix vectorArrayToMatrix(Vector[] vectors, boolean rows) {
		final int m = vectors.length;
		
		double [][] data = new double[m][];

		for (int i=0; i<m; i++)
			data[m - i - 1] = vectors[i].unwrap();

		Matrix mat = new Matrix(data);
		
		if (!rows) {
			mat = mat.transpose();
		} 
		return mat;
	}
	
	/**
	 * @return The S matrix
	 */
	public Matrix getSmatrix() {
		Matrix Smat = new Matrix(S.length, S.length);
		
		for (int r=0; r<S.length; r++)
			Smat.set(r, r, S[r]);
		
		return Smat;
	}
	
	/**
	 * @return The sqrt of the singular vals as a matrix.
	 */
	public Matrix getSmatrixSqrt() {
		Matrix Smat = new Matrix(S.length, S.length);
		
		for (int r=0; r<S.length; r++)
			Smat.set(r, r, Math.sqrt(S[r]));
		
		return Smat;
	}
	
	/**
	 * Reduce the rank of the input matrix using the thin SVD to
	 * get a lower rank least-squares estimate of the input.
	 * @param m matrix to reduce the rank of
	 * @param rank the desired rank
	 * @return the rank-reduced matrix
	 */
	public static Matrix reduceRank(Matrix m, int rank) {
		if(rank > Math.min(m.getColumnDimension(), m.getRowDimension())) {
			return m;
		}
		
		ThinSingularValueDecomposition t = new ThinSingularValueDecomposition(m,rank);
		return t.U.times(t.getSmatrix()).times(t.Vt);
	}
}
