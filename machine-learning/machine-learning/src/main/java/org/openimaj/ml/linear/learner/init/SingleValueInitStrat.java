package org.openimaj.ml.linear.learner.init;

import org.openimaj.math.matrix.SandiaMatrixUtils;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;

public class SingleValueInitStrat implements InitStrategy{

	private double val;
	
	public SingleValueInitStrat(double val) {
		this.val = val;
	}

	@Override
	public Matrix init(int rows, int cols) {
		return SandiaMatrixUtils.plusInplace(
			DenseMatrixFactoryMTJ.INSTANCE.createMatrix(rows, cols)
			, val
		);
	}

}
