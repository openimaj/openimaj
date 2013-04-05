package org.openimaj.ml.linear.learner.init;

import java.util.Random;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

public class SparseOnesInitStrategy implements InitStrategy{
	MatrixFactory<? extends Matrix> smf = MatrixFactory.getSparseDefault();
	private Random random;
	private double sparcity;
	public SparseOnesInitStrategy(double sparcity, Random random) {
		this.random = random;
		this.sparcity = sparcity;
	}
	@Override
	public Matrix init(int rows, int cols) {
		Matrix ret = smf.createMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if(this.random.nextDouble() > sparcity) 
					ret.setElement(i, j, 1d);
			}
		}
		return ret;
	}

}
