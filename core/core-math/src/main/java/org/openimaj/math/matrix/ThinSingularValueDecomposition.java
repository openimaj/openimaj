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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
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
