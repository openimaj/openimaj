package org.openimaj.ml.linear.evaluation;

import java.util.List;

import gov.sandia.cognition.math.matrix.Matrix;

import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.util.pair.Pair;

public abstract class BilinearEvaluator {
	protected BilinearSparseOnlineLearner learner;
	public void setLearner(BilinearSparseOnlineLearner learner){
		this.learner = learner;
	}
	public abstract double evaluate(List<Pair<Matrix>> testpairs);
}
