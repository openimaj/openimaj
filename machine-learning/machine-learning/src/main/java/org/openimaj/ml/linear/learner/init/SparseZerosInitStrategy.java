package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;

public class SparseZerosInitStrategy extends SingleValueInitStrat{

	public SparseZerosInitStrategy() {
		super(0f);
	}
	
	@Override
	public Matrix init(int rows, int cols) {
		return MatrixFactory.getSparseDefault().createMatrix(rows, cols);
	}

}
