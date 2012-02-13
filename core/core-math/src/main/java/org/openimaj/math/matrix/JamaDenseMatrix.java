package org.openimaj.math.matrix;

import java.util.Arrays;

import Jama.Matrix;

/** Dense matrix wrapper for a JAMA matrix.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class JamaDenseMatrix extends ch.akuhn.matrix.Matrix {
    /** The underlying matrix */
    public Matrix matrix;

    /**
     * Construct with matrix. The matrix is retained.
     * @param matrix The matrix. 
     */
    public JamaDenseMatrix(Matrix matrix) {
        this.matrix = matrix;
    }
    
    /**
     * Construct with 2d array of data.
     * @param values The data.
     */
    public JamaDenseMatrix(double[][] values) {
        this.matrix = new Matrix(values);
        this.assertInvariant();
    }
    
    protected void assertInvariant() throws IllegalArgumentException {
    	if (matrix.getArray().length == 0) return;
    	int m = matrix.getArray()[0].length;
		for (int n = 0; n < matrix.getArray().length; n++) {
			if (matrix.getArray()[n].length != m) throw new IllegalArgumentException();
		}
	}

	/**
	 * Construct with given dimensions.
	 * @param rows number of rows.
	 * @param columns number of columns.
	 */
	public JamaDenseMatrix(int rows, int columns) {
    	this.matrix = new Matrix(rows, columns);
    }

	@Override
    public double add(int row, int column, double value) {
        return matrix.getArray()[row][column] += value;
    }

    @Override
    public int columnCount() {
        return matrix.getColumnDimension();
    }

    @Override
    public double get(int row, int column) {
        return matrix.get(row, column);
    }

    @Override
    public double put(int row, int column, double value) {
        matrix.set(row, column, value);
        return value;
    }

    @Override
    public int rowCount() {
        return matrix.getRowDimension();
    }

    @Override
    public int used() {
        throw null;
    }

    @Override
    public double[][] unwrap() {
    	return matrix.getArray();
    }

	/**
	 * Fill the elements with a constant value.
	 * @param constant the value to set the elements to.
	 */
	public void fill(double constant) {
	    for (double[] row: matrix.getArray()) Arrays.fill(row, constant);
	}

	/**
	 * Multiply all elements by a constant.
	 * @param d the multiplication factor.
	 */
	public void applyMultiplication(double d) {
		matrix.timesEquals(d);
	}
	
	/**
	 * @return the wrapped JAMA matrix
	 */
	public Matrix getMatrix() {
		return matrix;
	}
}
