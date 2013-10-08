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

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixEntry;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;

import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

/**
 * Perform a multithreaded matrix multiplication
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ThreadedMatrixMulti {
	private double[][] answer;
	private double[][] a;
	private double[][] b;
	private int answerCols;
	private int answerRows;

	public ThreadedMatrixMulti() {

	}

	public ThreadedMatrixMulti(int numRows, int numCols) {
		this.newAnswer(numRows, numCols);
	}

	class MultiplicationOperation implements Operation<Integer> {
		@Override
		public void perform(Integer object) {
			final int rowi = object / answerCols;
			final int coli = object - (rowi * answerCols);
			double dot = 0;
			for (int i = 0; i < a[rowi].length; i++) {
				dot += a[rowi][i] * b[i][coli];
			}
			ThreadedMatrixMulti.this.setAnswerElement(rowi, coli, dot);
		}
	}

	public Matrix times(Matrix a, Matrix b) {
		final double[][] ad = fromMatrix(a);
		final double[][] bd = fromMatrix(b);
		return this.times(ad, bd);
	}

	public Matrix times(double[][] a, double[][] b) {
		this.a = a;
		this.b = b;
		this.answerCols = b[0].length;
		this.answerRows = a.length;

		if (this.answer != null) {
			if (!(this.answer[0].length == answerCols && this.answer.length == answerRows)) {
				this.answer = newAnswer(answerRows, answerCols);
			}
		}
		else {
			this.answer = newAnswer(answerRows, answerCols);
		}

		Parallel.forIndex(0, answerRows * answerCols, 1, new MultiplicationOperation());

		return DenseMatrixFactoryMTJ.INSTANCE.copyArray(this.answer);
	}

	private static double[][] fromMatrix(Matrix a) {
		final double[][] ret = new double[a.getNumRows()][a.getNumColumns()];
		for (final MatrixEntry ds : a) {
			ret[ds.getRowIndex()][ds.getColumnIndex()] = ds.getValue();
		}
		return ret;
	}

	public void setAnswerElement(int rowi, int coli, double ans) {
		this.answer[rowi][coli] = ans;
	}

	private double[][] newAnswer(int answerRows, int answerCols) {
		return new double[answerRows][answerCols];
	}

	// public static void main(String[] args) {
	// final int numRows = 1000;
	// final int numColumns = 2000;
	// final int repeat = 1;
	// final DenseMatrix left =
	// DenseMatrixFactoryMTJ.INSTANCE.createUniformRandom(numRows, numColumns,
	// 0, 1,
	// new Random(1));
	// final DenseMatrix right =
	// DenseMatrixFactoryMTJ.INSTANCE.createUniformRandom(numColumns, numRows,
	// 0, 1,
	// new Random(1));
	// final double[][] leftd = fromMatrix(left);
	// final double[][] rightd = fromMatrix(right);
	// final ThreadedMatrixMulti tmm = new ThreadedMatrixMulti(numRows,
	// numRows);
	//
	// final Timer t = Timer.timer();
	// double correctDur = 0, threadDur = 0;
	// for (int i = 0; i < repeat; i++) {
	// t.start();
	// final Matrix correct = left.times(right);
	// correctDur += t.duration();
	// t.start();
	// final Matrix thread = tmm.times(leftd, rightd);
	// threadDur += t.duration();
	//
	// }
	// System.out.println("Correct took: " + correctDur / repeat);
	// System.out.println("Threaded took: " + threadDur / repeat);
	// }
}
