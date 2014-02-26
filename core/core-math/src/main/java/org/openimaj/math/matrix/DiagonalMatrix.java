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

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.Vector;

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
	 * New empty matrix with val down the diagonals
	 * @param rowcol
	 * @param val 
	 */
	public DiagonalMatrix(int rowcol, double val) {
		this.vals = new double[rowcol];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = val;
		}
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
	public Vector mult(Vector x) {
		double[] y = new double[this.columnCount()];
		for (int i = 0; i < y.length; i++) {
			y[i] = this.vals[i] * x.get(i);
		}
		return Vector.wrap(y);
	}
	
	@Override
	public Vector transposeMultiply(Vector x) {
		return mult(x);
	}
	
	@Override
	public Vector transposeNonTransposeMultiply(Vector x) {
		double[] y = new double[this.columnCount()];
		for (int i = 0; i < y.length; i++) {
			y[i] = this.vals[i] * this.vals[i] * x.get(i);
		}
		return Vector.wrap(y);
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
