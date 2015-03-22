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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import ch.akuhn.matrix.SparseMatrix;

/**
 * Miscellaneous matrix operations.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MatrixUtils {
	private MatrixUtils() {
	}

	/**
	 * Are any values NaN or Inf?
	 * 
	 * @param matrix
	 *            matrix to test
	 * @return true if any elements are NaN or Inf; false otherwise
	 */
	public static boolean anyNaNorInf(Matrix matrix) {
		for (final double[] arrLine : matrix.getArray()) {
			for (final double d : arrLine) {
				if (Double.isNaN(d) || Double.isInfinite(d))
					return true;
			}
		}
		return false;
	}

	/**
	 * Get the maximum absolute value of the diagonal.
	 * 
	 * @param matrix
	 *            the matrix
	 * @return the maximum absolute value
	 */
	public static double maxAbsDiag(Matrix matrix) {
		double max = -1;

		for (int i = 0; i < matrix.getColumnDimension(); i++) {
			final double curr = Math.abs(matrix.get(i, i));
			if (max < curr) {
				max = curr;
			}
		}
		return max;
	}

	/**
	 * Get the minimum absolute value of the diagonal.
	 * 
	 * @param matrix
	 *            the matrix
	 * @return the minimum absolute value
	 */
	public static double minAbsDiag(Matrix matrix) {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < matrix.getColumnDimension(); i++) {
			final double curr = Math.abs(matrix.get(i, i));
			if (min > curr) {
				min = curr;
			}
		}
		return min;
	}

	/**
	 * Compute the principle square root, X, of the matrix A such that A=X*X
	 * 
	 * @param matrix
	 *            the matrix
	 * @return the sqrt of the matrix
	 */
	public static Matrix sqrt(Matrix matrix) {
		// A = V*D*V'
		final EigenvalueDecomposition evd = matrix.eig();
		final Matrix v = evd.getV();
		final Matrix d = evd.getD();

		// sqrt of cells of D and store in-place
		for (int r = 0; r < d.getRowDimension(); r++)
			for (int c = 0; c < d.getColumnDimension(); c++)
				d.set(r, c, Math.sqrt(d.get(r, c)));

		// Y = V*D/V
		// Y = V'.solve(V*D)'
		final Matrix a = v.inverse();
		final Matrix b = v.times(d).inverse();
		return a.solve(b).inverse();
	}

	/**
	 * Computes the Moore-Penrose pseudoinverse. This is just a convenience
	 * wrapper around {@link PseudoInverse#pseudoInverse(Matrix)}.
	 * 
	 * @param matrix
	 *            the matrix to invert.
	 * @return the inverted matrix
	 * @see PseudoInverse#pseudoInverse(Matrix)
	 */
	public static Matrix pseudoInverse(Matrix matrix) {
		return PseudoInverse.pseudoInverse(matrix);
	}

	/**
	 * Compute the inverse square root, X, of the symmetric matrix A; A^-(1/2)
	 * 
	 * @param matrix
	 *            the symmetric matrix
	 * @return the inverse sqrt of the matrix
	 */
	public static Matrix invSqrtSym(Matrix matrix) {
		// A = V*D*V'
		final EigenvalueDecomposition evd = matrix.eig();
		final Matrix v = evd.getV();
		final Matrix d = evd.getD();

		// sqrt of cells of D and store in-place
		for (int r = 0; r < d.getRowDimension(); r++) {
			for (int c = 0; c < d.getColumnDimension(); c++) {
				if (d.get(r, c) > 0)
					d.set(r, c, 1 / Math.sqrt(d.get(r, c)));
				else
					d.set(r, c, 0);
			}
		}

		return v.times(d).times(v.transpose());
	}

	/**
	 * Return a copy of the input matrix with all elements set to their absolute
	 * value.
	 * 
	 * @param mat
	 *            the matrix.
	 * @return the absolute matrix.
	 */
	public static Matrix abs(Matrix mat) {
		final Matrix copy = mat.copy();
		for (int i = 0; i < mat.getRowDimension(); i++) {
			for (int j = 0; j < mat.getColumnDimension(); j++) {
				copy.set(i, j, Math.abs(mat.get(i, j)));
			}
		}
		return copy;
	}

	/**
	 * Check if two matrices are equal
	 * 
	 * @param m1
	 *            first matrix
	 * @param m2
	 *            second matrix
	 * @param eps
	 *            epsilon for checking values
	 * @return true if matrices have same size and all elements are equal within
	 *         eps; false otherwise
	 */
	public static boolean equals(Matrix m1, Matrix m2, double eps) {
		final double[][] a1 = m1.getArray();
		final double[][] a2 = m2.getArray();

		if (a1.length != a2.length || a1[0].length != a2[0].length)
			return false;

		for (int r = 0; r < a1.length; r++)
			for (int c = 0; c < a1[r].length; c++)
				if (Math.abs(a1[r][c] - a2[r][c]) > eps)
					return false;

		return true;
	}

	/**
	 * Return a copy of the matrix with all the values raised to a power.
	 * 
	 * @param mat
	 *            the matrix.
	 * @param exp
	 *            the power.
	 * @return a matrix.
	 */
	public static Matrix pow(Matrix mat, double exp) {
		final Matrix copy = mat.copy();
		for (int i = 0; i < mat.getRowDimension(); i++) {
			for (int j = 0; j < mat.getColumnDimension(); j++) {
				copy.set(i, j, Math.pow(mat.get(i, j), exp));
			}
		}
		return copy;
	}

	/**
	 * Generate a {@link String} representation of a matrix.
	 * 
	 * @param mat
	 *            the matrix
	 * @return a string representation
	 */
	public static String toString(Matrix mat) {
		final StringWriter matWriter = new StringWriter();
		mat.print(new PrintWriter(matWriter), 5, 5);
		return matWriter.getBuffer().toString();
	}

	/**
	 * Compute the sum of all elements of the matrix.
	 * 
	 * @param mat
	 *            the matrix.
	 * @return the sum.
	 */
	public static double sum(Matrix mat) {
		double sum = 0;
		for (int i = 0; i < mat.getRowDimension(); i++) {
			for (int j = 0; j < mat.getColumnDimension(); j++) {
				sum += mat.get(i, j);
			}
		}
		return sum;
	}

	/**
	 * Zero the matrix
	 * 
	 * @param m
	 *            the matrix
	 */
	public static void zero(Matrix m) {
		m.timesEquals(0);
	}

	/**
	 * Compute the real Eigen decomposition of a symmetric 2x2 matrix. Warning:
	 * Doesn't check the size or whether the input is symmetric.
	 * 
	 * @param m
	 *            the matrix
	 * @return the Eigen vectors and values.
	 */
	public static EigenValueVectorPair symmetricEig2x2(Matrix m) {
		final double a = m.get(0, 0);
		final double b = m.get(0, 1);
		final double c = b;
		final double d = m.get(1, 1);

		final double trace = a + d;
		final double det = a * d - b * c;

		final Matrix val = new Matrix(2, 2);
		double sqrtInner = (trace * trace / 4) - det;
		// FIXME: make it deal with imaginary numbers.
		if (sqrtInner < 0) {
			final EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(), e.getV());
		}

		sqrtInner = Math.sqrt(sqrtInner);
		double firstEig = trace / 2 + sqrtInner;
		double secondEig = trace / 2 - sqrtInner;
		if (firstEig > secondEig) {
			final double tmp = firstEig;
			firstEig = secondEig;
			secondEig = tmp;
		}

		val.set(0, 0, firstEig);
		val.set(1, 1, secondEig);

		final Matrix vec = new Matrix(2, 2);

		final double v1 = firstEig - a;
		final double v2 = secondEig - a;
		final double norm1 = Math.sqrt(v1 * v1 + b * b);
		final double norm2 = Math.sqrt(b * b + v2 * v2);
		vec.set(0, 0, b / norm1);
		vec.set(0, 1, b / norm2);
		vec.set(1, 0, v1 / norm1);
		vec.set(1, 1, v2 / norm2);

		// To deal with rounding error
		vec.set(1, 0, vec.get(0, 1));

		final EigenValueVectorPair ret = new EigenValueVectorPair(val, vec);
		return ret;
	}

	/**
	 * An eigen decomposition that uses a deterministic method if the matrix is
	 * 2x2.
	 * 
	 * This function returns values as in {@link EigenvalueDecomposition} i.e.
	 * the largest eigen value is held in the [m.rows - 1,m.cols-1] (i.e. [1,1])
	 * location
	 * 
	 * @param m
	 * @return the decomposition
	 */
	public static EigenValueVectorPair eig2x2(Matrix m) {
		if (m.getColumnDimension() != 2 || m.getRowDimension() != 2) {
			final EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(), e.getV());
		}
		/**
		 * A = 1 B = a + d C = ad - bc
		 * 
		 * x = ( - B (+/-) sqrt(B^2 - 4AC) )/ (2A)
		 */
		final double a = m.get(0, 0);
		final double b = m.get(0, 1);
		final double c = m.get(1, 0);
		final double d = m.get(1, 1);

		final double trace = a + d;
		final double det = a * d - b * c;

		final Matrix val = new Matrix(2, 2);
		double sqrtInner = (trace * trace / 4) - det;
		// FIXME: make it deal with imaginary numbers.
		if (sqrtInner < 0) {
			final EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(), e.getV());
		}

		sqrtInner = Math.sqrt(sqrtInner);
		double firstEig = trace / 2 + sqrtInner;
		double secondEig = trace / 2 - sqrtInner;
		if (firstEig > secondEig) {
			final double tmp = firstEig;
			firstEig = secondEig;
			secondEig = tmp;
		}

		val.set(0, 0, firstEig);
		val.set(1, 1, secondEig);

		final Matrix vec = new Matrix(2, 2);
		if (b == 0 && c == 0) {
			vec.set(0, 0, 1);
			vec.set(1, 1, 1);
		} else {
			if (c != 0) {
				final double v1 = firstEig - d;
				final double v2 = secondEig - d;
				final double norm1 = Math.sqrt(v1 * v1 + c * c);
				final double norm2 = Math.sqrt(c * c + v2 * v2);
				vec.set(0, 0, v1 / norm1);
				vec.set(0, 1, v2 / norm2);
				vec.set(1, 0, c / norm1);
				vec.set(1, 1, c / norm2);
			} else if (b != 0) {
				final double v1 = firstEig - a;
				final double v2 = secondEig - a;
				final double norm1 = Math.sqrt(v1 * v1 + b * b);
				final double norm2 = Math.sqrt(b * b + v2 * v2);
				vec.set(0, 0, b / norm1);
				vec.set(0, 1, b / norm2);
				vec.set(1, 0, v1 / norm1);
				vec.set(1, 1, v2 / norm2);
			}
		}

		final EigenValueVectorPair ret = new EigenValueVectorPair(val, vec);
		return ret;
	}

	/**
	 * Construct a matrix from a 2D float array of data.
	 * 
	 * @param data
	 *            the data.
	 * @return the matrix.
	 */
	public static Matrix matrixFromFloat(float[][] data) {
		final Matrix out = new Matrix(data.length, data[0].length);
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				out.set(j, i, data[i][j]);
			}
		}
		return out;
	}

	/**
	 * Reduce the rank a matrix by estimating a the best (in a least-squares
	 * sense) approximation using the thin SVD.
	 * 
	 * @param m
	 *            the matrix to reduce.
	 * @param rank
	 *            the desired rank.
	 * @return the rank-reduced matrix.
	 */
	public static Matrix reduceRank(Matrix m, int rank) {
		if (rank > Math.min(m.getColumnDimension(), m.getRowDimension())) {
			return m;
		}

		final no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(
				m.getArray());
		no.uib.cipr.matrix.SVD svd;
		try {
			svd = no.uib.cipr.matrix.SVD.factorize(mjtA);
		} catch (final NotConvergedException e) {
			throw new RuntimeException(e);
		}

		final DenseMatrix U = svd.getU();
		final DenseMatrix Vt = svd.getVt();
		final double[] svector = svd.getS();
		final DenseMatrix S = new DenseMatrix(U.numColumns(), Vt.numRows());
		for (int i = 0; i < rank; i++)
			S.set(i, i, svector[i]);

		final DenseMatrix C = new DenseMatrix(U.numRows(), S.numColumns());
		final DenseMatrix out = new DenseMatrix(C.numRows(), Vt.numColumns());
		U.mult(S, C);
		C.mult(Vt, out);

		final Matrix outFinal = convert(out);
		return outFinal;
	}

	/**
	 * Convert a {@link DenseMatrix} to a {@link Matrix}.
	 * 
	 * @param mjt
	 *            {@link DenseMatrix} to convert
	 * @return converted matrix.
	 */
	public static Matrix convert(DenseMatrix mjt) {
		return convert(mjt, mjt.numRows(), mjt.numColumns());
	}

	/**
	 * Convert a {@link DenseMatrix} to a {@link Matrix}.
	 * 
	 * @param mjt
	 *            {@link DenseMatrix} to convert
	 * @param nrows
	 *            number of rows to copy
	 * @param ncols
	 *            number of columns to copy
	 * @return converted matrix.
	 */
	public static Matrix convert(DenseMatrix mjt, int nrows, int ncols) {
		final double[][] d = new double[nrows][ncols];

		final double[] mjtd = mjt.getData();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				d[r][c] = mjtd[r + c * d.length];
			}
		}
		return new Matrix(d);
	}

	/**
	 * Create a copy of a matrix with the columns in reverse order.
	 * 
	 * @param m
	 *            the input matrix
	 * @return a copy with the column order reversed
	 */
	public static Matrix reverseColumns(Matrix m) {
		return reverseColumnsInplace(m.copy());
	}

	/**
	 * Reverse the column order of the input matrix inplace.
	 * 
	 * @param m
	 *            the input matrix
	 * @return the input matrix
	 */
	public static Matrix reverseColumnsInplace(Matrix m) {
		final double[][] data = m.getArray();
		final int rows = data.length;
		final int cols = data[0].length;
		final int halfCols = cols / 2;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < halfCols; c++) {
				final double tmp = data[r][c];
				data[r][c] = data[r][cols - c - 1];
				data[r][cols - c - 1] = tmp;
			}
		}

		return m;
	}

	/**
	 * Create a copy of a matrix with the rows in reverse order.
	 * 
	 * @param m
	 *            the input matrix
	 * @return a copy with the row order reversed
	 */
	public static Matrix reverseRows(Matrix m) {
		return reverseRowsInplace(m.copy());
	}

	/**
	 * Reverse the row order of the input matrix inplace.
	 * 
	 * @param m
	 *            the input matrix
	 * @return the input matrix
	 */
	public static Matrix reverseRowsInplace(Matrix m) {
		final double[][] data = m.getArray();
		final int rows = data.length;
		final int halfRows = rows / 2;

		for (int r = 0; r < halfRows; r++) {
			final double[] tmp = data[r];
			data[r] = data[rows - r - 1];
			data[rows - r - 1] = tmp;
		}

		return m;
	}

	/**
	 * Create a diagonal matrix
	 * 
	 * @param s
	 *            length diagonal numbers
	 * @return new Matrix(s.length,s.length) s.t. diagonal element i,i = s[i]
	 */
	public static Matrix diag(double[] s) {
		final Matrix r = new Matrix(s.length, s.length);
		for (int i = 0; i < s.length; i++) {
			r.set(i, i, s[i]);
		}
		return r;
	}

	/**
	 * Set the values of the elements in a single column to a constant value.
	 * 
	 * @param m
	 *            the matrix
	 * @param c
	 *            the column
	 * @param v
	 *            the constant value
	 * @return the matrix
	 */
	public static Matrix setColumn(Matrix m, int c, double v) {
		final double[][] data = m.getArray();
		final int rows = m.getRowDimension();

		for (int r = 0; r < rows; r++)
			data[r][c] = v;

		return m;
	}

	/**
	 * Set the values of the elements in a single column to a constant value.
	 * 
	 * @param m
	 *            the matrix
	 * @param r
	 *            the row
	 * @param v
	 *            the constant value
	 * @return the matrix
	 */
	public static Matrix setRow(Matrix m, int r, double v) {
		final double[][] data = m.getArray();
		final int cols = m.getColumnDimension();

		for (int c = 0; c < cols; c++)
			data[r][c] = v;

		return m;
	}

	/**
	 * Fill a matrix with a constant value.
	 * 
	 * @param m
	 *            the matrix
	 * @param v
	 *            the constant value
	 * @return the matrix
	 */
	public static Matrix fill(Matrix m, double v) {
		final double[][] data = m.getArray();

		final int rows = m.getRowDimension();
		final int cols = m.getColumnDimension();

		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				data[r][c] = v;

		return m;
	}

	/**
	 * Subtract a constant from all values
	 * 
	 * @param m
	 *            the matrix
	 * @param v
	 *            the constant value
	 * @return the matrix
	 */
	public static Matrix minus(Matrix m, double v) {
		final double[][] data = m.getArray();

		final int rows = m.getRowDimension();
		final int cols = m.getColumnDimension();

		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				data[r][c] -= v;

		return m;
	}

	/**
	 * Add a constant to all values
	 * 
	 * @param m
	 *            the matrix
	 * @param v
	 *            the constant value
	 * @return the matrix
	 */
	public static Matrix plus(Matrix m, double v) {
		final double[][] data = m.getArray();

		final int rows = m.getRowDimension();
		final int cols = m.getColumnDimension();

		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				data[r][c] += v;

		return m;
	}

	/**
	 * Get a reshaped copy of the input matrix
	 * 
	 * @param m
	 *            the matrix to reshape
	 * @param newRows
	 *            the new number of rows
	 * @return new matrix
	 */
	public static Matrix reshape(Matrix m, int newRows) {
		final int oldCols = m.getColumnDimension();
		final int length = oldCols * m.getRowDimension();
		final int newCols = length / newRows;
		final Matrix mat = new Matrix(newRows, newCols);

		final double[][] m1v = m.getArray();
		final double[][] m2v = mat.getArray();

		int r1 = 0, r2 = 0, c1 = 0, c2 = 0;
		for (int i = 0; i < length; i++) {
			m2v[r2][c2] = m1v[r1][c1];

			c1++;
			if (c1 >= oldCols) {
				c1 = 0;
				r1++;
			}

			c2++;
			if (c2 >= newCols) {
				c2 = 0;
				r2++;
			}
		}

		return mat;
	}

	/**
	 * Get a reshaped copy of the input matrix
	 * 
	 * @param m
	 *            the matrix to reshape
	 * @param newRows
	 *            the new number of rows
	 * @param columnMajor
	 *            if true, values are drawn and placed down columns first. if
	 *            false values are drawn and placed across rows first
	 * @return new matrix
	 */
	public static Matrix reshape(Matrix m, int newRows, boolean columnMajor) {
		final int oldCols = m.getColumnDimension();
		final int oldRows = m.getRowDimension();
		final int length = oldCols * m.getRowDimension();
		final int newCols = length / newRows;
		final Matrix mat = new Matrix(newRows, newCols);

		final double[][] m1v = m.getArray();
		final double[][] m2v = mat.getArray();

		int r1 = 0, r2 = 0, c1 = 0, c2 = 0;
		if (!columnMajor) {
			for (int i = 0; i < length; i++) {
				m2v[r2][c2] = m1v[r1][c1];

				c1++;
				if (c1 >= oldCols) {
					c1 = 0;
					r1++;
				}

				c2++;
				if (c2 >= newCols) {
					c2 = 0;
					r2++;
				}
			}
		}
		else {
			for (int i = 0; i < length; i++) {
				m2v[r2][c2] = m1v[r1][c1];

				r1++;
				if (r1 >= oldRows) {
					r1 = 0;
					c1++;
				}

				r2++;
				if (r2 >= newRows) {
					r2 = 0;
					c2++;
				}
			}
		}

		return mat;
	}

	/**
	 * Compute the sum of values in a single column
	 * 
	 * @param m
	 *            the matrix
	 * @param col
	 *            the column
	 * @return the sum of values in column col
	 */
	public static double sumColumn(Matrix m, int col) {
		final double[][] data = m.getArray();
		final int rows = m.getRowDimension();

		double sum = 0;
		for (int r = 0; r < rows; r++)
			sum += data[r][col];

		return sum;
	}

	/**
	 * Compute the sum of values in a single row
	 * 
	 * @param m
	 *            the matrix
	 * @param row
	 *            the row
	 * @return the sum of values in row row
	 */
	public static double sumRow(Matrix m, int row) {
		final double[][] data = m.getArray();
		final int cols = m.getColumnDimension();

		double sum = 0;
		for (int c = 0; c < cols; c++)
			sum += data[row][c];

		return sum;
	}

	/**
	 * Increment values in a single column by a constant
	 * 
	 * @param m
	 *            the matrix
	 * @param col
	 *            the column
	 * @param value
	 *            the constant
	 * @return the matrix
	 */
	public static Matrix incrColumn(Matrix m, int col, double value) {
		final double[][] data = m.getArray();
		final int rows = m.getRowDimension();

		for (int r = 0; r < rows; r++)
			data[r][col] += value;

		return m;
	}

	/**
	 * Increment values in a single column by a constant
	 * 
	 * @param m
	 *            the matrix
	 * @param row
	 *            the row
	 * @param value
	 *            the constant
	 * @return the sum of values in row row
	 */
	public static Matrix incrRow(Matrix m, int row, double value) {
		final double[][] data = m.getArray();
		final int cols = m.getColumnDimension();

		for (int c = 0; c < cols; c++)
			data[row][c] += value;

		return m;
	}

	/**
	 * round (using {@link Math#round(double)} each value of the matrix
	 * 
	 * @param times
	 * @return same matrix as handed in
	 */
	public static Matrix round(Matrix times) {
		final double[][] data = times.getArray();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = Math.round(data[i][j]);
			}
		}
		return times;
	}

	/**
	 * min(A,B) returns an array the same size as A and B with the smallest
	 * elements taken from A or B. The dimensions of A and B must match
	 * 
	 * @param A
	 * @param B
	 * @return new Matrix filled with min from A and B
	 */
	public static Matrix min(Matrix A, Matrix B) {
		final double[][] dataA = A.getArray();
		final double[][] dataB = B.getArray();
		final Matrix ret = A.copy();
		final double[][] dataRet = ret.getArray();
		for (int i = 0; i < dataA.length; i++) {
			for (int j = 0; j < dataB[i].length; j++) {
				dataRet[i][j] = Math.min(dataA[i][j], dataB[i][j]);
			}
		}
		return ret;
	}

	/**
	 * d to the power of each value in range. as with {@link #range} range is: -
	 * a single number (a) (0:1:a) - two numbers (a,b) (a:1:b) - three numbers
	 * (a,b,c) (a:b:c)
	 * 
	 * any other amount of range results in a {@link RuntimeException}
	 * 
	 * @param d
	 * @param range
	 * @return d to the power of each value in range
	 */
	public static Matrix rangePow(double d, double... range) {
		double start, end, delta;
		if (range.length == 1) {
			start = 0;
			end = range[0];
			delta = 1;
		} else if (range.length == 2) {
			start = range[0];
			end = range[1];
			delta = 1;
		}
		else if (range.length == 3) {
			start = range[0];
			end = range[2];
			delta = range[1];
		}
		else {
			throw new RuntimeException("Invalid range options selected");
		}
		final int l = (int) ((end - start + 1) / delta);
		final double[][] out = new double[1][l];
		for (int i = 0; i < l; i++) {
			out[0][i] = Math.pow(d, start + (i * delta));
		}
		return new Matrix(out);
	}

	/**
	 * range is: - a single number (a) (0:1:a) - two numbers (a,b) (a:1:b) -
	 * three numbers (a,b,c) (a:b:c)
	 * 
	 * @param range
	 * @return the range defined
	 */
	public static Matrix range(double... range) {
		double start, end, delta;
		if (range.length == 1) {
			start = 0;
			end = range[0];
			delta = 1;
		} else if (range.length == 2) {
			start = range[0];
			end = range[1];
			delta = 1;
		}
		else if (range.length == 3) {
			start = range[0];
			end = range[2];
			delta = range[1];
		}
		else {
			throw new RuntimeException("Invalid range options selected");
		}
		final int l = (int) Math.floor((end - start) / delta) + 1;
		final double[][] out = new double[1][l];
		for (int i = 0; i < l; i++) {
			out[0][i] = start + (i * delta);
		}
		return new Matrix(out);
	}

	/**
	 * range is: - a single number (a) (0:1:a) - two numbers (a,b) (a:1:b) -
	 * three numbers (a,b,c) (a:b:c)
	 * 
	 * @param range
	 * @return the range defined
	 */
	public static Matrix range(int... range) {
		int start, end, delta;
		if (range.length == 1) {
			start = 0;
			end = range[0];
			delta = 1;
		} else if (range.length == 2) {
			start = range[0];
			end = range[1];
			delta = 1;
		}
		else if (range.length == 3) {
			start = range[0];
			end = range[2];
			delta = range[1];
		}
		else {
			throw new RuntimeException("Invalid range options selected");
		}
		final int l = (int) Math.floor((end - start) / delta) + 1;
		final double[][] out = new double[1][l];
		for (int i = 0; i < l; i++) {
			out[0][i] = start + (i * delta);
		}
		return new Matrix(out);
	}

	/**
	 * Given two row vectors, construct the power set of rowvector combinations
	 * 
	 * @param A
	 * @param B
	 * @return a new matrix of size A.cols * B.cols
	 */
	public static Matrix ntuples(Matrix A, Matrix B) {
		final double[][] Adata = A.getArray();
		final double[][] Bdata = B.getArray();

		final double[][] out = new double[2][Adata[0].length * Bdata[0].length];
		int i = 0;
		for (final double a : Adata[0]) {
			for (final double b : Bdata[0]) {
				out[0][i] = a;
				out[1][i] = b;
				i++;
			}
		}
		return new Matrix(out);
	}

	/**
	 * Given a matrix, repeat the matrix over i rows and j columns
	 * 
	 * @param x
	 * @param i
	 * @param j
	 * @return repeated matrix
	 */
	public static Matrix repmat(Matrix x, int i, int j) {
		final double[][] xdata = x.getArray();
		final double[][] newmat = new double[xdata.length * i][xdata[0].length * j];
		for (int k = 0; k < newmat.length; k += xdata.length) {
			for (int l = 0; l < newmat[0].length; l += xdata[0].length) {
				int rowcopyindex = 0;
				for (final double[] ds : xdata) {
					System.arraycopy(ds, 0, newmat[k + rowcopyindex], l, xdata[0].length);
					rowcopyindex += 1;
				}
			}
		}
		return new Matrix(newmat);
	}

	/**
	 * horizontally stack all the matrices provided. i.e. ret = [x1 x2 x3 x4 ...
	 * xn]
	 * 
	 * @param x
	 * @return horizontally stacked
	 */
	public static Matrix hstack(Matrix... x) {
		final int height = x[0].getRowDimension();
		int width = 0;
		for (final Matrix matrix : x) {
			width += matrix.getColumnDimension();
		}
		final double[][] newmat = new double[height][width];
		int colindex = 0;
		for (final Matrix matrix : x) {
			final double[][] matdata = matrix.getArray();
			final int w = matrix.getColumnDimension();
			for (int i = 0; i < height; i++) {
				System.arraycopy(matdata[i], 0, newmat[i], colindex, w);
			}
			colindex += w;
		}
		return new Matrix(newmat);
	}

	/**
	 * Add the rows to the mat at rowIndex. Assumes MANY things with no checks:
	 * rows.rows == rowIndex.length mat.cols == rows.cols rowIndex.length <
	 * mat.rows for x in rowIndex: x < mat.rows && x >= 0 etc.
	 * 
	 * @param mat
	 * @param rows
	 * @param rowIndex
	 * @return the input matrix
	 */
	public static Matrix plusEqualsRow(Matrix mat, Matrix rows, int[] rowIndex) {
		final double[][] matdata = mat.getArray();
		final double[][] rowdata = rows.getArray();
		int i = 0;
		for (final int row : rowIndex) {
			for (int j = 0; j < rowdata[i].length; j++) {
				matdata[row][j] += rowdata[i][j];
			}
			i++;
		}
		return mat;
	}

	/**
	 * @param x
	 * @param val
	 * @return a new matrix for x < val
	 */
	public static Matrix lessThan(Matrix x, double val) {
		final Matrix retMat = x.copy();
		final double[][] data = x.getArray();
		final double[][] retdata = retMat.getArray();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				retdata[i][j] = data[i][j] < val ? 1 : 0;
			}
		}
		return retMat;
	}

	/**
	 * @param x
	 * @param val
	 * @return a new matrix for x > val
	 */
	public static Matrix greaterThan(Matrix x, double val) {
		final Matrix retMat = x.copy();
		final double[][] data = x.getArray();
		final double[][] retdata = retMat.getArray();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				retdata[i][j] = data[i][j] > val ? 1 : 0;
			}
		}
		return retMat;
	}

	/**
	 * @param x
	 * @return a new matrix for x1 && x2 && ... && xn where && means "!=0"
	 */
	public static Matrix and(Matrix... x) {
		final Matrix retMat = MatrixUtils.ones(x[0].getRowDimension(), x[0].getColumnDimension());
		final double[][] retdata = retMat.getArray();

		for (final Matrix matrix : x) {
			final double[][] data = matrix.getArray();
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					retdata[i][j] = (retdata[i][j] != 0 && data[i][j] != 0) ? 1 : 0;
				}
			}
		}
		return retMat;
	}

	/**
	 * @param rowDimension
	 * @param columnDimension
	 * @return matrix of dimensions filled with ones
	 */
	public static Matrix ones(int rowDimension, int columnDimension) {
		final Matrix ret = new Matrix(rowDimension, columnDimension);
		return plus(ret, 1);
	}

	/**
	 * @param x
	 * @return logical-and each column of x
	 */
	public static Matrix all(Matrix x) {
		final int cols = x.getColumnDimension();
		final int rows = x.getRowDimension();
		final Matrix ret = new Matrix(1, cols);
		final double[][] retdata = ret.getArray();
		final double[][] data = x.getArray();
		for (int i = 0; i < cols; i++) {
			boolean cool = true;
			for (int j = 0; j < rows; j++) {
				cool = data[j][i] != 0 && cool;
				if (!cool)
					break;
			}
			retdata[0][i] = cool ? 1 : 0;
		}
		return ret;
	}

	/**
	 * @param vals
	 * @return given vals, return the array indexes where vals != 0
	 */
	public static int[] valsToIndex(double[] vals) {
		int nindex = 0;
		for (final double d : vals) {
			nindex += d != 0 ? 1 : 0;
		}

		final int[] indexes = new int[nindex];
		nindex = 0;
		int i = 0;
		for (final double d : vals) {
			if (d != 0) {
				indexes[i] = nindex;
				i++;
			}
			nindex++;
		}
		return indexes;
	}

	/**
	 * for every value in x greater than val set toset
	 * 
	 * @param x
	 * @param val
	 * @param toset
	 * @return same matrix handed in
	 */
	public static Matrix greaterThanSet(Matrix x, int val, int toset) {
		final double[][] data = x.getArray();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = data[i][j] > val ? toset : data[i][j];
			}
		}
		return x;
	}

	/**
	 * for every value in x less than val set toset
	 * 
	 * @param x
	 * @param val
	 * @param toset
	 * @return same matrix handed in
	 */
	public static Matrix lessThanSet(Matrix x, int val, int toset) {
		final double[][] data = x.getArray();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = data[i][j] < val ? toset : data[i][j];
			}
		}
		return x;
	}

	/**
	 * Subtract the given row vector from every row of the given matrix,
	 * returning the result in a new matrix.
	 * 
	 * @param in
	 *            the matrix
	 * @param row
	 *            the row vector
	 * @return the resultant matrix
	 */
	public static Matrix minusRow(Matrix in, double[] row) {
		final Matrix out = in.copy();
		final double[][] outData = out.getArray();
		final int rows = out.getRowDimension();
		final int cols = out.getColumnDimension();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				outData[r][c] -= row[c];
			}
		}

		return out;
	}

	/**
	 * Subtract the given col vector (held as a Matrix) from every col of the
	 * given matrix, returning the result in a new matrix.
	 * 
	 * @param in
	 *            the matrix
	 * @param col
	 *            the col Matrix (Only the first column is used)
	 * @return the resultant matrix
	 */
	public static Matrix minusCol(Matrix in, Matrix col) {
		final Matrix out = in.copy();
		final double[][] outData = out.getArray();
		final int rows = out.getRowDimension();
		final int cols = out.getColumnDimension();
		final double[][] colArr = col.getArray();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				outData[r][c] -= colArr[r][0];
			}
		}

		return out;
	}

	/**
	 * Add a matrix to another inline.
	 * 
	 * @param result
	 *            the matrix to add to
	 * @param add
	 *            the matrix to add
	 * @return the result matrix
	 */
	public static Matrix plusEquals(Matrix result, Matrix add) {
		final int rows = result.getRowDimension();
		final int cols = result.getColumnDimension();

		final double[][] resultData = result.getArray();
		final double[][] addData = add.getArray();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				resultData[r][c] += addData[r][c];
			}
		}

		return result;
	}

	/**
	 * Multiply a matrix by a constant inplace, returning the matrix.
	 * 
	 * @param m
	 *            the matrix
	 * @param val
	 *            the value to multiply by
	 * @return the matrix
	 */
	public static Matrix times(Matrix m, double val) {
		final double[][] data = m.getArray();

		final int rows = m.getRowDimension();
		final int cols = m.getColumnDimension();

		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				data[r][c] *= val;

		return m;
	}

	/**
	 * Convert an mtj matrix into a 2d double array
	 * 
	 * @param mat
	 * @return a double array
	 */
	public static double[][] mtjToDoubleArray(no.uib.cipr.matrix.DenseMatrix mat) {
		final double[][] out = new double[mat.numRows()][mat.numColumns()];
		final double[] data = mat.getData();
		for (int r = 0; r < out.length; r++) {
			final double[] outr = out[r];
			for (int c = 0; c < out[0].length; c++) {
				outr[c] = data[r + c * out.length];
			}
		}
		return out;
	}

	/**
	 * Compute the sum of values in all rows
	 * 
	 * @param m
	 *            the matrix
	 * @return the sum of values across all cols in all rows
	 */
	public static Matrix sumRows(Matrix m) {
		final double[][] data = m.getArray();
		final int cols = m.getColumnDimension();
		final int rows = m.getRowDimension();

		final Matrix sum = new Matrix(rows, 1);
		final double[][] sumArr = sum.getArray();
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++)
			{
				sumArr[r][0] += data[r][c];
			}
		}

		return sum;
	}

	/**
	 * Compute the sum of values in all cols
	 * 
	 * @param m
	 *            the matrix
	 * @return the sum of values across all rows in all cols
	 */
	public static Matrix sumCols(Matrix m) {
		final double[][] data = m.getArray();
		final int cols = m.getColumnDimension();
		final int rows = m.getRowDimension();

		final Matrix sum = new Matrix(1, cols);
		final double[][] sumArr = sum.getArray();
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++)
			{
				sumArr[0][c] += data[r][c];
			}
		}

		return sum;
	}

	/**
	 * Generate a matrix with Gaussian distributed randoms
	 * 
	 * @param rows
	 *            the number of rows
	 * @param cols
	 *            the number of columns
	 * @return a matrix containing values drawn from a 0 mean 1.0 sdev gaussian
	 */
	public static Matrix randGaussian(int rows, int cols) {
		final Matrix m = new Matrix(rows, cols);
		final double[][] d = m.getArray();
		final Random r = new Random();
		for (int row = 0; row < d.length; row++) {
			for (int col = 0; col < d[row].length; col++) {
				d[row][col] = r.nextGaussian();
			}
		}
		return m;
	}

	/**
	 * Compute the sparsity (i.e. ratio of non-zero elements to matrix size) of
	 * the given matrix
	 * 
	 * @param matrix
	 *            the matrix
	 * @return the sparsity
	 */
	public static double sparsity(SparseMatrix matrix) {
		final double density = matrix.used() / ((double) matrix.rowCount() * (double) matrix.columnCount());
		return 1 - density;
	}

	/**
	 * Extract the diagonal component from the given matrix
	 * 
	 * @param cv
	 *            the matrix
	 * @return a new matrix with the diagonal values from the input matrix and
	 *         other values set to zero.
	 */
	public static Matrix diag(Matrix cv) {
		final Matrix d = new Matrix(cv.getRowDimension(), cv.getColumnDimension());

		for (int i = 0; i < Math.min(cv.getRowDimension(), cv.getColumnDimension()); i++)
			d.set(i, i, cv.get(i, i));

		return d;
	}

	/**
	 * Extract the diagonal component from the given matrix
	 * 
	 * @param cv
	 *            the matrix
	 * @return a new matrix with the diagonal values from the input matrix and
	 *         other values set to zero.
	 */
	public static double[] diagVector(Matrix cv) {
		final double[] d = new double[Math.min(cv.getRowDimension(), cv.getColumnDimension())];

		for (int i = 0; i < Math.min(cv.getRowDimension(), cv.getColumnDimension()); i++)
			d[i] = cv.get(i, i);

		return d;
	}

	/**
	 * Format a matrix as a single-line string suitable for using in matlab or
	 * octave
	 * 
	 * @param mat
	 *            the matrix to format
	 * @return the string
	 */
	public static String toMatlabString(Matrix mat) {
		return "[" + toString(mat).trim().replace("\n ", ";").replace(" ", ",") + "]";
	}

	/**
	 * Format a matrix as a single-line string suitable for using in python
	 * 
	 * @param mat
	 *            the matrix to format
	 * @return the string
	 */
	public static String toPythonString(Matrix mat) {
		return "[[" + toString(mat).trim().replace("\n ", "][").replace(" ", ",") + "]]";
	}

	/**
	 * @param mat
	 * @return trace of the matrix
	 */
	public static double trace(Matrix mat) {
		double sum = 0;
		for (int i = 0; i < mat.getRowDimension(); i++) {
			sum += mat.get(i, i);
		}
		return sum;
	}

	/**
	 * Solves the system <code>Ax = 0</code>, returning the vector x as an
	 * array. Internally computes the least-squares solution using the SVD of
	 * <code>A</code>.
	 * 
	 * @param A
	 *            the matrix describing the system
	 * @return the solution vector
	 */
	public static double[] solveHomogeneousSystem(Matrix A) {
		return solveHomogeneousSystem(new DenseMatrix(A.getArray()));
	}

	/**
	 * Solves the system <code>Ax = 0</code>, returning the vector x as an
	 * array. Internally computes the least-squares solution using the SVD of
	 * <code>A</code>.
	 * 
	 * @param A
	 *            the matrix describing the system
	 * @return the solution vector
	 */
	public static double[] solveHomogeneousSystem(double[][] A) {
		return solveHomogeneousSystem(new DenseMatrix(A));
	}

	/**
	 * Solves the system <code>Ax = 0</code>, returning the vector x as an
	 * array. Internally computes the least-squares solution using the SVD of
	 * <code>A</code>.
	 * 
	 * @param A
	 *            the matrix describing the system
	 * @return the solution vector
	 */
	public static double[] solveHomogeneousSystem(DenseMatrix A) {
		try {
			final SVD svd = SVD.factorize(A);

			final double[] x = new double[svd.getVt().numRows()];
			final int c = svd.getVt().numColumns() - 1;

			for (int i = 0; i < x.length; i++) {
				x[i] = svd.getVt().get(c, i);
			}

			return x;
		} catch (final NotConvergedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Format a matrix as a single-line string suitable for using in java
	 * 
	 * @param mat
	 *            the matrix to format
	 * @return the string
	 */
	public static String toJavaString(Matrix mat) {
		return "{" + toString(mat).trim().replaceAll("^", "{").replace("\n ", "},{").replace(" ", ",") + "}}";
	}

	/**
	 * Construct a matrix from a row-packed (i.e. row-by-row) vector of the
	 * data.
	 * 
	 * @param vector
	 *            the row-packed vector
	 * @param ncols
	 *            the number of columns
	 * @return the reconstructed matrix
	 */
	public static Matrix fromRowPacked(double[] vector, int ncols) {
		final int nrows = vector.length / ncols;

		final Matrix a = new Matrix(nrows, ncols);
		final double[][] ad = a.getArray();
		for (int r = 0, i = 0; r < nrows; r++)
			for (int c = 0; c < ncols; c++, i++)
				ad[r][c] = vector[i];

		return a;
	}

	/**
	 * Compute the covariance matrix of the given samples (assumed each sample
	 * is a row).
	 * 
	 * @param m
	 *            the samples matrix
	 * @return the covariance matrix
	 */
	public static Matrix covariance(Matrix m) {
		final int N = m.getRowDimension();
		return times(m.transpose().times(m), 1.0 / (N > 1 ? N - 1 : N));
	}

	/**
	 * For each element of X, sign(X) returns 1 if the element is greater than
	 * zero, 0 if it equals zero and -1 if it is less than zero.
	 * 
	 * @param m
	 *            the matrix
	 * @return the sign matrix
	 */
	public static Matrix sign(Matrix m) {
		final Matrix o = new Matrix(m.getRowDimension(), m.getColumnDimension());
		final double[][] md = m.getArray();
		final double[][] od = o.getArray();

		for (int r = 0; r < o.getRowDimension(); r++) {
			for (int c = 0; c < o.getColumnDimension(); c++) {
				if (md[r][c] > 0)
					od[r][c] = 1;
				if (md[r][c] < 0)
					od[r][c] = -1;
			}
		}

		return o;
	}

	/**
	 * Return a copy of the input matrix where every value is the exponential of
	 * the elements, e to the X.
	 * 
	 * @param m
	 *            the input matrix
	 * @return the exponential matrix
	 */
	public static Matrix exp(Matrix m) {
		final Matrix o = new Matrix(m.getRowDimension(), m.getColumnDimension());
		final double[][] md = m.getArray();
		final double[][] od = o.getArray();

		for (int r = 0; r < o.getRowDimension(); r++) {
			for (int c = 0; c < o.getColumnDimension(); c++) {
				od[r][c] = Math.exp(md[r][c]);
			}
		}

		return o;
	}

	/**
	 * Return a copy of the input matrix where every value is the hyerbolic
	 * tangent of the elements.
	 * 
	 * @param m
	 *            the input matrix
	 * @return the tanh matrix
	 */
	public static Matrix tanh(Matrix m) {
		final Matrix o = new Matrix(m.getRowDimension(), m.getColumnDimension());
		final double[][] md = m.getArray();
		final double[][] od = o.getArray();

		for (int r = 0; r < o.getRowDimension(); r++) {
			for (int c = 0; c < o.getColumnDimension(); c++) {
				od[r][c] = Math.tanh(md[r][c]);
			}
		}

		return o;
	}
}
