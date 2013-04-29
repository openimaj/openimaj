package org.openimaj.ml.linear.learner.init;

import java.util.Random;

import org.openimaj.ml.linear.learner.OnlineLearner;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

public class SparseRandomInitStrategy implements InitStrategy{
	MatrixFactory<? extends Matrix> smf = MatrixFactory.getSparseDefault();
	private double min;
	private double max;
	private Random random;
	private double sparcity;
	public SparseRandomInitStrategy(double min,double max, double sparcity, Random random) {
		this.min = min;
		this.max = max;
		this.random = random;
		this.sparcity = sparcity;
	}
	@Override
	public Matrix init(int rows, int cols) {
		SparseMatrix rand = (SparseMatrix) smf.createUniformRandom(rows, cols, min, max, random);
		Matrix ret = smf.createMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if(this.random.nextDouble() > sparcity) ret.setElement(i, j, rand.getElement(i, j));
			}
		}
		return ret;
	}

}
