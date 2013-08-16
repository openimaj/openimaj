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

import java.util.Arrays;

import Jama.Matrix;

/**
 * Dense matrix wrapper for a JAMA matrix.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class JamaDenseMatrix extends ch.akuhn.matrix.Matrix {
	/** The underlying matrix */
	public Matrix matrix;

	/**
	 * Construct with matrix. The matrix is retained.
	 * 
	 * @param matrix
	 *            The matrix.
	 */
	public JamaDenseMatrix(Matrix matrix) {
		this.matrix = matrix;
	}

	/**
	 * Construct with 2d array of data.
	 * 
	 * @param values
	 *            The data.
	 */
	public JamaDenseMatrix(double[][] values) {
		this.matrix = new Matrix(values);
		this.assertInvariant();
	}

	protected void assertInvariant() throws IllegalArgumentException {
		if (matrix.getArray().length == 0)
			return;
		final int m = matrix.getArray()[0].length;
		for (int n = 0; n < matrix.getArray().length; n++) {
			if (matrix.getArray()[n].length != m)
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Construct with given dimensions.
	 * 
	 * @param rows
	 *            number of rows.
	 * @param columns
	 *            number of columns.
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
	 * 
	 * @param constant
	 *            the value to set the elements to.
	 */
	public void fill(double constant) {
		for (final double[] row : matrix.getArray())
			Arrays.fill(row, constant);
	}

	/**
	 * Multiply all elements by a constant.
	 * 
	 * @param d
	 *            the multiplication factor.
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

	@Override
	public double[][] asArray() {
		return matrix.getArray();
	}

	@Override
	public ch.akuhn.matrix.Matrix newInstance(int rows, int cols) {
		return new JamaDenseMatrix(rows, cols);
	}
}
