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

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixEntry;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorEntry;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Utility methods for dealing with the cognitive foundry {@link Matrix}.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class CFMatrixUtils {

	/**
	 * Create a new matrix with the absolute values of the input matrix.
	 * 
	 * @param mat
	 *            input matrix
	 * @return absolute matrix
	 */
	public static Matrix abs(Matrix mat) {
		final Matrix ret = mat.clone();
		final int nrows = ret.getNumRows();
		final int ncols = ret.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				ret.setElement(r, c, Math.abs(mat.getElement(r, c)));
			}
		}
		return ret;
	}

	/**
	 * Compute the absolute sum of values in the matrix
	 * 
	 * @param mat
	 *            the matrix
	 * @return the absolute sum
	 */
	public static double absSum(Matrix mat) {
		double tot = 0;
		final int nrows = mat.getNumRows();
		final int ncols = mat.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				tot += Math.abs(mat.getElement(r, c));
			}
		}
		return tot;
	}

	/**
	 * Multiply a matrix by a constant, storing the results in the input matrix.
	 * 
	 * @param mat
	 *            the matrix
	 * @param etat
	 *            the constant
	 * @return the input matrix
	 */
	public static Matrix timesInplace(Matrix mat, double etat) {
		final int nrows = mat.getNumRows();
		final int ncols = mat.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				mat.setElement(r, c, mat.getElement(r, c) * etat);
			}
		}
		return mat;
	}

	/**
	 * Convert a matlab {@link MLArray} to a {@link Matrix}
	 * 
	 * @param mlArray
	 *            the matrlab matrix
	 * @return the matrix
	 */
	public static Matrix asMat(MLArray mlArray) {
		final MLDouble mlArrayDbl = (MLDouble) mlArray;
		final int rows = mlArray.getM();
		final int cols = mlArray.getN();

		final Matrix mat = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(rows, cols);

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				mat.setElement(r, c, mlArrayDbl.get(r, c));
			}
		}
		return mat;
	}

	/**
	 * Compute the proportion of completely zero rows to rows with non-zero
	 * values
	 * 
	 * @param mat
	 *            the matrix
	 * @return the row sparsity
	 */
	public static double rowSparsity(Matrix mat) {
		final double nrows = mat.getNumRows();
		double nsparse = 0;
		for (int r = 0; r < nrows; r++) {
			if (mat.getRow(r).sum() == 0) {
				nsparse++;
			}
		}
		return nsparse / nrows;
	}

	/**
	 * Compute the proportion of completely zero columns to columns with
	 * non-zero values
	 * 
	 * @param mat
	 *            the matrix
	 * @return the column sparsity
	 */
	public static double colSparcity(Matrix mat) {
		final double ncols = mat.getNumColumns();
		double nsparse = 0;
		for (int c = 0; c < ncols; c++) {
			if (mat.getColumn(c).sum() == 0) {
				nsparse++;
			}
		}
		return nsparse / ncols;
	}

	/**
	 * Add a constant to the matrix, returning the input matrix
	 * 
	 * @param mat
	 *            the matrix
	 * @param etat
	 *            the constant
	 * @return the matrix
	 */
	public static Matrix plusInplace(Matrix mat, double etat) {
		final int nrows = mat.getNumRows();
		final int ncols = mat.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				mat.setElement(r, c, mat.getElement(r, c) + etat);
			}
		}
		return mat;
	}

	/**
	 * Extract the diagonal elements as a vector
	 * 
	 * @param mat
	 *            the matrix to extract from
	 * @return the diagonal
	 */
	public static Vector diag(Matrix mat) {
		Vector ret;

		if (mat.getNumColumns() > mat.getNumRows()) {
			ret = mat.getRow(0);
		}
		else {
			ret = mat.getColumn(0);
		}
		final int rowcol = ret.getDimensionality();
		for (int rc = 0; rc < rowcol; rc++) {
			ret.setElement(rc, mat.getElement(rc, rc));
		}
		return ret;
	}

	/**
	 * Stack matrices vertically
	 * 
	 * @param matrixFactory
	 *            factory to create output matrix
	 * @param matricies
	 *            matrices to stack
	 * @return matrix created from the stacking
	 */
	public static Matrix vstack(MatrixFactory<? extends Matrix> matrixFactory, Matrix... matricies) {
		int nrows = 0;
		int ncols = 0;
		for (final Matrix matrix : matricies) {
			nrows += matrix.getNumRows();
			ncols = matrix.getNumColumns();
		}
		final Matrix ret = matrixFactory.createMatrix(nrows, ncols);
		int currentRow = 0;
		for (final Matrix matrix : matricies) {
			ret.setSubMatrix(currentRow, 0, matrix);
			currentRow += matrix.getNumRows();
		}
		return ret;
	}

	/**
	 * Stack matrices vertically
	 * 
	 * @param matricies
	 *            matrices to stack
	 * @return matrix created from the stacking
	 */
	public static Matrix vstack(Matrix... matricies) {
		return vstack(MatrixFactory.getDefault(), matricies);
	}

	/**
	 * Get the data array backing this matrix (in column-major format)
	 * 
	 * @param w
	 *            the matrix
	 * @return the data
	 */
	public static double[] getData(Matrix w) {
		return ((no.uib.cipr.matrix.DenseMatrix) DenseMatrixFactoryMTJ.INSTANCE.copyMatrix(w).getInternalMatrix())
				.getData();
	}

	/**
	 * Get the minimum element value
	 * 
	 * @param u
	 *            the matrix
	 * @return the min value
	 */
	public static double min(Matrix u) {
		double min = Double.MAX_VALUE;
		for (final MatrixEntry matrixEntry : u) {
			min = Math.min(min, matrixEntry.getValue());
		}
		return min;
	}

	/**
	 * Get the maximum element value
	 * 
	 * @param u
	 *            the matrix
	 * @return the max value
	 */
	public static double max(Matrix u) {
		double max = -Double.MAX_VALUE;
		for (final MatrixEntry matrixEntry : u) {
			max = Math.max(max, matrixEntry.getValue());
		}
		return max;
	}

	/**
	 * Get the minimum element value
	 * 
	 * @param column
	 *            the vector
	 * @return the min value
	 */
	public static double min(Vector column) {
		double min = Double.MAX_VALUE;
		for (final VectorEntry vectorEntry : column) {
			min = Math.min(min, vectorEntry.getValue());
		}
		return min;
	}

	/**
	 * Get the maximum element value
	 * 
	 * @param column
	 *            the vector
	 * @return the max value
	 */
	public static double max(Vector column) {
		double max = -Double.MAX_VALUE;
		for (final VectorEntry vectorEntry : column) {
			max = Math.max(max, vectorEntry.getValue());
		}
		return max;
	}

	/**
	 * Compute the sparsity
	 * 
	 * @param mat
	 *            the matrix
	 * @return the sparsity
	 */
	public static double sparsity(SparseMatrix mat) {
		final double size = mat.getNumRows() * mat.getNumColumns();

		double count = 0;
		for (final MatrixEntry matrixEntry : mat) {
			if (matrixEntry.getValue() != 0)
				count++;
		}

		return (size - count) / size;
	}

	/**
	 * Bring each element to the power d
	 * 
	 * @param degree
	 * @param d
	 * @return the input
	 */
	public static <T extends Matrix> T powInplace(T degree, double d) {
		for (final MatrixEntry ent : degree) {
			degree.setElement(ent.getRowIndex(), ent.getColumnIndex(), Math.pow(ent.getValue(), d));
		}
		return degree;
	}

	/**
	 * @param laplacian
	 * @return copy and convert
	 */
	public static Jama.Matrix asJama(Matrix laplacian) {

		final Jama.Matrix ret = new Jama.Matrix(laplacian.getNumRows(), laplacian.getNumColumns());
		for (final MatrixEntry matrixEntry : laplacian) {
			ret.set(matrixEntry.getRowIndex(), matrixEntry.getColumnIndex(), matrixEntry.getValue());
		}

		return ret;
	}

}
