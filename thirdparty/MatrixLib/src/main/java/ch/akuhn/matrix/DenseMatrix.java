package ch.akuhn.matrix;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A Dense matrix.
 * 
 * @author Adrian Kuhn
 * 
 */
public class DenseMatrix extends Matrix {

	protected double[][] values;

	/**
	 * Construct with the given values
	 * 
	 * @param values
	 *            the values
	 */
	public DenseMatrix(double[][] values) {
		this.values = values;
		this.assertInvariant();
	}

	protected void assertInvariant() throws IllegalArgumentException {
		if (values.length == 0)
			return;
		final int m = values[0].length;
		for (int n = 0; n < values.length; n++) {
			if (values[n].length != m)
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Construct with the given size
	 * 
	 * @param rows
	 * @param columns
	 */
	public DenseMatrix(int rows, int columns) {
		this.values = makeValues(rows, columns);
		this.assertInvariant();
	}

	protected double[][] makeValues(int rows, int columns) {
		return new double[rows][columns];
	}

	@Override
	public double add(int row, int column, double value) {
		return values[row][column] += value;
	}

	@Override
	public int columnCount() {
		return values[0].length;
	}

	@Override
	public double get(int row, int column) {
		return values[row][column];
	}

	@Override
	public double put(int row, int column, double value) {
		return values[row][column] = value;
	}
	
	@Override
	public Iterable<Vector> rows() {
		return new Iterable<Vector>() {
			
			@Override
			public Iterator<Vector> iterator() {
				return new Iterator<Vector>() {
					int i = 0;
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Vector next() {
						return new DenseVector(DenseMatrix.this.values[i++]);
					}
					
					@Override
					public boolean hasNext() {
						return i < DenseMatrix.this.values.length;
					}
				};
			}
		};
	}
	
	@Override
	public int rowCount() {
		return values.length;
	}

	@Override
	public int used() {
		// TODO Auto-generated method stub
		throw null;
	}

	@Override
	public double[][] unwrap() {
		return values;
	}

	/**
	 * Fill with a constant
	 * 
	 * @param constant
	 */
	public void fill(double constant) {
		for (final double[] row : values)
			Arrays.fill(row, constant);
	}

	/**
	 * Inline multiplication by a constant
	 * 
	 * @param d
	 */
	public void applyMultiplication(double d) {
		Util.times(values, d);
	}

	@Override
	public Matrix newInstance(int rows, int cols) {
		return new DenseMatrix(rows, cols);
	}

}
