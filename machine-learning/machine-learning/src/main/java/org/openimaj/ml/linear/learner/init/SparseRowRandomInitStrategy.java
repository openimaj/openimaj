package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;

import java.util.Random;

public class SparseRowRandomInitStrategy implements InitStrategy {
	MatrixFactory<? extends Matrix> smf = MatrixFactory.getSparseDefault();
	private double min;
	private double max;
	private Random random;
	private double sparcity;

	public SparseRowRandomInitStrategy(double min, double max, double sparcity, Random random) {
		this.min = min;
		this.max = max;
		this.random = random;
		this.sparcity = sparcity;
	}

	@Override
	public Matrix init(int rows, int cols) {
		final SparseMatrix rand = (SparseMatrix) smf.createUniformRandom(rows, cols, min, max, random);
		final Matrix ret = smf.createMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			if (this.random.nextDouble() > sparcity) {
				ret.setRow(i, rand.getRow(i));
			}
		}
		return ret;
	}
}
