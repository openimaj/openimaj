package ch.akuhn.matrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.akuhn.matrix.Vector.Entry;

/**
 * Two-dimensional table of floating point numbers.
 * <P>
 * 
 * @author Adrian Kuhn
 * 
 */
public abstract class Matrix {

	private static final int MAX_PRINT = 100;

	/**
	 * Add to the value at the given row/column
	 * 
	 * @param row
	 * @param column
	 * @param value
	 * @return the new value
	 */
	public double add(int row, int column, double value) {
		return put(row, column, get(row, column) + value);
	}

	/**
	 * Get an {@link Iterable} over the rows
	 * 
	 * @return an {@link Iterable} over the rows
	 */
	public Iterable<Vector> rows() {
		return vecs(/* isRow */true);
	}

	private Iterable<Vector> vecs(final boolean isRow) {
		return new Iterable<Vector>() {
			@Override
			public Iterator<Vector> iterator() {
				return new Iterator<Vector>() {

					private int count = 0;

					@Override
					public boolean hasNext() {
						return count < (isRow ? rowCount() : columnCount());
					}

					@Override
					public Vector next() {
						if (!hasNext())
							throw new NoSuchElementException();
						return new Vec(count++, isRow);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/**
	 * Get an {@link Iterable} over the columns
	 * 
	 * @return an {@link Iterable} over the columns
	 */
	public Iterable<Vector> columns() {
		return vecs(/* isRow */false);
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	public abstract int columnCount();

	/**
	 * Get the density
	 * 
	 * @return the density
	 */
	public double density() {
		return (double) used() / elementCount();
	}

	/**
	 * @return the number of elements
	 */
	public int elementCount() {
		return rowCount() * columnCount();
	}

	/**
	 * @param row
	 * @param column
	 * @return the value at the given row and column
	 */
	public abstract double get(int row, int column);

	/**
	 * Set the value at the given row/column
	 * 
	 * @param row
	 * @param column
	 * @param value
	 * @return the value being set
	 */
	public abstract double put(int row, int column, double value);

	/**
	 * @return the number of rows
	 */
	public abstract int rowCount();

	/**
	 * @return the number of non-zero elements
	 */
	public abstract int used();

	/**
	 * I/O
	 * 
	 * @param appendable
	 * @throws IOException
	 * @see "http://tedlab.mit.edu/~dr/svdlibc/SVD_F_ST.html"
	 */
	public void storeSparseOn(Appendable appendable) throws IOException {
		// this stores the transposed matrix, but as we will transpose it again
		// when reading it, this can be done without loss of generality.
		appendable.append(this.columnCount() + " ");
		appendable.append(this.rowCount() + " ");
		appendable.append(this.used() + "\r");
		for (final Vector row : rows()) {
			appendable.append(row.used() + "\r");
			for (final Entry each : row.entries()) {
				appendable.append(each.index + " " + each.value + " ");
			}
			appendable.append("\r");
		}
	}

	/**
	 * Write to file
	 * 
	 * @param fname
	 *            filename
	 * @throws IOException
	 */
	public void storeSparseOn(String fname) throws IOException {
		final FileWriter fw = new FileWriter(new File(fname));
		storeSparseOn(fw);
		fw.close();
	}

	/**
	 * Get the given row as a vector
	 * 
	 * @param row
	 * @return the row
	 */
	public Vector row(int row) {
		return new Vec(row, /* isRow */true);
	}

	/**
	 * Get the given column as a vector
	 * 
	 * @param column
	 * @return the column
	 */
	public Vector column(int column) {
		return new Vec(column, /* isRow */false);
	}

	/**
	 * Get the matrix data as a 2D dense array
	 * 
	 * @return the array representation
	 */
	public double[][] asArray() {
		final double[][] result = new double[rowCount()][columnCount()];
		for (int x = 0; x < result.length; x++) {
			for (int y = 0; y < result[x].length; y++) {
				result[x][y] = get(x, y);
			}
		}
		return result;
	}

	/**
	 * Get the index of the given vector
	 * 
	 * @param vec
	 * @return the index
	 */
	public static int indexOf(Vector vec) {
		return ((Vec) vec).index0;
	}

	private class Vec extends Vector {

		int index0;
		private boolean isRow;

		Vec(int n, boolean isRow) {
			this.isRow = isRow;
			this.index0 = n;
		}

		@Override
		public int size() {
			return isRow ? columnCount() : rowCount();
		}

		@Override
		public double put(int index, double value) {
			return isRow ? Matrix.this.put(this.index0, index, value)
					: Matrix.this.put(index, this.index0, value);
		}

		@Override
		public double get(int index) {
			return isRow ? Matrix.this.get(this.index0, index)
					: Matrix.this.get(index, this.index0);
		}

		@Override
		public boolean equals(Vector v, double epsilon) {
			throw new Error("Not yet implemented");
		}

		@Override
		public Vector times(double scalar) {
			throw new Error("Not yet implemented");
		}

		@Override
		public Vector timesEquals(double scalar) {
			throw new Error("Not yet implemented");
		}
	}

	/**
	 * Returns <code>y = Ax</code>.
	 * 
	 * @param x
	 * @return the result
	 * 
	 */
	public Vector mult(Vector x) {
		assert x.size() == this.columnCount();
		final Vector y = Vector.dense(this.rowCount());
		int i = 0;
		for (final Vector row : rows())
			y.put(i++, row.dot(x));
		return y;
	}

	/**
	 * Returns <code>y = (A^T)x</code>.
	 * 
	 * @param x
	 * @return the result
	 */
	public Vector transposeMultiply(Vector x) {
		assert x.size() == this.rowCount();
		final Vector y = Vector.dense(this.columnCount());
		int i = 0;
		for (final Vector row : rows())
			row.scaleAndAddTo(x.get(i++), y);
		return y;
	}

	/**
	 * Returns <code>y = (A^T)Ax</code>.
	 * <P>
	 * Useful for doing singular decomposition using ARPACK's dsaupd routine.
	 * 
	 * @param x
	 * @return the result
	 */
	public Vector transposeNonTransposeMultiply(Vector x) {
		return this.transposeMultiply(this.mult(x));
	}

	/**
	 * Build a matrix from the given values (row-major)
	 * 
	 * @param n
	 * @param m
	 * @param values
	 * @return the matrix
	 */
	public static Matrix from(int n, int m, double... values) {
		assert n * m == values.length;
		final double[][] data = new double[n][];
		for (int i = 0; i < n; i++)
			data[i] = Arrays.copyOfRange(values, i * m, (i + 1) * m);
		return new DenseMatrix(data);
	}

	/**
	 * Create a zeroed dense matrix
	 * 
	 * @param n
	 * @param m
	 * @return the matrix
	 */
	public static Matrix dense(int n, int m) {
		return new DenseMatrix(n, m);
	}

	/**
	 * @return true of matrix is square; false otherwise
	 */
	public boolean isSquare() {
		return columnCount() == rowCount();
	}

	/**
	 * Get in col-major format
	 * 
	 * @return the data in column major format
	 */
	public double[] asColumnMajorArray() {
		final double[] data = new double[columnCount() * rowCount()];
		final int n = columnCount();
		int i = 0;
		for (final Vector row : rows()) {
			for (final Entry each : row.entries()) {
				data[i + each.index * n] = each.value;
			}
			i++;
		}
		return data;
	}

	/**
	 * Create a sparse matrix
	 * 
	 * @param n
	 * @param m
	 * @return new sparse matrix
	 */
	public static SparseMatrix sparse(int n, int m) {
		return new SparseMatrix(n, m);
	}

	/**
	 * @return max value in matrix
	 */
	public double max() {
		return Util.max(this.unwrap(), Double.NaN);
	}

	/**
	 * @return min value in matrix
	 */
	public double min() {
		return Util.min(this.unwrap(), Double.NaN);
	}

	/**
	 * @return mean value of matrix
	 */
	public double mean() {
		final double[][] values = unwrap();
		return Util.sum(values) / Util.count(values);
	}

	/**
	 * @return unwrapped matrix
	 */
	public double[][] unwrap() {
		throw new IllegalStateException("cannot unwrap instance of " + this.getClass().getSimpleName());
	}

	/**
	 * @return mean of each row
	 */
	public double[] rowwiseMean() {
		final double[] mean = new double[rowCount()];
		int i = 0;
		for (final Vector row : rows())
			mean[i++] = row.mean();
		return mean;
	}

	/**
	 * @return the histogram
	 */
	public int[] getHistogram() {
		return Util.getHistogram(this.unwrap(), 100);
	}

	/**
	 * @return an empty instance of this matrix type
	 */
	public Matrix newInstance() {
		return newInstance(rowCount(), columnCount());
	}

	/**
	 * @param rows
	 * @param cols
	 * @return an empty instance of this matrix type
	 */
	public abstract Matrix newInstance(int rows, int cols);

	@Override
	public String toString() {
		final Writer sw = new StringWriter();
		final PrintWriter writer = new PrintWriter(sw);
		writer.println("NRows = " + rowCount());
		writer.println("NCols = " + columnCount());
		final int maxPrint = Math.min(rowCount() * columnCount(), MAX_PRINT);
		int i;
		for (i = 0; i < maxPrint; i++) {
			final int row = i / columnCount();
			final int col = i - (row * columnCount());
			writer.printf("%d\t%d\t%2.5f\n", row, col, this.get(row, col));
		}
		if (i < rowCount() * columnCount() - 1) {
			writer.printf("...");
		}
		writer.flush();
		return sw.toString();
	}
}
