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

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Miscellaneous matrix operations.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MatrixUtils {
	/**
	 * Are any values NaN or Inf?
	 * 
	 * @param matrix
	 *            matrix to test
	 * @return true if any elements are NaN or Inf; false otherwise
	 */
	public static boolean anyNaNorInf(Matrix matrix) {
		for (double[] arrLine : matrix.getArray()) {
			for (double d : arrLine) {
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
			double curr = Math.abs(matrix.get(i, i));
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
			double curr = Math.abs(matrix.get(i, i));
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
		EigenvalueDecomposition evd = matrix.eig();
		Matrix v = evd.getV();
		Matrix d = evd.getD();

		// sqrt of cells of D and store in-place
		for (int r = 0; r < d.getRowDimension(); r++)
			for (int c = 0; c < d.getColumnDimension(); c++)
				d.set(r, c, Math.sqrt(d.get(r, c)));

		// Y = V*D/V
		// Y = V'.solve(V*D)'
		Matrix a = v.inverse();
		Matrix b = v.times(d).inverse();
		return a.solve(b).inverse();
	}

	/**
	 * Computes the Moore-Penrose pseudoinverse. This is just a convenience
	 * wrapper around {@link PseudoInverse#pseudoInverse(Matrix)}.
	 * @param matrix the matrix to invert.
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
		EigenvalueDecomposition evd = matrix.eig();
		Matrix v = evd.getV();
		Matrix d = evd.getD();

		// sqrt of cells of D and store in-place
		for (int r = 0; r < d.getRowDimension(); r++) {
			for (int c = 0; c < d.getColumnDimension(); c++) {
				if (d.get(r, c) > 0)
					d.set(r, c, 1 / Math.sqrt(d.get(r, c)));
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
		Matrix copy = mat.copy();
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
		double[][] a1 = m1.getArray();
		double[][] a2 = m2.getArray();

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
		Matrix copy = mat.copy();
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
		StringWriter matWriter = new StringWriter();
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
	 * @param m the matrix
	 * @return the Eigen vectors and values.
	 */
	public static EigenValueVectorPair symmetricEig2x2(Matrix m) {
		double a = m.get(0, 0);
		double b = m.get(0, 1);
		double c = b;
		double d = m.get(1, 1);

		double trace = a + d;
		double det = a * d - b * c;

		Matrix val = new Matrix(2, 2);
		double sqrtInner = (trace * trace / 4) - det;
		// FIXME: make it deal with imaginary numbers.
		if (sqrtInner < 0) {
			EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(), e.getV());
		}

		sqrtInner = Math.sqrt(sqrtInner);
		double firstEig = trace / 2 + sqrtInner;
		double secondEig = trace / 2 - sqrtInner;
		if (firstEig > secondEig) {
			double tmp = firstEig;
			firstEig = secondEig;
			secondEig = tmp;
		}

		val.set(0, 0, firstEig);
		val.set(1, 1, secondEig);

		Matrix vec = new Matrix(2, 2);

		double v1 = firstEig - a;
		double v2 = secondEig - a;
		double norm1 = Math.sqrt(v1 * v1 + b * b);
		double norm2 = Math.sqrt(b * b + v2 * v2);
		vec.set(0, 0, b / norm1);
		vec.set(0, 1, b / norm2);
		vec.set(1, 0, v1 / norm1);
		vec.set(1, 1, v2 / norm2);

		// To deal with rounding error
		vec.set(1, 0, vec.get(0, 1));

		EigenValueVectorPair ret = new EigenValueVectorPair(val, vec);
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
			EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(), e.getV());
		}
		/**
		 * A = 1 B = a + d C = ad - bc
		 * 
		 * x = ( - B (+/-) sqrt(B^2 - 4AC) )/ (2A)
		 */
		double a = m.get(0, 0);
		double b = m.get(0, 1);
		double c = m.get(1, 0);
		double d = m.get(1, 1);

		double trace = a + d;
		double det = a * d - b * c;

		Matrix val = new Matrix(2, 2);
		double sqrtInner = (trace * trace / 4) - det;
		// FIXME: make it deal with imaginary numbers.
		if (sqrtInner < 0) {
			EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(), e.getV());
		}

		sqrtInner = Math.sqrt(sqrtInner);
		double firstEig = trace / 2 + sqrtInner;
		double secondEig = trace / 2 - sqrtInner;
		if (firstEig > secondEig) {
			double tmp = firstEig;
			firstEig = secondEig;
			secondEig = tmp;
		}

		val.set(0, 0, firstEig);
		val.set(1, 1, secondEig);

		Matrix vec = new Matrix(2, 2);
		if (b == 0 && c == 0) {
			vec.set(0, 0, 1);
			vec.set(1, 1, 1);
		} else {
			if (c != 0) {
				double v1 = firstEig - d;
				double v2 = secondEig - d;
				double norm1 = Math.sqrt(v1 * v1 + c * c);
				double norm2 = Math.sqrt(c * c + v2 * v2);
				vec.set(0, 0, v1 / norm1);
				vec.set(0, 1, v2 / norm2);
				vec.set(1, 0, c / norm1);
				vec.set(1, 1, c / norm2);
			} else if (b != 0) {
				double v1 = firstEig - a;
				double v2 = secondEig - a;
				double norm1 = Math.sqrt(v1 * v1 + b * b);
				double norm2 = Math.sqrt(b * b + v2 * v2);
				vec.set(0, 0, b / norm1);
				vec.set(0, 1, b / norm2);
				vec.set(1, 0, v1 / norm1);
				vec.set(1, 1, v2 / norm2);
			}
		}

		EigenValueVectorPair ret = new EigenValueVectorPair(val, vec);
		return ret;
	}

	/**
	 * Construct a matrix from a 2D float array of data.
	 * 
	 * @param data the data.
	 * @return the matrix.
	 */
	public static Matrix matrixFromFloat(float[][] data) {
		Matrix out = new Matrix(data.length, data[0].length);
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
	 * @param m the matrix to reduce.
	 * @param rank the desired rank.
	 * @return the rank-reduced matrix.
	 */
	public static Matrix reduceRank(Matrix m, int rank) {
		if (rank > Math.min(m.getColumnDimension(), m.getRowDimension())) {
			return m;
		}

		no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(
				m.getArray());
		no.uib.cipr.matrix.SVD svd;
		try {
			svd = no.uib.cipr.matrix.SVD.factorize(mjtA);
		} catch (NotConvergedException e) {
			throw new RuntimeException(e);
		}

		DenseMatrix U = svd.getU();
		DenseMatrix Vt = svd.getVt();
		double[] svector = svd.getS();
		DenseMatrix S = new DenseMatrix(U.numColumns(), Vt.numRows());
		for (int i = 0; i < rank; i++)
			S.set(i, i, svector[i]);

		DenseMatrix C = new DenseMatrix(U.numRows(), S.numColumns());
		DenseMatrix out = new DenseMatrix(C.numRows(), Vt.numColumns());
		U.mult(S, C);
		C.mult(Vt, out);

		Matrix outFinal = convert(out);
		return outFinal;
	}

	/**
	 * Convert a {@link DenseMatrix} to a {@link Matrix}.
	 * 
	 * @param mjt {@link DenseMatrix} to convert
	 * @return converted matrix.
	 */
	public static Matrix convert(DenseMatrix mjt) {
		return convert(mjt, mjt.numRows(), mjt.numColumns());
	}

	/**
	 * Convert a {@link DenseMatrix} to a {@link Matrix}.
	 * 
	 * @param mjt {@link DenseMatrix} to convert
	 * @param nrows number of rows to copy
	 * @param ncols number of columns to copy
	 * @return converted matrix.
	 */
	public static Matrix convert(DenseMatrix mjt, int nrows, int ncols) {
		double[][] d = new double[nrows][ncols];

		double[] mjtd = mjt.getData();
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
	 * @param m the input matrix
	 * @return a copy with the column order reversed
	 */
	public static Matrix reverseColumns(Matrix m) {
		return reverseColumnsInplace(m.copy());
	}

	/**
	 * Reverse the column order of the input matrix inplace.
	 * 
	 * @param m the input matrix
	 * @return the input matrix
	 */
	public static Matrix reverseColumnsInplace(Matrix m) {
		final double[][] data = m.getArray();
		final int rows = data.length;
		final int cols = data[0].length;
		final int halfCols = cols / 2;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < halfCols; c++) {
				double tmp = data[r][c];
				data[r][c] = data[r][cols - c - 1];
				data[r][cols - c - 1] = tmp;
			}
		}

		return m;
	}

	/**
	 * Create a copy of a matrix with the rows in reverse order.
	 * 
	 * @param m the input matrix
	 * @return a copy with the row order reversed
	 */
	public static Matrix reverseRows(Matrix m) {
		return reverseRowsInplace(m.copy());
	}

	/**
	 * Reverse the row order of the input matrix inplace.
	 * 
	 * @param m the input matrix
	 * @return the input matrix
	 */
	public static Matrix reverseRowsInplace(Matrix m) {
		final double[][] data = m.getArray();
		final int rows = data.length;
		final int halfRows = rows / 2;

		for (int r = 0; r < halfRows; r++) {
			double[] tmp = data[r];
			data[r] = data[rows - r - 1];
			data[rows - r - 1] = tmp;
		}

		return m;
	}

	/**
	 * Create a diagonal matrix
	 * @param s length diagonal numbers
	 * @return new Matrix(s.length,s.length) s.t. diagonal element i,i = s[i]
	 */
	public static Matrix diag(double[] s) {
		Matrix r = new Matrix(s.length, s.length);
		for (int i = 0; i < s.length; i++) {
			r.set(i, i, s[i]);
		}
		return r;
	}
	
	/**
	 * Set the values of the elements in a single column
	 * to a constant value.
	 * @param m the matrix
	 * @param c the column 
	 * @param v the constant value
	 * @return the matrix
	 */
	public static Matrix setColumn(Matrix m, int c, double v) {
		final double[][] data = m.getArray();
		final int rows = m.getRowDimension();
		
		for (int r=0; r<rows; r++)
			data[r][c] = v;
		
		return m;
	}
	
	/**
	 * Set the values of the elements in a single column
	 * to a constant value.
	 * @param m the matrix
	 * @param r the row 
	 * @param v the constant value
	 * @return the matrix
	 */
	public static Matrix setRow(Matrix m, int r, double v) {
		final double[][] data = m.getArray();
		final int cols = m.getColumnDimension();
		
		for (int c=0; c<cols; c++)
			data[r][c] = v;
		
		return m;
	}
	
	/**
	 * Fill a matrix with a constant value.
	 * @param m the matrix
	 * @param v the constant value
	 * @return the matrix
	 */
	public static Matrix fill(Matrix m, double v) {
		final double[][] data = m.getArray();

		final int rows = m.getRowDimension();
		final int cols = m.getColumnDimension();
		
		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = v;
		
		return m;
	}

	/**
	 * Subtract a constant from all values
	 * @param m the matrix
	 * @param v the constant value
	 * @return the matrix
	 */
	public static Matrix minus(Matrix m, double v) {
		final double[][] data = m.getArray();

		final int rows = m.getRowDimension();
		final int cols = m.getColumnDimension();
		
		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] -= v;
		
		return m;
	}

	/**
	 * Add a constant to all values
	 * @param m the matrix
	 * @param v the constant value
	 * @return the matrix
	 */
	public static Matrix plus(Matrix m, double v) {
		final double[][] data = m.getArray();

		final int rows = m.getRowDimension();
		final int cols = m.getColumnDimension();
		
		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] += v;
		
		return m;
	}
	
	/**
	 * Get a reshaped copy of the input matrix
	 * @param m the matrix to reshape
	 * @param newRows the new number of rows
	 * @return new matrix
	 */
	public static Matrix reshape(Matrix m, int newRows) {
		final int oldCols = m.getColumnDimension();
		final int length = oldCols * m.getRowDimension();
		final int newCols = length / newRows;
		final Matrix mat = new Matrix(newRows, newCols);
	
		final double [][] m1v = m.getArray();
		final double [][] m2v = mat.getArray();
		
		int r1 = 0, r2 = 0, c1 = 0, c2 = 0;
		for (int i=0; i<length; i++) {
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
	 * @param m the matrix to reshape
	 * @param newRows the new number of rows
	 * @param columnMajor if true, values are drawn and placed down columns first. if false values are drawn and placed across rows first
	 * @return new matrix
	 */
	public static Matrix reshape(Matrix m, int newRows,boolean columnMajor) {
		final int oldCols = m.getColumnDimension();
		final int oldRows = m.getRowDimension();
		final int length = oldCols * m.getRowDimension();
		final int newCols = length / newRows;
		final Matrix mat = new Matrix(newRows, newCols);
	
		final double [][] m1v = m.getArray();
		final double [][] m2v = mat.getArray();
		
		int r1 = 0, r2 = 0, c1 = 0, c2 = 0;
		if(!columnMajor){
			for (int i=0; i<length; i++) {
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
		else{
			for (int i=0; i<length; i++) {
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
	 * @param m the matrix
	 * @param col the column
	 * @return the sum of values in column col
	 */
	public static double sumColumn(Matrix m, int col) {
		final double[][] data = m.getArray();
		final int rows = m.getRowDimension();
		
		double sum = 0;
		for (int r=0; r<rows; r++)
			sum += data[r][col];
		
		return sum;		
	}
	
	/**
	 * Compute the sum of values in a single row
	 * @param m the matrix
	 * @param row the row
	 * @return the sum of values in row row
	 */
	public static double sumRow(Matrix m, int row) {
		final double[][] data = m.getArray();
		final int cols = m.getColumnDimension();
		
		double sum = 0;
		for (int c=0; c<cols; c++)
			sum += data[row][c];
		
		return sum;		
	}
	
	/**
	 * Increment values in a single column by a constant
	 * @param m the matrix
	 * @param col the column
	 * @param value the constant
	 * @return the matrix
	 */
	public static Matrix incrColumn(Matrix m, int col, double value) {
		final double[][] data = m.getArray();
		final int rows = m.getRowDimension();
		
		for (int r=0; r<rows; r++)
			data[r][col] += value;
		
		return m;
	}
	
	/**
	 * Increment values in a single column by a constant
	 * @param m the matrix
	 * @param row the row
	 * @param value the constant
	 * @return the sum of values in row row
	 */
	public static Matrix incrRow(Matrix m, int row, double value) {
		final double[][] data = m.getArray();
		final int cols = m.getColumnDimension();
		
		for (int c=0; c<cols; c++)
			data[row][c] += value;
		
		return m;
	}

	/**
	 * round (using {@link Math#round(double)} each value of the matrix
	 * @param times
	 * @return same matrix as handed in
	 */
	public static Matrix round(Matrix times) {
		double[][] data = times.getArray();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = Math.round(data[i][j]);
			}
		}
		return times;
	}

	/**
	 *  min(A,B) returns an array the same size as A and B with the smallest elements taken from A or B. 
	 *  The dimensions of A and B must match
	 * @param A
	 * @param B
	 * @return new Matrix filled with min from A and B
	 */
	public static Matrix min(Matrix A, Matrix B) {
		double[][] dataA = A.getArray();
		double[][] dataB = B.getArray();
		Matrix ret = A.copy();
		double[][] dataRet = ret.getArray();
		for (int i = 0; i < dataA.length; i++) {
			for (int j = 0; j < dataB[i].length; j++) {
				dataRet[i][j] = Math.min(dataA[i][j], dataB[i][j]); 
			}
		}
		return ret;
	}

	/**
	 * d to the power of each value in range. 
	 * as with {@link #range} range is:
	 * - a single number (a) (0:1:a)
	 * - two numbers (a,b) (a:1:b)
	 * - three numbers (a,b,c) (a:b:c)
	 * 
	 * any other amount of range results in a {@link RuntimeException}
	 * @param d
	 * @param range
	 * @return d to the power of each value in range
	 */
	public static Matrix rangePow(double d, double ... range) {
		double start,end,delta;
		if(range.length == 1){
			start = 0;
			end = range[0];
			delta = 1;
		}else if(range.length == 2){
			start = range[0];
			end = range[1];
			delta = 1;
		}
		else if(range.length == 3){
			start = range[0];
			end = range[2];
			delta = range[1];
		}
		else{
			throw new RuntimeException("Invalid range options selected");
		}
		int l = (int) ((end-start+1)/delta);
		double[][] out = new double[1][l];
		for (int i = 0; i < l; i++) {
			out[0][i] = Math.pow(d, start + (i * delta));
		}
		return new Matrix(out);
	}
	
	/**
	 * range is:
	 * - a single number (a) (0:1:a)
	 * - two numbers (a,b) (a:1:b)
	 * - three numbers (a,b,c) (a:b:c)
	 * @param range
	 * @return the range defined
	 */
	public static Matrix range(double ... range) {
		double start,end,delta;
		if(range.length == 1){
			start = 0;
			end = range[0];
			delta = 1;
		}else if(range.length == 2){
			start = range[0];
			end = range[1];
			delta = 1;
		}
		else if(range.length == 3){
			start = range[0];
			end = range[2];
			delta = range[1];
		}
		else{
			throw new RuntimeException("Invalid range options selected");
		}
		int l = (int) Math.floor((end-start)/delta) + 1;
		double[][] out = new double[1][l];
		for (int i = 0; i < l; i++) {
			out[0][i] = start + (i * delta);
		}
		return new Matrix(out);
	}
	
	/**
	 * range is:
	 * - a single number (a) (0:1:a)
	 * - two numbers (a,b) (a:1:b)
	 * - three numbers (a,b,c) (a:b:c)
	 * @param range
	 * @return the range defined
	 */
	public static Matrix range(int ... range) {
		int start,end,delta;
		if(range.length == 1){
			start = 0;
			end = range[0];
			delta = 1;
		}else if(range.length == 2){
			start = range[0];
			end = range[1];
			delta = 1;
		}
		else if(range.length == 3){
			start = range[0];
			end = range[2];
			delta = range[1];
		}
		else{
			throw new RuntimeException("Invalid range options selected");
		}
		int l = (int) Math.floor((end-start)/delta) + 1;
		double[][] out = new double[1][l];
		for (int i = 0; i < l; i++) {
			out[0][i] = start + (i * delta);
		}
		return new Matrix(out);
	}

	/**
	 * Given two row vectors, construct the power set of rowvector combinations 
	 * @param A
	 * @param B
	 * @return a new matrix of size A.cols * B.cols
	 */
	public static Matrix ntuples(Matrix A, Matrix B) {
		double[][] Adata = A.getArray();
		double[][] Bdata = B.getArray();
		
		double[][] out = new double[2][Adata[0].length * Bdata[0].length];
		int i = 0;
		for (double a : Adata[0]) {
			for (double b : Bdata[0]) {
				out[0][i] = a;
				out[1][i] = b;
				i++;
			}
		}
		return new Matrix(out);
	}

	/**
	 * Given a matrix, repeat the matrix over i rows and j columns
	 * @param x
	 * @param i
	 * @param j
	 * @return repeated matrix
	 */
	public static Matrix repmat(Matrix x, int i, int j) {
		double[][] xdata = x.getArray();
		double[][] newmat = new double[xdata.length * i ][xdata[0].length * j];
		for (int k = 0; k < newmat.length; k+=xdata.length) {
			for (int l = 0; l < newmat[0].length; l+=xdata[0].length) {
				int rowcopyindex = 0;
				for (double[] ds : xdata) {
					System.arraycopy(ds, 0, newmat[k+rowcopyindex], l, xdata[0].length);
					rowcopyindex+=1;
				}
			}
		}
		return new Matrix(newmat);
	}

	/**
	 * horizontally stack all the matricies provided. i.e. ret = [x1 x2 x3 x4 ... xn]
	 * @param x
	 * @return horizontally stacked 
	 */
	public static Matrix hstack(Matrix ... x) {
		int height = x[0].getRowDimension();
		int width = 0;
		for (Matrix matrix : x) {
			width += matrix.getColumnDimension();
		}
		double[][] newmat = new double[height][width];
		int colindex = 0;
		for (Matrix matrix : x) {
			double[][] matdata = matrix.getArray();
			int w = matrix.getColumnDimension();
			for (int i = 0; i < height; i++) {
				System.arraycopy(matdata[i], 0, newmat[i], colindex, w);
			}
			colindex +=w;
		}
		return new Matrix(newmat);
	}
	
	
	/**
	 * Add the rows to the mat at rowIndex. Assumes MANY things with no checks:
	 * rows.rows == rowIndex.length
	 * mat.cols == rows.cols
	 * rowIndex.length < mat.rows
	 * for x in rowIndex: x < mat.rows && x >= 0 
	 * etc.
	 * 
	 * @param mat
	 * @param rows
	 * @param rowIndex
	 * @return the input matrix
	 */
	public static Matrix plusEqualsRow(Matrix mat, Matrix rows, int[] rowIndex) {
		double[][] matdata = mat.getArray();
		double[][] rowdata = rows.getArray();
		int i = 0;
		for (int row : rowIndex) {
			for (int j = 0; j < rowdata[i].length; j++) {
				matdata[row][j] += rowdata[i][j];
			}
			i ++;
		}
		return mat;
	}

	/**
	 * @param x
	 * @param val
	 * @return a new matrix for x < val
	 */
	public static Matrix lessThan(Matrix x, double val) {
		Matrix retMat = x.copy();
		double[][] data = x.getArray();
		double[][] retdata = retMat.getArray();
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
		Matrix retMat = x.copy();
		double[][] data = x.getArray();
		double[][] retdata = retMat.getArray();
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
	public static Matrix and(Matrix ... x) {
		Matrix retMat = MatrixUtils.ones(x[0].getRowDimension(),x[0].getColumnDimension());
		double[][] retdata = retMat.getArray();
		
		for (Matrix matrix : x) {
			double[][] data = matrix.getArray();
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
		Matrix ret = new Matrix(rowDimension,columnDimension);
		return plus(ret, 1);
	}

	/**
	 * @param x
	 * @return logical-and each column of x
	 */
	public static Matrix all(Matrix x) {
		int cols = x.getColumnDimension();
		int rows = x.getRowDimension();
		Matrix ret = new Matrix(1,cols);
		double[][] retdata = ret.getArray();
		double[][] data = x.getArray();
		for (int i = 0; i < cols; i++) {
			boolean cool = true;
			for (int j = 0; j < rows; j++) {
				cool = data[j][i] != 0 && cool;
				if(!cool) break;
			}
			retdata[0][i] = cool ? 1 : 0;
		}
		return ret;
	}
	
	/**
	 * @param vals
	 * @return given vals, return the array indexes where vals != 0 
	 */
	public static int[] valsToIndex(double[] vals){
		int nindex = 0;
		for (double d : vals) {
			nindex += d != 0 ? 1 : 0;
		}
		
		int[] indexes = new int[nindex];
		nindex = 0;
		int i = 0;
		for (double d : vals) {
			if(d != 0){
				indexes[i] = nindex;
				i++;
			}
			nindex++;
		}
		return indexes;
	}

	/**
	 * for every value in x greater than val set toset
	 * @param x
	 * @param val
	 * @param toset 
	 * @return same matrix handed in
	 */
	public static Matrix greaterThanSet(Matrix x, int val, int toset) {
		double[][] data = x.getArray();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = data[i][j] > val ? toset : data[i][j];
			}
		}
		return x;
	}
	
	/**
	 * for every value in x less than val set toset
	 * @param x
	 * @param val
	 * @param toset 
	 * @return same matrix handed in
	 */
	public static Matrix lessThanSet(Matrix x, int val, int toset) {
		double[][] data = x.getArray();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = data[i][j] < val ? toset : data[i][j];
			}
		}
		return x;
	}
	
	
}
