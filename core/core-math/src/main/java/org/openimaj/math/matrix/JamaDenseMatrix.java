package org.openimaj.math.matrix;

import java.util.Arrays;

import Jama.Matrix;

/** Dense matrix wrapper for a JAMA matrix.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class JamaDenseMatrix extends ch.akuhn.matrix.Matrix {

    public Matrix matrix;

    public JamaDenseMatrix(Matrix matrix) {
        this.matrix = matrix;
    }
    
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

	public void fill(double constant) {
	    for (double[] row: matrix.getArray()) Arrays.fill(row, constant);
	}

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
