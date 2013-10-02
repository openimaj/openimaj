package ch.akuhn.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import ch.akuhn.matrix.Vector.Entry;

/**
 * A sparse matrix
 * 
 * @author Adrian Kuhn
 */
public class SparseMatrix extends Matrix {

	private int columns;

	private List<Vector> rows;

	/**
	 * Construct with the given values
	 * 
	 * @param values
	 */
	public SparseMatrix(double[][] values) {
		this.columns = values[0].length;
		this.rows = new ArrayList<Vector>(values.length);
		for (final double[] each : values)
			addRow(each);
	}

	/**
	 * Construct with the given size
	 * 
	 * @param rows
	 * @param columns
	 */
	public SparseMatrix(int rows, int columns) {
		this.columns = columns;
		this.rows = new ArrayList<Vector>(rows);
		for (int times = 0; times < rows; times++)
			addRow();
	}

	@Override
	public double add(int row, int column, double sum) {
		return rows.get(row).add(column, sum);
	}

	/**
	 * Add a new column to the end, increasing the number of columns by 1
	 * 
	 * @return number of cols BEFORE new one was added
	 */
	public int addColumn() {
		columns++;
		for (final Vector each : rows)
			((SparseVector) each).resizeTo(columns);
		return columns - 1;
	}

	/**
	 * Add a new row to the end, increasing the number of rows by 1
	 * 
	 * @return number of rows BEFORE new one was added
	 */
	public int addRow() {
		rows.add(new SparseVector(columns));
		return rowCount() - 1;
	}

	protected int addRow(double[] values) {
		rows.add(new SparseVector(values));
		return rowCount() - 1;
	}

	/**
	 * Add the given values to the given row
	 * 
	 * @param row
	 * @param values
	 */
	public void addToRow(int row, Vector values) {
		final Vector v = rows.get(row);
		for (final Entry each : values.entries())
			v.add(each.index, each.value);
	}

	/**
	 * Convert to a dense 2d double array
	 * 
	 * @return 2d double array
	 */
	public double[][] asDenseDoubleDouble() {
		final double[][] dense = new double[rowCount()][columnCount()];

		for (int ri = 0; ri < rows.size(); ri++) {
			final Vector row = rows.get(ri);

			for (final Entry column : row.entries()) {
				dense[ri][column.index] = column.value;
			}
		}
		return dense;
	}

	@Override
	public int columnCount() {
		return columns;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SparseMatrix && rows.equals(((SparseMatrix) obj).rows);
	}

	@Override
	public double get(int row, int column) {
		return rows.get(row).get(column);
	}

	@Override
	public int hashCode() {
		return rows.hashCode();
	}

	@Override
	public double put(int row, int column, double value) {
		return rows.get(row).put(column, value);
	}

	@Override
	public Iterable<Vector> rows() {
		return Collections.unmodifiableCollection(rows);
	}

	@Override
	public Vector row(int row) {
		return this.rows.get(row);
	}

	@Override
	public int rowCount() {
		return rows.size();
	}

	/**
	 * Sets the row, no check is made on {@link SparseVector#size()} Use with
	 * care.
	 * 
	 * @param row
	 * @param values
	 */
	public void setRow(int row, SparseVector values) {
		rows.set(row, values);
	}

	@Override
	public int used() {
		int used = 0;
		for (final Vector each : rows)
			used += each.used();
		return used;
	}

	/**
	 * Trim each row
	 */
	public void trim() {
		for (final Vector each : rows) {
			((SparseVector) each).trim();
		}
	}

	/**
	 * Read matrix from {@link Scanner}
	 * 
	 * @param scan
	 * @return the matrix
	 */
	public static SparseMatrix readFrom(Scanner scan) {
		final int columns = scan.nextInt();
		final int rows = scan.nextInt();
		final int used = scan.nextInt();
		final SparseMatrix matrix = new SparseMatrix(rows, columns);
		for (int row = 0; row < rows; row++) {
			final int len = scan.nextInt();
			for (int i = 0; i < len; i++) {
				final int column = scan.nextInt();
				final double value = scan.nextDouble();
				matrix.put(row, column, value);
			}
		}
		assert matrix.used() == used;
		return matrix;
	}

	/**
	 * Create a random matrix
	 * 
	 * @param n
	 * @param m
	 * @param density
	 * @return the matrix
	 */
	public static SparseMatrix random(int n, int m, double density) {
		final Random random = new Random();
		final SparseMatrix A = new SparseMatrix(n, m);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (random.nextDouble() > density)
					continue;
				A.put(i, j, random.nextDouble());
			}
		}
		return A;
	}

	@Override
	public Vector mult(Vector dense) {
		assert dense.size() == this.columnCount();
		final double[] y = new double[this.rowCount()];
		final double[] x = ((DenseVector) dense).values;
		for (int i = 0; i < y.length; i++) {
			final SparseVector row = (SparseVector) rows.get(i);
			double sum = 0;
			for (int k = 0; k < row.used; k++) {
				sum += x[row.keys[k]] * row.values[k];
			}
			y[i] = sum;
		}
		return Vector.wrap(y);
	}

	@Override
	public Vector transposeMultiply(Vector dense) {
		assert dense.size() == this.rowCount();
		final double[] y = new double[this.columnCount()];
		final double[] x = ((DenseVector) dense).values;
		for (int i = 0; i < x.length; i++) {
			final SparseVector row = (SparseVector) rows.get(i);
			for (int k = 0; k < row.used; k++) {
				y[row.keys[k]] += x[i] * row.values[k];
			}
		}
		return Vector.wrap(y);
	}

	@Override
	public Matrix newInstance(int rows, int cols) {
		return new SparseMatrix(rows, cols);
	}
}
