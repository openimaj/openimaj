package org.openimaj.ml.linear.learner.init;

import java.util.Random;

import org.openimaj.math.matrix.SandiaMatrixUtils;
import org.openimaj.ml.linear.learner.OnlineLearner;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;

public class SparseRowOnesInitStrategy implements InitStrategy{
	MatrixFactory<? extends Matrix> smf = MatrixFactory.getSparseDefault();
	private Random random;
	private double sparcity;
	public SparseRowOnesInitStrategy(double sparcity, Random random) {
		this.random = random;
		this.sparcity = sparcity;
	}
	@Override
	public Matrix init(int rows, int cols) {
		Matrix ret = smf.createMatrix(rows, cols);
		Vector oneRow = SandiaMatrixUtils.plusInplace(smf.createMatrix(1, cols),1).getRow(0);
		for (int i = 0; i < rows; i++) {
			if(this.random.nextDouble() > sparcity){
				ret.setRow(i, oneRow.clone());
			}
		}
		return ret;
	}

}
