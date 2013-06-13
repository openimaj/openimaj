package org.openimaj.ml.linear.evaluation;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;

import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.loss.LossFunction;
import org.openimaj.ml.linear.learner.loss.MatLossFunction;
import org.openimaj.util.pair.Pair;

public class SumLossEvaluator extends BilinearEvaluator {
	Logger logger = Logger.getLogger(SumLossEvaluator.class);

	@Override
	public double evaluate(List<Pair<Matrix>> data) {
		final Matrix u = learner.getU();
		final Matrix w = learner.getW();
		final Matrix bias = learner.getBias();
		final double sumloss = sumLoss(data, u, w, bias, learner.getParams());
		return sumloss;
	}

	public double sumLoss(List<Pair<Matrix>> pairs, Matrix u, Matrix w, Matrix bias, BilinearLearnerParameters params) {
		LossFunction loss = params.getTyped(BilinearLearnerParameters.LOSS);
		loss = new MatLossFunction(loss);
		double total = 0;
		int i = 0;
		for (final Pair<Matrix> pair : pairs) {
			final Matrix X = pair.firstObject();
			final Matrix Y = pair.secondObject();
			final SparseMatrix Yexp = BilinearSparseOnlineLearner.expandY(Y);
			final Matrix expectedAll = u.transpose().times(X.transpose()).times(w);
			loss.setY(Yexp);
			loss.setX(expectedAll);
			if (bias != null)
				loss.setBias(bias);
			logger.debug("Testing pair: " + i);
			total += loss.eval(null); // Assums an identity w.
			i++;
		}

		return total;
	}
}
