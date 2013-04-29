package org.openimaj.ml.linear.learner.init;

import java.util.Random;

import org.openimaj.ml.linear.learner.OnlineLearner;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

public class RandomInitStrategy implements InitStrategy{
	MatrixFactory<? extends Matrix> smf = MatrixFactory.getDenseDefault();
	private double min;
	private double max;
	private Random random;
	public RandomInitStrategy(double min,double max, Random random) {
		this.min = min;
		this.max = max;
		this.random = random;
	}
	@Override
	public Matrix init(int rows, int cols) {
		return smf.createUniformRandom(rows, cols, min, max, random);
	}
	

}
