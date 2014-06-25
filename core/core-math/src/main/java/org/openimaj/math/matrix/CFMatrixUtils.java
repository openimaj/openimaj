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

import gov.sandia.cognition.math.matrix.DimensionalityMismatchException;
import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixEntry;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorEntry;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;
import gov.sandia.cognition.math.matrix.mtj.SparseColumnMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;
import gov.sandia.cognition.math.matrix.mtj.SparseRowMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseVector;

import java.util.Random;

import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Utility methods for dealing with the cognitive foundry {@link Matrix}.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class CFMatrixUtils {
	private static final double EPS = 1e-100;

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
		if (mat instanceof SparseColumnMatrix) {
			return absSumSparse((SparseColumnMatrix) mat);
		} else if (mat instanceof SparseRowMatrix) {
			return absSumSparse((SparseRowMatrix) mat);
		}

		double tot = 0;
		final int nrows = mat.getNumRows();
		final int ncols = mat.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				final double element = mat.getElement(r, c);
				if (Double.isNaN(element)) {
					throw new RuntimeException("hmm?");
				}
				tot += Math.abs(element);
			}
		}
		return tot;
	}

	/**
	 * @param mat
	 * @return sum of a {@link SparseColumnMatrix}
	 */
	public static double absSumSparse(SparseColumnMatrix mat) {
		double sum = 0;
		for (int i = 0; i < mat.getNumColumns(); i++) {
			sum += mat.getColumn(i).sum();
		}
		return sum;
	}

	/**
	 * @param mat
	 * @return sum of a {@link SparseRowMatrix}
	 */
	public static double absSumSparse(SparseRowMatrix mat) {
		double sum = 0;
		for (int i = 0; i < mat.getNumRows(); i++) {
			sum += mat.getRow(i).sum();
		}
		return sum;
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
		mat.scaleEquals(etat);
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
	 * Convert a matlab {@link MLArray} to a {@link Matrix}
	 * 
	 * @param name
	 * @param mat
	 * 
	 * @return the matrix
	 */
	public static MLArray toMLArray(String name, Matrix mat) {
		final MLDouble mlArrayDbl = new MLDouble(name, new int[] { mat.getNumRows(), mat.getNumColumns() });

		for (final MatrixEntry matrixEntry : mat) {
			mlArrayDbl.set(matrixEntry.getValue(), matrixEntry.getRowIndex(), matrixEntry.getColumnIndex());
		}
		return mlArrayDbl;
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
	public static double sparsity(Matrix mat) {
		double count = 0;
		final double size = mat.getNumRows() * mat.getNumColumns();
		if (mat instanceof SparseRowMatrix) {
			for (int i = 0; i < mat.getNumRows(); i++) {
				final SparseVector row = (SparseVector) mat.getRow(i);
				count += row.getNumElementsUsed();
			}
		} else if (mat instanceof SparseColumnMatrix) {
			for (int i = 0; i < mat.getNumColumns(); i++) {
				final SparseVector col = (SparseVector) mat.getColumn(i);
				count += col.getNumElementsUsed();
			}
		} else {
			for (final MatrixEntry matrixEntry : mat) {
				if (matrixEntry.getValue() != 0)
					count++;
			}
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
	 * Bring each element to the power d
	 * 
	 * @param degree
	 * @param d
	 * @return the input
	 */
	public static <T extends Vector> T powInplace(T degree, double d) {
		for (final VectorEntry ent : degree) {
			degree.setElement(ent.getIndex(), Math.pow(ent.getValue(), d));
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

	/**
	 * @param vt
	 * @return mean of each row
	 */
	public static Vector rowMean(Matrix vt) {
		final Vector sumOfColumns = vt.sumOfColumns();
		sumOfColumns.scaleEquals(1. / vt.getNumColumns());
		return sumOfColumns;
	}

	/**
	 * @param vt
	 * @return mean of each row
	 */
	public static Vector colMean(Matrix vt) {
		final Vector sumOfColumns = vt.sumOfRows();
		sumOfColumns.scaleEquals(1. / vt.getNumRows());
		return sumOfColumns;
	}

	/**
	 * @param A
	 * @param col
	 */
	public static void minusEqualsCol(Matrix A, Vector col) {
		for (int i = 0; i < A.getNumRows(); i++) {
			for (int j = 0; j < A.getNumColumns(); j++) {
				A.setElement(i, j, A.getElement(i, j) - col.getElement(i));
			}
		}
	}

	/**
	 * @param A
	 * @param row
	 */
	public static void minusEqualsRow(Matrix A, Vector row) {
		for (int i = 0; i < A.getNumRows(); i++) {
			for (int j = 0; j < A.getNumColumns(); j++) {
				A.setElement(i, j, A.getElement(i, j) - row.getElement(j));
			}
		}
	}

	/**
	 * Create a random {@link SparseColumnMatrix}
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the maximum value
	 * @param density
	 *            the matrix density
	 * @param random
	 *            the RNG
	 * @return the random matrix
	 */
	public static SparseColumnMatrix randomSparseCol(int rows, int cols, double min, double max, double density,
			Random random)
	{
		final SparseColumnMatrix ret = SparseMatrixFactoryMTJ.INSTANCE.createWrapper(new FlexCompColMatrix(rows, cols));
		for (int i = 0; i < cols; i++) {
			final Vector v = ret.getColumn(i);
			for (int j = 0; j < rows; j++) {
				if (random.nextDouble() <= density) {
					v.setElement(j, random.nextDouble() * (max - min) + min);
				}
			}
		}
		return ret;
	}

	/**
	 * Create a random {@link SparseRowMatrix}
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the maximum value
	 * @param density
	 *            the matrix density
	 * @param random
	 *            the RNG
	 * @return the random matrix
	 */
	public static SparseRowMatrix randomSparseRow(int rows, int cols, double min, double max, double density,
			Random random)
	{
		final SparseRowMatrix ret = SparseMatrixFactoryMTJ.INSTANCE.createWrapper(new FlexCompRowMatrix(rows, cols));
		for (int i = 0; i < rows; i++) {
			final Vector v = ret.getRow(i);
			for (int j = 0; j < cols; j++) {
				if (random.nextDouble() <= density) {
					v.setElement(j, random.nextDouble() * (max - min) + min);
				}
			}
		}
		return ret;
	}

	/**
	 * @param a
	 * @param b
	 * @return performs a dot product taking advantage of the sparcity in b and
	 *         a
	 */
	public static SparseRowMatrix fastsparsedot(SparseRowMatrix a, SparseColumnMatrix b) {
		final SparseRowMatrix ret = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(a.getNumRows(), b.getNumColumns());
		if (a.getNumColumns() != b.getNumRows()) {
			return null;
		}
		for (int i = 0; i < a.getNumRows(); i++) {
			final no.uib.cipr.matrix.sparse.SparseVector arow = a.getInternalMatrix().getRow(i);
			for (int j = 0; j < b.getNumColumns(); j++) {
				double v = 0;
				final no.uib.cipr.matrix.sparse.SparseVector bcol = b.getInternalMatrix().getColumn(j);
				final no.uib.cipr.matrix.sparse.SparseVector leading;
				final no.uib.cipr.matrix.sparse.SparseVector other;
				if (arow.getUsed() < bcol.getUsed()) {
					leading = arow;
					other = bcol;
				} else {
					leading = bcol;
					other = arow;
				}
				for (final no.uib.cipr.matrix.VectorEntry vectorEntry : leading) {
					v += vectorEntry.get() * other.get(vectorEntry.index());
				}
				if (Math.abs(v) > EPS) {
					ret.setElement(i, j, v);
				}
			}
		}

		return ret;
	}

	/**
	 * @param a
	 * @param b
	 * @return checks for the fastest way to do this dot product
	 */
	public static Matrix fastdot(Matrix a, Matrix b) {
		if (a instanceof SparseRowMatrix && b instanceof SparseColumnMatrix) {
			return fastsparsedot((SparseRowMatrix) a, (SparseColumnMatrix) b);
		}
		return a.times(b);
	}

	/**
	 * @param a
	 * @return turns the provided matrix into a {@link SparseColumnMatrix}
	 */
	public static SparseColumnMatrix asSparseColumn(Matrix a) {
		return SparseMatrixFactoryMTJ.INSTANCE.createWrapper(
				new FlexCompColMatrix(
						SparseMatrixFactoryMTJ.INSTANCE.copyMatrix(a).getInternalMatrix()
				)
				);
	}

	/**
	 * Convert a {@link Matrix} to a {@link SparseRowMatrix}
	 * 
	 * @param a
	 * @return the {@link SparseRowMatrix}
	 */
	public static SparseRowMatrix asSparseRow(Matrix a) {
		return SparseMatrixFactoryMTJ.INSTANCE.copyMatrix(a);
	}

	/**
	 * 
	 * @param ret
	 * @param c
	 * @param col
	 */
	public static void fastsetcol(Matrix ret, int c, Vector col) {
		if (ret instanceof SparseColumnMatrix && col instanceof SparseVector) {
			((SparseColumnMatrix) ret).setColumn(c, (SparseVector) col);
		}
		else {
			ret.setColumn(c, col);
		}
	}

	/**
	 * @param A
	 * @param B
	 * @return A - B, done using
	 *         {@link #fastsparseminusEquals(SparseColumnMatrix, SparseColumnMatrix)}
	 *         if possible
	 */
	public static Matrix fastminusEquals(Matrix A, Matrix B) {
		if (A instanceof SparseColumnMatrix && B instanceof SparseColumnMatrix) {
			return fastsparseminusEquals((SparseColumnMatrix) A, (SparseColumnMatrix) B);
		}
		A.minusEquals(B);
		return A;
	}

	/**
	 * @param A
	 * @param B
	 * @return A - B, done using {@link #fastminusEquals(Matrix, Matrix)} if
	 *         possible
	 */
	public static Matrix fastminus(Matrix A, Matrix B) {
		return fastminusEquals(A.clone(), B);
	}

	/**
	 * @param A
	 * @param B
	 * @return A - B
	 */
	public static Matrix fastsparseminusEquals(SparseColumnMatrix A, SparseColumnMatrix B) {
		if (A.getNumColumns() != B.getNumColumns() || A.getNumRows() != B.getNumRows()) {
			throw new DimensionalityMismatchException();
		}
		final SparseColumnMatrix ret = A;

		final FlexCompColMatrix retint = ret.getInternalMatrix();
		final FlexCompColMatrix Bint = B.getInternalMatrix();
		for (int i = 0; i < A.getNumColumns(); i++) {
			final no.uib.cipr.matrix.sparse.SparseVector aCol = retint.getColumn(i);
			final no.uib.cipr.matrix.sparse.SparseVector bCol = Bint.getColumn(i);
			aCol.add(-1, bCol);
		}
		return ret;
	}

	/**
	 * @param A
	 * @param B
	 * @return A - B
	 */
	public static Matrix fastsparseminusEquals(SparseRowMatrix A, SparseRowMatrix B) {
		if (A.getNumColumns() != B.getNumColumns() || A.getNumRows() != B.getNumRows()) {
			throw new DimensionalityMismatchException();
		}
		final SparseRowMatrix ret = A;

		final FlexCompRowMatrix retInt = ret.getInternalMatrix();
		final FlexCompRowMatrix bint = B.getInternalMatrix();
		for (int i = 0; i < A.getNumRows(); i++) {
			final no.uib.cipr.matrix.sparse.SparseVector aCol = retInt.getRow(i);
			final no.uib.cipr.matrix.sparse.SparseVector bCol = bint.getRow(i);
			aCol.add(-1, bCol);
		}
		return ret;
	}

	/**
	 * @param mat
	 * @return checks elements using {@link Double#isNaN()}
	 */
	public static boolean containsNaN(Matrix mat) {
		for (final MatrixEntry matrixEntry : mat) {
			if (Double.isNaN(matrixEntry.getValue())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param vec
	 * @return checks elements using {@link Double#isNaN()}
	 */
	public static boolean containsNaN(Vector vec) {
		for (final VectorEntry vectorEntry : vec) {
			if (Double.isNaN(vectorEntry.getValue())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param mat
	 * @return checks elements using {@link Double#isInfinite()}
	 */
	public static boolean containsInfinity(Matrix mat) {
		for (final MatrixEntry matrixEntry : mat) {
			if (Double.isInfinite(matrixEntry.getValue())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sum the matrix entries
	 * 
	 * @param A
	 * @return the sum
	 */
	public static double sum(Matrix A) {
		double sum = 0;
		for (final MatrixEntry matrixEntry : A) {
			sum += matrixEntry.getValue();
		}
		return sum;
	}

}
