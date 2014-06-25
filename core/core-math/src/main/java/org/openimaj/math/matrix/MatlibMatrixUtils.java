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

import gnu.trove.TIntCollection;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntProcedure;
import gov.sandia.cognition.math.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.array.SparseBinSearchDoubleArray;
import org.openimaj.util.array.SparseDoubleArray;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.DenseVector;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.SparseVector;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.Vector.Entry;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Some helpful operations on {@link Matrix} instances from Adrian Kuhn's
 * library.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class MatlibMatrixUtils {
	private static final double EPS = 1e-8;

	/**
	 * Compute the matrix sparsity (i.e. proportion of zero elements)
	 * 
	 * @param mat
	 *            the matrix
	 * @return the sparsity
	 * @see SparseMatrix#density()
	 */
	public static double sparsity(Matrix mat) {
		return 1 - mat.density();
	}

	/**
	 * Raise each element to the power d, operating on the matrix itself
	 * 
	 * @param matrix
	 *            the matrix
	 * @param d
	 *            the power
	 * @return the input matrix, with elements raised to the given power
	 */
	public static <T extends Matrix> T powInplace(T matrix, double d) {
		int rowN = 0;
		for (final Vector ent : matrix.rows()) {
			for (final Entry row : ent.entries()) {
				matrix.put(rowN, row.index, Math.pow(row.value, d));
			}
			rowN++;
		}
		return matrix;
	}

	/**
	 * Left multiply two matrices: <code>R = D . A</code>
	 * 
	 * @param D
	 *            first matrix
	 * @param A
	 *            second matrix
	 * @return result of multiplication
	 */
	public static SparseMatrix times(DiagonalMatrix D, SparseMatrix A) {
		final SparseMatrix mat = new SparseMatrix(A.rowCount(), A.columnCount());
		final double[] Dvals = D.getVals();
		int rowIndex = 0;

		for (final Vector row : A.rows()) {
			for (final Entry ent : row.entries()) {
				mat.put(rowIndex, ent.index, ent.value * Dvals[rowIndex]);
			}
			rowIndex++;
		}

		return mat;
	}

	/**
	 * Right multiply two matrices: <code>R = A . D</code>
	 * 
	 * @param D
	 *            first matrix
	 * @param A
	 *            second matrix
	 * @return result of multiplication
	 */
	public static SparseMatrix times(SparseMatrix A, DiagonalMatrix D) {
		final SparseMatrix mat = new SparseMatrix(A.rowCount(), A.columnCount());
		int rowIndex = 0;
		final double[] Dvals = D.getVals();
		for (final Vector row : A.rows()) {
			for (final Entry ent : row.entries()) {
				mat.put(rowIndex, ent.index, ent.value * Dvals[ent.index]);
			}
			rowIndex++;
		}
		return mat;
	}

	/**
	 * Add two matrices, storing the results in the first:
	 * <code>A = A + B</code>
	 * 
	 * @param A
	 *            first matrix
	 * @param B
	 *            matrix to add
	 * @return A first matrix
	 */
	public static SparseMatrix plusInplace(SparseMatrix A, Matrix B) {
		for (int i = 0; i < A.rowCount(); i++) {
			A.addToRow(i, B.row(i));
		}
		return A;
	}

	/**
	 * Add two matrices, storing the results in the first:
	 * <code>A = A + B</code>
	 * 
	 * @param A
	 *            first matrix
	 * @param B
	 *            matrix to add
	 * @return A first matrix
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Matrix> T plusInplace(T A, Matrix B) {
		if (A instanceof SparseMatrix)
			return (T) plusInplace((SparseMatrix) A, B);
		for (int i = 0; i < A.rowCount(); i++) {
			final Vector brow = B.row(i);
			for (int j = 0; j < A.columnCount(); j++) {
				A.row(i).add(j, brow.get(j));
			}
		}
		return A;
	}

	/**
	 * Add a constant inplace <code>A = A + d</code>
	 * 
	 * @param A
	 *            first matrix
	 * @param d
	 *            the constant to add
	 * @return A first matrix
	 */
	public static <T extends Matrix> T plusInplace(T A, double d) {
		for (int i = 0; i < A.rowCount(); i++) {
			for (int j = 0; j < A.columnCount(); j++) {
				A.row(i).add(j, d);
			}
		}
		return A;
	}

	/**
	 * Subtract two matrices, storing the result in the second:
	 * <code>A = D - A</code>
	 * 
	 * @param D
	 *            first matrix
	 * @param A
	 *            second matrix
	 * @return second matrix
	 * 
	 */
	public static <T extends Matrix> T minusInplace(DiagonalMatrix D, T A) {
		final double[] Dval = D.getVals();
		for (int i = 0; i < Dval.length; i++) {
			final Iterable<Entry> rowents = A.row(i).entries();
			for (final Entry entry : rowents) {
				A.put(i, entry.index, -entry.value);
			}
			A.put(i, i, Dval[i] - A.get(i, i));
		}
		return A;
	}

	/**
	 * Subtract two matrices, storing the result in the first:
	 * <code>A = A - B</code>
	 * 
	 * @param A
	 *            first matrix
	 * @param B
	 *            second matrix
	 * @return first matrix
	 * 
	 */
	public static Matrix minusInplace(Matrix A, Matrix B) {
		for (int i = 0; i < A.rowCount(); i++) {
			final Iterable<Entry> rowents = A.row(i).entries();
			for (final Entry entry : rowents) {
				A.put(i, entry.index, entry.value - B.get(i, entry.index));
			}
		}
		return A;
	}

	/**
	 * Subtract a vector from another vector <code>A = A - D</code>
	 * 
	 * @param A
	 *            first matrix
	 * @param D
	 *            second matrix
	 * @return second matrix
	 * 
	 */
	public static <T extends Vector> T minusInplace(T A, Vector D) {
		for (int i = 0; i < A.size(); i++) {
			A.put(i, A.get(i) - D.get(i));
		}
		return A;
	}

	/**
	 * Subtract a vector from another vector <code>A = A + D</code>
	 * 
	 * @param A
	 *            first matrix
	 * @param D
	 *            second matrix
	 * @return second matrix
	 * 
	 */
	public static <T extends Vector> T plusInplace(T A, Vector D) {
		for (int i = 0; i < A.size(); i++) {
			A.put(i, A.get(i) + D.get(i));
		}
		return A;
	}

	/**
	 * Add two matrices, storing the results in the second:
	 * <code>A = D + A</code>
	 * 
	 * @param D
	 *            first matrix
	 * @param A
	 *            second matrix
	 * @return second matrix
	 * 
	 */
	public static <T extends Matrix> T plusInplace(DiagonalMatrix D, T A) {
		final double[] Dval = D.getVals();
		for (int i = 0; i < Dval.length; i++) {
			A.put(i, i, A.get(i, i) + Dval[i]);
		}
		return A;
	}

	/**
	 * Compute A^T . B
	 * 
	 * @param A
	 * @param B
	 * @return A^T . B
	 */
	public static Matrix transposeDotProduct(Matrix A, Matrix B) {
		final int mA = A.columnCount();
		final int nB = B.columnCount();
		final Matrix ret = A.newInstance(mA, nB);
		for (int i = 0; i < mA; i++) {
			final Vector column = A.column(i);
			for (int j = 0; j < nB; j++) {
				final double dot = column.dot(B.column(j));
				if (Math.abs(dot) > EPS)
					ret.put(i, j, dot);
			}
		}
		return ret;
	}

	/**
	 * Compute Y = A . B^T
	 * 
	 * @param A
	 * @param B
	 * @return Y
	 */
	public static Matrix dotProductTranspose(Matrix A, Matrix B) {
		final Matrix ret = A.newInstance(A.rowCount(), B.rowCount());
		return dotProductTranspose(A, B, ret);
	}

	/**
	 * Perform: A.T.dot(B.T) without performing the transpose. This is fine for
	 * dense matricies but is very inefficient for sparse matrices, consider
	 * performing the transpose manually.
	 * 
	 * @param A
	 * @param B
	 * @return A.T.dot(B.T)
	 */
	public static Matrix dotProductTransposeTranspose(Matrix A, Matrix B) {
		final int mA = A.columnCount();
		final int nB = B.rowCount();
		final Matrix ret = A.newInstance(mA, nB);
		for (int i = 0; i < mA; i++) {
			final Vector column = A.column(i);
			for (int j = 0; j < nB; j++) {
				final double dot = column.dot(B.row(j));
				if (Math.abs(dot) > EPS)
					ret.put(i, j, dot);
			}
		}
		return ret;
	}

	/**
	 * Y = A . Bt
	 * 
	 * @param A
	 * @param B
	 * @param Y
	 * @return Y
	 */
	public static <T extends Matrix> T dotProductTranspose(Matrix A, Matrix B,
			T Y)
	{
		if (A.columnCount() != B.columnCount())
			throw new RuntimeException(
					String.format("Matrix size mismatch, A.cols == %d and B.T.cols == %d", A.columnCount(),
							B.columnCount()));
		final int mA = A.rowCount();
		final int nB = B.rowCount();
		for (int i = 0; i < mA; i++) {
			final Vector arow = A.row(i);
			for (int j = 0; j < nB; j++) {
				final Vector brow = B.row(j);
				final double dot = arow.dot(brow);
				if (Math.abs(dot) > EPS)
					Y.put(i, j, dot);
			}
		}
		return Y;
	}

	/**
	 * A = A . s
	 * 
	 * @param A
	 * @param s
	 * @return A
	 */
	public static <T extends Matrix> T scaleInplace(T A, double s) {
		for (final Vector row : A.rows()) {
			row.timesEquals(s);
		}
		return A;
	}

	/**
	 * @param laplacian
	 * @return returns a dense jama matrix
	 */
	public static Jama.Matrix toJama(Matrix laplacian) {
		double[][] asArray = null;
		if (laplacian instanceof DenseMatrix) {
			asArray = ((DenseMatrix) laplacian).unwrap();
		} else {
			asArray = laplacian.asArray();
		}
		final Jama.Matrix ret = new Jama.Matrix(asArray, laplacian.rowCount(),
				laplacian.columnCount());
		return ret;
	}

	/**
	 * @param vector
	 * @return the vector as a column in a matrix
	 */
	public static Jama.Matrix toColJama(Vector vector) {
		final double[] vec = new double[vector.size()];
		vector.storeOn(vec, 0);
		final Jama.Matrix ret = new Jama.Matrix(vec.length, 1);
		for (int i = 0; i < vec.length; i++) {
			ret.set(i, 0, vec[i]);
		}

		return ret;
	}

	/**
	 * @param vector
	 * @return the vector as a row in a matrix
	 */
	public static Jama.Matrix toRowJama(Vector vector) {
		final double[] vec = new double[vector.size()];
		vector.storeOn(vec, 0);
		final Jama.Matrix ret = new Jama.Matrix(1, vec.length);
		for (int i = 0; i < vec.length; i++) {
			ret.set(0, i, vec[i]);
		}

		return ret;
	}

	/**
	 * @param sol
	 * @return Dense matrix from a {@link Jama.Matrix}
	 */
	public static Matrix fromJama(Jama.Matrix sol) {
		// final DenseMatrix mat = new DenseMatrix(sol.getRowDimension(),
		// sol.getColumnDimension());
		// for (int i = 0; i < mat.rowCount(); i++) {
		// for (int j = 0; j < mat.columnCount(); j++) {
		// mat.put(i, j, sol.get(i, j));
		// }
		// }
		final Matrix mat = new DenseMatrix(sol.getArray());
		return mat;
	}

	/**
	 * @param sol
	 * @return Dense matrix from a {@link Jama.Matrix}
	 */
	public static no.uib.cipr.matrix.Matrix toMTJ(Matrix sol) {
		no.uib.cipr.matrix.Matrix mat;
		if (sol instanceof SparseMatrix) {
			final FlexCompRowMatrix fmat = new FlexCompRowMatrix(
					sol.rowCount(), sol.columnCount());
			int i = 0;
			for (final Vector vec : sol.rows()) {

				final no.uib.cipr.matrix.sparse.SparseVector x = new no.uib.cipr.matrix.sparse.SparseVector(
						vec.size(), vec.used());
				for (final Entry ve : vec.entries()) {
					x.set(ve.index, ve.value);
				}
				fmat.setRow(i, x);
				i++;
			}
			mat = fmat;
		} else {
			mat = new no.uib.cipr.matrix.DenseMatrix(sol.rowCount(),
					sol.columnCount());
			for (int i = 0; i < sol.rowCount(); i++) {
				for (int j = 0; j < sol.columnCount(); j++) {
					mat.set(i, j, sol.get(i, j));
				}
			}
		}
		return mat;
	}

	/**
	 * Extract the submatrix of the same type of mat
	 * 
	 * @param mat
	 * @param rowstart
	 * @param rowend
	 * @param colstart
	 * @param colend
	 * @return new instance
	 */
	public static <T extends Matrix> T subMatrix(T mat, int rowstart,
			int rowend, int colstart, int colend)
	{
		@SuppressWarnings("unchecked")
		final T ret = (T) mat.newInstance(rowend - rowstart, colend - colstart);

		for (int i = 0; i < ret.rowCount(); i++) {
			final Vector row = mat.row(i + rowstart);
			for (final Entry ent : row.entries()) {
				if (ent.index >= colstart && ent.index < colend) {
					ret.put(i, ent.index - colstart, ent.value);
				}
			}
		}

		return ret;
	}

	/**
	 * Extract a submatrix from the given rows and cols
	 * 
	 * @param mat
	 *            the matrix to extract from
	 * @param rows
	 *            the rows to extract
	 * @param cols
	 *            the columns to extract
	 * @return the extracted matrix
	 */
	public static <T extends Matrix> T subMatrix(final T mat,
			TIntCollection rows, TIntCollection cols)
	{
		final TIntIntHashMap actualCols;
		if (!(cols instanceof TIntIntHashMap)) {
			actualCols = new TIntIntHashMap();
			cols.forEach(new TIntProcedure() {
				int seen = 0;

				@Override
				public boolean execute(int value) {
					actualCols.put(value, seen++);
					return true;
				}
			});
		} else {
			actualCols = (TIntIntHashMap) cols;
		}
		@SuppressWarnings("unchecked")
		final T ret = (T) mat.newInstance(rows.size(), cols.size());
		rows.forEach(new TIntProcedure() {
			int seenrows = 0;

			@Override
			public boolean execute(final int rowIndex) {
				final Vector row = mat.row(rowIndex);
				for (final Entry ent : row.entries()) {
					if (actualCols.contains(ent.index)) {
						ret.put(seenrows, actualCols.get(ent.index), ent.value);
					}
				}
				seenrows++;
				return true;
			}
		});

		return ret;
	}

	/**
	 * @param mat
	 * @param rows
	 * @param colstart
	 * @param colend
	 * @return the submatrix
	 */
	public static <T extends Matrix> T subMatrix(final T mat,
			TIntArrayList rows, final int colstart, final int colend)
	{
		@SuppressWarnings("unchecked")
		final T ret = (T) mat.newInstance(rows.size(), colend - colstart);
		rows.forEach(new TIntProcedure() {
			int seen = 0;

			@Override
			public boolean execute(int rowIndex) {
				final Vector row = mat.row(rowIndex);
				for (final Entry ent : row.entries()) {
					if (ent.index >= colstart && ent.index < colend) {
						ret.put(seen, ent.index - colstart, ent.value);
					}
				}
				seen++;
				return true;
			}
		});

		return ret;

	}

	/**
	 * @param mat
	 * @param rowstart
	 * @param rowend
	 * @param cols
	 * @return the submatrix
	 */
	public static <T extends Matrix> T subMatrix(final T mat,
			final int rowstart, final int rowend, TIntArrayList cols)
	{
		@SuppressWarnings("unchecked")
		final T ret = (T) mat.newInstance(rowend - rowstart, cols.size());
		cols.forEach(new TIntProcedure() {
			int seen = 0;

			@Override
			public boolean execute(int colIndex) {
				final Vector col = mat.column(colIndex);
				for (final Entry ent : col.entries()) {
					if (ent.index >= rowstart && ent.index < rowend) {
						ret.put(ent.index - rowstart, seen, ent.value);
					}
				}
				seen++;
				return true;
			}
		});

		return ret;

	}

	/**
	 * @param m
	 * @return a {@link MLDouble} for matlab
	 */
	public static MLDouble asMatlab(Matrix m) {
		final double[][] retArr = new double[m.rowCount()][m.columnCount()];
		for (int i = 0; i < retArr.length; i++) {
			for (int j = 0; j < retArr[i].length; j++) {
				retArr[i][j] = m.get(i, j);
			}
		}
		final MLDouble ret = new MLDouble("out", retArr);
		return ret;
	}

	/**
	 * Calculate all 3, used by {@link #min(Matrix)}, {@link #max(Matrix)} and
	 * {@link #mean(Matrix)}
	 * 
	 * @param mat
	 * @return the min, max and mean of the provided matrix
	 */
	public static double[] minmaxmean(Matrix mat) {
		double min = Double.MAX_VALUE, max = -Double.MAX_VALUE, mean = 0;
		final double size = mat.rowCount() * mat.columnCount();
		for (final Vector v : mat.rows()) {
			for (final Entry ent : v.entries()) {
				min = Math.min(min, ent.value);
				max = Math.max(max, ent.value);
				mean += ent.value / size;
			}
		}
		return new double[] { min, max, mean };
	}

	/**
	 * uses the first value returned by {@link #minmaxmean(Matrix)}
	 * 
	 * @param mat
	 * @return the min
	 */
	public static double min(Matrix mat) {
		return minmaxmean(mat)[0];
	}

	/**
	 * uses the second value returned by {@link #minmaxmean(Matrix)}
	 * 
	 * @param mat
	 * @return the min
	 */
	public static double max(Matrix mat) {
		return minmaxmean(mat)[1];
	}

	/**
	 * uses the third value returned by {@link #minmaxmean(Matrix)}
	 * 
	 * @param mat
	 * @return the min
	 */
	public static double mean(Matrix mat) {
		return minmaxmean(mat)[2];
	}

	/**
	 * @param l
	 * @param v
	 * @return performs l - v returning a matrix of type T
	 */
	public static <T extends Matrix> T minus(T l, double v) {
		@SuppressWarnings("unchecked")
		final T ret = (T) l.newInstance(l.rowCount(), l.columnCount());
		int r = 0;
		for (final Vector vec : l.rows()) {
			for (final Entry ent : vec.entries()) {
				ret.put(r, ent.index, ent.value - v);
			}
			r++;
		}
		return ret;
	}

	/**
	 * @param l
	 * @param v
	 * @return performs l - v returning a matrix of type T
	 */
	public static Vector minus(Vector l, Vector v) {
		final Vector ret = DenseVector.dense(l.size());
		for (int i = 0; i < l.size(); i++) {
			ret.put(i, l.get(i) - v.get(i));
		}
		return ret;
	}

	/**
	 * @param v
	 * @param l
	 * @return performs v - l returning a matrix of type T
	 */
	public static <T extends Matrix> T minus(double v, T l) {
		@SuppressWarnings("unchecked")
		final T ret = (T) l.newInstance(l.rowCount(), l.columnCount());
		for (int i = 0; i < l.rowCount(); i++) {
			for (int j = 0; j < l.columnCount(); j++) {
				ret.put(i, j, v - l.get(i, j));
			}
		}

		return ret;
	}

	/**
	 * Create a {@link Matrix} from a matlab {@link MLArray}
	 * 
	 * @param mlArray
	 *            the matlab array
	 * @return the matrix
	 */
	public static Matrix fromMatlab(MLArray mlArray) {
		final Matrix mat = new DenseMatrix(mlArray.getM(), mlArray.getN());
		final MLDouble mlDouble = (MLDouble) mlArray;
		for (int i = 0; i < mat.rowCount(); i++) {
			for (int j = 0; j < mat.columnCount(); j++) {
				mat.put(i, j, mlDouble.get(i, j));
			}
		}
		return mat;
	}

	/**
	 * Sum the diagonal of the given matrix
	 * 
	 * @param d
	 *            the matrix
	 * @return the sum along the diagonal
	 */
	public static double sum(DiagonalMatrix d) {
		return ArrayUtils.sumValues(d.getVals());
	}

	/**
	 * Set a submatrix of a larger matrix
	 * 
	 * @param to
	 *            the matrix to write into
	 * @param row
	 *            the row to start inserting from
	 * @param col
	 *            the column to start inserting from
	 * @param from
	 *            the matrix to insert
	 */
	public static void setSubMatrix(Matrix to, int row, int col, Matrix from) {
		for (int i = row; i < row + from.rowCount(); i++) {
			for (int j = col; j < col + from.columnCount(); j++) {
				to.put(i, j, from.get(i - row, j - col));
			}
		}
	}

	/**
	 * Transpose a matrix, returning a new matrix.
	 * 
	 * @param mat
	 *            the matrix to transpose
	 * @return the transposed matrix
	 */
	public static <T extends Matrix> T transpose(T mat) {
		@SuppressWarnings("unchecked")
		final T ret = (T) mat.newInstance(mat.columnCount(), mat.rowCount());
		// for (int i = 0; i < mat.columnCount(); i++) {
		// for (int j = 0; j < mat.rowCount(); j++) {
		// ret.put(i, j, mat.get(j, i));
		// }
		// }
		for (int i = 0; i < mat.rowCount(); i++) {
			final Vector v = mat.row(i);
			for (final Entry ent : v.entries()) {
				ret.put(ent.index, i, ent.value);
			}
		}

		return ret;
	}

	/**
	 * @param A
	 * @param B
	 * @return A = MAX(A,B)
	 */
	public static SparseMatrix maxInplace(SparseMatrix A, SparseMatrix B) {
		for (int i = 0; i < A.rowCount(); i++) {
			final Vector arow = A.row(i);
			final Vector brow = B.row(i);
			for (final Entry br : brow.entries()) {
				if (arow.get(br.index) < br.value) {
					A.put(i, br.index, br.value);
				}
			}
		}
		return A;
	}

	/**
	 * @param A
	 * @param B
	 * @return A = MIN(A,B)
	 */
	public static SparseMatrix minInplace(SparseMatrix A, SparseMatrix B) {
		for (int i = 0; i < A.rowCount(); i++) {
			final Vector arow = A.row(i);
			final Vector brow = B.row(i);
			for (final Entry br : brow.entries()) {
				if (arow.get(br.index) > br.value) {
					A.put(i, br.index, br.value);
				}
			}
		}
		return A;
	}

	/**
	 * @param A
	 * @param B
	 * @return A = A.*B
	 */
	public static SparseMatrix timesInplace(SparseMatrix A, SparseMatrix B) {
		for (int i = 0; i < A.rowCount(); i++) {
			final Vector arow = A.row(i);
			final Vector brow = B.row(i);
			for (final Entry br : brow.entries()) {
				A.put(i, br.index, br.value * arow.get(br.index));
			}

			for (final Entry ar : arow.entries()) {
				if (brow.get(ar.index) == 0) // All items in A not in B must be
												// set to 0
					A.put(i, ar.index, 0);
			}
		}
		return A;
	}

	/**
	 * @param A
	 * @param B
	 * @return A = A.*B
	 */
	public static Matrix timesInplace(Matrix A, Matrix B) {
		for (int i = 0; i < A.rowCount(); i++) {
			final Vector arow = A.row(i);
			final Vector brow = B.row(i);
			for (final Entry br : brow.entries()) {
				A.put(i, br.index, br.value * arow.get(br.index));
			}

			for (final Entry ar : arow.entries()) {
				if (brow.get(ar.index) == 0) // All items in A not in B must be
												// set to 0
					A.put(i, ar.index, 0);
			}
		}
		return A;
	}

	/**
	 * Copy a matrix
	 * 
	 * @param sparseMatrix
	 *            the matrix to copy
	 * @return the copy
	 */
	public static <T extends Matrix> T copy(T sparseMatrix) {
		@SuppressWarnings("unchecked")
		final T t = (T) sparseMatrix.newInstance();

		for (int r = 0; r < sparseMatrix.rowCount(); r++) {
			final Vector row = sparseMatrix.row(r);
			for (final Entry ent : row.entries()) {
				t.put(r, ent.index, ent.value);
			}
		}
		return t;
	}

	/**
	 * Set values below the given threshold to zero in the output matrix.
	 * 
	 * @param data
	 *            the input matrix
	 * @param thresh
	 *            the threshold
	 * @return a new matrix with values in the input matrix set to zero.
	 */
	public static SparseMatrix threshold(SparseMatrix data, double thresh) {
		final SparseMatrix newdata = (SparseMatrix) data.newInstance();

		for (int r = 0; r < data.rowCount(); r++) {
			final Vector vec = data.row(r);
			for (final Entry ent : vec.entries()) {
				if (ent.value > thresh) {
					newdata.put(r, ent.index, 1);
				}
			}
		}
		return newdata;
	}

	/**
	 * Convert a {@link ch.akuhn.matrix.SparseVector} to a
	 * {@link SparseDoubleArray}.
	 * 
	 * @param row
	 *            the vector to convert
	 * @return the converted vector
	 */
	public static SparseDoubleArray sparseVectorToSparseArray(
			ch.akuhn.matrix.SparseVector row)
	{
		final SparseDoubleArray sda = new SparseBinSearchDoubleArray(
				row.size(), row.used(), row.keys(), row.values());
		return sda;
	}

	/**
	 * 
	 * @param to
	 *            add items to this
	 * @param startindex
	 *            starting index in to
	 * @param from
	 *            add items from this
	 */
	public static void setSubVector(Vector to, int startindex, Vector from) {
		if (to instanceof DenseVector && from instanceof DenseVector) {
			final double[] tod = ((DenseVector) to).unwrap();
			final double[] fromd = ((DenseVector) from).unwrap();
			System.arraycopy(fromd, 0, tod, startindex, fromd.length);
			return;
		}
		for (int i = 0; i < from.size(); i++) {
			to.put(i + startindex, from.get(i));
		}
	}

	/**
	 * Starting from a given column of a row, set the values of a matrix to the
	 * values of v
	 * 
	 * @param to
	 * @param row
	 * @param col
	 * @param v
	 */
	public static void setSubMatrixRow(Matrix to, int row, int col, Vector v) {
		for (int i = col, j = 0; i < col + v.size(); i++, j++) {
			to.put(row, i, v.get(j));
		}
	}

	/**
	 * Starting from a given row of a column, set the values of a matrix to the
	 * values of v
	 * 
	 * @param to
	 * @param row
	 * @param col
	 * @param v
	 */
	public static void setSubMatrixCol(Matrix to, int row, int col, Vector v) {
		for (int i = row, j = 0; i < row + v.size(); i++, j++) {
			to.put(i, col, v.get(j));
		}
	}

	/**
	 * @param v
	 *            the value vector
	 * @param d
	 *            the check value
	 * @return for each item in the vector, returns 1 if the value is less than
	 *         the check value
	 */
	public static Vector lessThan(Vector v, double d) {
		final Vector out = new SparseVector(v.size(), 1);
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) < d)
				out.put(i, 1);
		}
		return out;
	}

	/**
	 * @param v
	 * @return any values of the vector are not-zero
	 */
	public static boolean any(Vector v) {
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) != 0)
				return true;
		}
		return false;
	}

	/**
	 * @param m
	 * @param col
	 * @return m with the added column
	 */
	public static <T extends Matrix> T appendColumn(T m, Vector col) {
		@SuppressWarnings("unchecked")
		final T ret = (T) m.newInstance(m.rowCount(), m.columnCount() + 1);
		setSubMatrixCol(ret, 0, m.columnCount(), col);
		return ret;
	}

	/**
	 * @param m
	 * @param row
	 * @return m with the added column
	 */
	public static <T extends Matrix> T appendRow(T m, Vector row) {
		@SuppressWarnings("unchecked")
		final T ret = (T) m.newInstance(m.rowCount() + 1, m.columnCount());
		setSubMatrixRow(ret, m.rowCount(), 0, row);
		return ret;
	}

	/**
	 * Create a {@link Matrix} from the Cognitive Foundry equivalent
	 * 
	 * @param init
	 *            the matrix
	 * @return the converted matrix
	 */
	public static Matrix fromCF(gov.sandia.cognition.math.matrix.Matrix init) {
		Matrix ret;
		final int m = init.getNumRows();
		final int n = init.getNumColumns();
		if (init instanceof gov.sandia.cognition.math.matrix.mtj.SparseMatrix) {
			ret = SparseMatrix.sparse(init.getNumRows(), init.getNumColumns());
		} else {
			ret = DenseMatrix.dense(m, n);
		}
		for (final MatrixEntry ent : init) {
			ret.put(ent.getRowIndex(), ent.getColumnIndex(), ent.getValue());
		}
		return ret;
	}

	/**
	 * Compute the dot product X.W
	 * 
	 * @param X
	 * @param W
	 * @return dot product
	 */
	public static Matrix dotProduct(Matrix X, Matrix W) {
		final Matrix ret = X.newInstance(X.rowCount(), W.columnCount());
		for (int j = 0; j < ret.columnCount(); j++) {
			final Vector column = W.column(j);
			for (int i = 0; i < ret.rowCount(); i++) {
				ret.put(i, j, X.row(i).dot(column));
			}
		}
		return ret;
	}

	/**
	 * Compute the 2-norm (Euclidean norm) of the vector
	 * 
	 * @param row
	 *            the vector
	 * @return the Euclidean norm
	 */
	public static double norm2(Vector row) {
		double norm = 0;
		for (final Entry e : row.entries())
			norm += e.value * e.value;
		return Math.sqrt(norm);
	}

	/**
	 * Subtract matrices A-B
	 * 
	 * @param A
	 * @param B
	 * @return A-B
	 */
	public static Matrix minus(Matrix A, Matrix B) {
		final Matrix ret = copy(A);
		minusInplace(ret, B);
		return ret;
	}

	/**
	 * Compute the Frobenius norm
	 * 
	 * @param A
	 *            the matrix
	 * @return the F norm
	 */
	public static double normF(Matrix A) {
		double scale = 0, ssq = 1;
		for (final Vector v : A.rows()) {
			for (final Entry e : v.entries()) {
				final double Aval = e.value;
				if (Aval != 0) {
					final double absxi = Math.abs(Aval);
					if (scale < absxi) {
						ssq = 1 + ssq * Math.pow(scale / absxi, 2);
						scale = absxi;
					} else {
						ssq = ssq + Math.pow(absxi / scale, 2);
					}
				}
			}
		}
		return scale * Math.sqrt(ssq);
	}

	/**
	 * Stack matrices vertically
	 * 
	 * @param matricies
	 *            matrices to stack
	 * @return matrix created from the stacking
	 */
	public static Matrix vstack(Matrix... matricies) {
		int nrows = 0;
		int ncols = 0;
		for (final Matrix matrix : matricies) {
			nrows += matrix.rowCount();
			ncols = matrix.columnCount();
		}
		final Matrix ret = matricies[0].newInstance(nrows, ncols);
		int currentRow = 0;
		for (final Matrix matrix : matricies) {
			setSubMatrix(ret, currentRow, 0, matrix);
			currentRow += matrix.rowCount();
		}
		return ret;
	}

}
