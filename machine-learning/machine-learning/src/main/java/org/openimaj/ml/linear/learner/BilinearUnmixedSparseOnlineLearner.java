package org.openimaj.ml.linear.learner;

import gov.sandia.cognition.math.matrix.Matrix;

import org.apache.log4j.Logger;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.matrix.CFMatrixUtils;

/**
 * An implementation of a stochastic gradient decent with proximal parameter
 * adjustment (for regularised parameters).
 * <p>
 * Data is dealt with sequentially using a one pass implementation of the online
 * proximal algorithm described in chapter 9 and 10 of: The Geometry of
 * Constrained Structured Prediction: Applications to Inference and Learning of
 * Natural Language Syntax, PhD, Andre T. Martins
 * <p>
 * This is a direct extension of the {@link BilinearSparseOnlineLearner} but
 * instead of a mixed update scheme (i.e. for a number of iterations W and U are
 * updated synchronously) we have an unmixed scheme where W is updated for a
 * number of iterations, followed by U for a number of iterations continuing as
 * a whole for a number of iterations
 * <p>
 * The implementation does the following:
 * <ul>
 * <li>When an X,Y is received:
 * <ul>
 * <li>Update currently held batch
 * <li>If the batch is full:
 * <ul>
 * <li>While There is a great deal of change in U and W:
 * <ul>
 * <li>While There is a great deal of change in W:
 * <ul>
 * <li>Calculate the gradient of W holding U fixed
 * <li>Proximal update of W
 * <li>Calculate the gradient of Bias holding U and W fixed
 * </ul>
 * <li>While There is a great deal of change in U:
 * <ul>
 * <li>Calculate the gradient of U holding W fixed
 * <li>Proximal update of U
 * <li>Calculate the gradient of Bias holding U and W fixed
 * </ul>
 * </ul>
 * <li>flush the batch
 * </ul>
 * <li>return current U and W (same as last time is batch isn't filled yet)
 * </ul>
 * </ul>
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
@Reference(
		author = { "Andre F. T. Martins" },
		title = "The Geometry of Constrained Structured Prediction: Applications to Inference and Learning of Natural Language Syntax",
		type = ReferenceType.Phdthesis,
		year = "2012")
public class BilinearUnmixedSparseOnlineLearner extends BilinearSparseOnlineLearner {

	static Logger logger = Logger.getLogger(BilinearUnmixedSparseOnlineLearner.class);

	@Override
	protected Matrix updateW(Matrix currentW, double wLossWeighted, double weightedLambda) {
		Matrix current = currentW;
		int iter = 0;
		final Double biconvextol = this.params.getTyped(BilinearLearnerParameters.BICONVEX_TOL);
		final Integer maxiter = this.params.getTyped(BilinearLearnerParameters.BICONVEX_MAXITER);
		while (true) {
			final Matrix newcurrent = super.updateW(current, wLossWeighted, weightedLambda);
			final double sumchange = CFMatrixUtils.absSum(current.minus(newcurrent));
			final double total = CFMatrixUtils.absSum(current);
			final double ratio = sumchange / total;
			current = newcurrent;
			if (ratio < biconvextol || iter >= maxiter) {
				logger.debug("W tolerance reached after iteration: " + iter);
				break;
			}
			iter++;
		}
		return current;
	}

	@Override
	protected Matrix updateU(Matrix currentU, double uLossWeighted, double weightedLambda) {
		Matrix current = currentU;
		int iter = 0;
		final Double biconvextol = this.params.getTyped(BilinearLearnerParameters.BICONVEX_TOL);
		final Integer maxiter = this.params.getTyped(BilinearLearnerParameters.BICONVEX_MAXITER);
		while (true) {
			final Matrix newcurrent = super.updateU(current, uLossWeighted, weightedLambda);
			final double sumchange = CFMatrixUtils.absSum(current.minus(newcurrent));
			final double total = CFMatrixUtils.absSum(current);
			final double ratio = sumchange / total;
			current = newcurrent;
			if (ratio < biconvextol || iter >= maxiter) {
				logger.debug("U tolerance reached after iteration: " + iter);
				break;
			}
			iter++;
		}
		return current;
	}
}
