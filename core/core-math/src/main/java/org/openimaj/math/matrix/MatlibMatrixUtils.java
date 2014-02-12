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
	public static double sparsity(SparseMatrix mat) {
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
	 * Y = A . Bt
	 * 
	 * @param A
	 * @param B
	 * @param Y
	 * @return Y
	 */
	public static <T extends Matrix> T dotProductTranspose(Matrix A, Matrix B, T Y) {
		final int mA = A.rowCount();
		final int nB = B.rowCount();

		for (int i = 0; i < mA; i++) {
			for (int j = 0; j < nB; j++) {
				final double dot = A.row(i).dot(B.row(j));
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
			System.out.println(row);
			row.timesEquals(s);
		}
		return A;
	}

	/**
	 * @param laplacian
	 * @return returns a dense jama matrix
	 */
	public static Jama.Matrix toJama(Matrix laplacian) {
		final Jama.Matrix ret = new Jama.Matrix(laplacian.asArray());
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
		final DenseMatrix mat = new DenseMatrix(sol.getRowDimension(), sol.getColumnDimension());
		for (int i = 0; i < mat.rowCount(); i++) {
			for (int j = 0; j < mat.columnCount(); j++) {
				mat.put(i, j, sol.get(i, j));
			}
		}
		return mat;
	}

	/**
	 * @param sol
	 * @return Dense matrix from a {@link Jama.Matrix}
	 */
	public static no.uib.cipr.matrix.Matrix toMTJ(Matrix sol) {
		no.uib.cipr.matrix.Matrix mat;
		if (sol instanceof SparseMatrix) {
			final FlexCompRowMatrix fmat = new FlexCompRowMatrix(sol.rowCount(), sol.columnCount());
			int i = 0;
			for (final Vector vec : sol.rows()) {

				final no.uib.cipr.matrix.sparse.SparseVector x = new no.uib.cipr.matrix.sparse.SparseVector(vec.size(),
						vec.used());
				for (final Entry ve : vec.entries()) {
					x.set(ve.index, ve.value);
				}
				fmat.setRow(i, x);
				i++;
			}
			mat = fmat;
		}
		else {
			mat = new no.uib.cipr.matrix.DenseMatrix(sol.rowCount(), sol.columnCount());
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
	public static <T extends Matrix> T subMatrix(T mat, int rowstart, int rowend, int colstart, int colend) {
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
	public static <T extends Matrix> T subMatrix(final T mat, TIntCollection rows, TIntCollection cols) {
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
	public static <T extends Matrix> T subMatrix(final T mat, TIntArrayList rows, final int colstart, final int colend) {
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
	 * @param newSeenMatrix
	 *            the matrix to write into
	 * @param row
	 *            the row to start inserting from
	 * @param col
	 *            the column to start inserting from
	 * @param current
	 *            the matrix to insert
	 */
	public static void setSubMatrix(Matrix newSeenMatrix, int row, int col, Matrix current) {
		for (int i = row; i < row + current.rowCount(); i++) {
			for (int j = col; j < col + current.columnCount(); j++) {
				newSeenMatrix.put(i, j, current.get(i - row, j - col));
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
		for (int i = 0; i < mat.columnCount(); i++) {
			for (int j = 0; j < mat.rowCount(); j++) {
				ret.put(i, j, mat.get(j, i));
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
				if (ent.value > thresh)
				{
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
	public static SparseDoubleArray sparseVectorToSparseArray(ch.akuhn.matrix.SparseVector row) {
		final SparseDoubleArray sda = new SparseBinSearchDoubleArray(row.size(), row.used(), row.keys(), row.values());
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
	 * @param v the value vector 
	 * @param d the check value
	 * @return for each item in the vector, returns 1 if the value is less than the check value
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

}
