package org.openimaj.math.matrix;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixEntry;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrix;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;

import java.util.Random;

import org.openimaj.time.Timer;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

/**
 * Perform a multithreaded matrix multiplication
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

	class MultiplicationOperation implements Operation<Integer>{

		@Override
		public void perform(Integer object) {
			int rowi = object / answerCols;
			int coli = object - (rowi * answerCols);
			double dot = 0;
			for (int i = 0; i < a[rowi].length; i++) {
				dot += a[rowi][i] * b[i][coli];
			}
			ThreadedMatrixMulti.this.setAnswerElement(rowi, coli, dot);
		}

	}

	/**
	 * @param a
	 * @param b
	 */
	public Matrix times(Matrix a, Matrix b){
		double[][] ad = fromMatrix(a);
		double[][] bd = fromMatrix(b);
		return this.times(ad, bd);
	}
	public Matrix times(double[][] a, double[][]b){
		this.a = a;
		this.b = b;
		this.answerCols = b[0].length;
		this.answerRows = a.length;

		if(this.answer != null){
			if(!(this.answer[0].length == answerCols && this.answer.length == answerRows)){
				this.answer = newAnswer(answerRows,answerCols);
			}
		}
		else{
			this.answer = newAnswer(answerRows,answerCols);
		}

		Parallel.forIndex(0, answerRows * answerCols, 1, new MultiplicationOperation());

		return DenseMatrixFactoryMTJ.INSTANCE.copyArray(this.answer);
	}

	private static double[][] fromMatrix(Matrix a) {
		double[][] ret = new double[a.getNumRows()][a.getNumColumns()];
		for (MatrixEntry ds : a) {
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

	public static void main(String[] args) {
		int numRows = 1000;
		int numColumns = 2000;
		int repeat = 1;
		DenseMatrix left = DenseMatrixFactoryMTJ.INSTANCE.createUniformRandom(numRows , numColumns , 0, 1, new Random(1));
		DenseMatrix right = DenseMatrixFactoryMTJ.INSTANCE.createUniformRandom(numColumns, numRows , 0, 1, new Random(1));
		double[][] leftd = fromMatrix(left);
		double[][] rightd = fromMatrix(right);
		ThreadedMatrixMulti tmm = new ThreadedMatrixMulti(numRows,numRows);

		Timer t = Timer.timer();
		double correctDur = 0,threadDur = 0;
		for (int i = 0; i < repeat; i++) {
			t.start();
			Matrix correct = left.times(right);
			correctDur += t.duration();
			t.start();
			Matrix thread = tmm.times(leftd, rightd);
			threadDur += t.duration();

		}
		System.out.println("Correct took: " + correctDur/repeat);
		System.out.println("Threaded took: " + threadDur/repeat);



	}

}
