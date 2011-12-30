package org.openimaj.math.matrix;

import no.uib.cipr.matrix.NotConvergedException;
import Jama.Matrix;

/**
 * Methods for calculating the Moore-Penrose Pseudo-Inverse
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class PseudoInverse {
	public static Matrix pseudoInverse(Matrix matrix) {
		no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(matrix.getArray());
		no.uib.cipr.matrix.SVD svd;
		
		try {
			svd = no.uib.cipr.matrix.SVD.factorize(mjtA);
		} catch (NotConvergedException e) {
			throw new RuntimeException(e);
		}

		Matrix Sinv = new Matrix(matrix.getColumnDimension(), matrix.getRowDimension());
		
		double[] Sarr = svd.getS();
		for (int i=0; i<svd.getS().length; i++) {
			Sinv.set(i, i, 1.0 / Sarr[i]);  
		}
		
		Matrix Vt = new Matrix(svd.getVt().numRows(), svd.getVt().numColumns());
		for (int r=0; r<svd.getVt().numRows(); r++) {
			for (int c=0; c<svd.getVt().numColumns(); c++) {
				Vt.set(r,c, svd.getVt().get(r, c));
			}
		}
		
		Matrix U = new Matrix(svd.getU().numRows(), svd.getU().numColumns());
		for (int r=0; r<svd.getU().numRows(); r++) {
			for (int c=0; c<svd.getU().numColumns(); c++) {
				U.set(r,c, svd.getU().get(r, c));
			}
		}
		
		Matrix pinv = Vt.transpose().times(Sinv).times(U.transpose());
		
		return pinv;
	}
	
	public static Matrix pseudoInverse(Matrix matrix, int rank) {
		return pseudoInverse(new JamaDenseMatrix(matrix), rank);
	}
	
	public static Matrix pseudoInverse(ch.akuhn.matrix.Matrix matrix, int rank) {
		ThinSingularValueDecomposition tsvd = new ThinSingularValueDecomposition(matrix, rank);
		
		Matrix Sinv = new Matrix(tsvd.S.length, tsvd.S.length);
		for (int i=0; i<tsvd.S.length; i++) {
			Sinv.set(i, i, 1.0 / tsvd.S[i]);  
		}
		
		Matrix pinv = tsvd.Vt.transpose().times(Sinv).times(tsvd.U.transpose());
		
		return pinv;
	}
}
