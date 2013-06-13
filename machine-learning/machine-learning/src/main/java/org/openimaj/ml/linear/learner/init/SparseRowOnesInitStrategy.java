package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;

import java.util.Random;

import org.openimaj.math.matrix.CFMatrixUtils;

public class SparseRowOnesInitStrategy implements InitStrategy {
	MatrixFactory<? extends Matrix> smf = MatrixFactory.getSparseDefault();
	private Random random;
	private double sparcity;

	public SparseRowOnesInitStrategy(double sparcity, Random random) {
		this.random = random;
		this.sparcity = sparcity;
	}

	@Override
	public Matrix init(int rows, int cols) {
		final Matrix ret = smf.createMatrix(rows, cols);
		final Vector oneRow = CFMatrixUtils.plusInplace(smf.createMatrix(1, cols), 1).getRow(0);
		for (int i = 0; i < rows; i++) {
			if (this.random.nextDouble() > sparcity) {
				ret.setRow(i, oneRow.clone());
			}
		}
		return ret;
	}

}
