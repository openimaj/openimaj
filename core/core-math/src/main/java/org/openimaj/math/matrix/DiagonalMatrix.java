package org.openimaj.math.matrix;

import ch.akuhn.matrix.Matrix;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DiagonalMatrix extends Matrix{

	private double[] vals;

	/**
	 * New empty matrix with 0s down the diagonals
	 * @param rowcol
	 */
	public DiagonalMatrix(int rowcol) {
		this.vals = new double[rowcol];
	}

	/**
	 * Diagonal version of the matrix handed
	 * @param arr
	 */
	public DiagonalMatrix(double[][] arr) {
		this(Math.min(arr.length, arr[0].length));
		for (int i = 0; i < vals.length; i++) {
			this.vals[i] = arr[i][i];
		}
	}

	/**
	 * @param mat
	 */
	public DiagonalMatrix(Matrix mat) {
		this(Math.min(mat.rowCount(), mat.columnCount()));
		for (int i = 0; i < vals.length; i++) {
			this.vals[i] = mat.get(i, i);
		}
	}

	@Override
	public int columnCount() {
		return this.vals.length;
	}

	@Override
	public double get(int row, int column) {
		if(row!=column) return 0;
		else return vals[row];
	}

	@Override
	public double put(int row, int column, double value) {
		if(row == column)
			return vals[row] = value;
		return 0;
	}

	@Override
	public int rowCount() {
		return vals.length;
	}

	@Override
	public int used() {
		return vals.length;
	}


	/**
	 * @param rowcol
	 * @return a matrix of ones in the diagonal
	 */
	public static DiagonalMatrix zeros(int rowcol) {
		return fill(rowcol,0.);
	}

	/**
	 * @param rowcol
	 * @return a matrix of ones in the diagonal
	 */
	public static DiagonalMatrix ones(int rowcol) {
		return fill(rowcol,1.);
	}

	/**
	 * @param rowcol
	 * @param d
	 * @return a matrix with d in the diagonal
	 */
	public static DiagonalMatrix fill(int rowcol, double d) {
		DiagonalMatrix ret = new DiagonalMatrix(rowcol);
		for (int i = 0; i < rowcol; i++) {
			ret.vals[i] = d;
		}
		return ret;
	}

	/**
	 * @return the diagonals
	 */
	public double[] getVals() {
		return this.vals;
	}

	@Override
	public Matrix newInstance(int rows, int cols) {
		return new DiagonalMatrix(rows);
	}
}
