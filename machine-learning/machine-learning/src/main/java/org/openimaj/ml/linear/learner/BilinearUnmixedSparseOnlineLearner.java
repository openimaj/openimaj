package org.openimaj.ml.linear.learner;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import org.apache.log4j.Logger;
import org.openimaj.math.matrix.SandiaMatrixUtils;
import org.openimaj.ml.linear.learner.init.InitStrategy;
import org.openimaj.ml.linear.learner.loss.LossFunction;
import org.openimaj.ml.linear.learner.loss.MatLossFunction;
import org.openimaj.ml.linear.learner.regul.Regulariser;


/**
 * An implementation of a stochastic gradient decent with proximal perameter adjustment
 * (for regularised parameters).
 * 
 * Data is dealt with sequentially using a one pass implementation of the 
 * online proximal algorithm described in chapter 9 and 10 of:
 * The Geometry of Constrained Structured Prediction: Applications to Inference and
 * Learning of Natural Language Syntax, PhD, Andre T. Martins
 * 
 * This is a direct extention of the {@link BilinearSparseOnlineLearner} but instead of
 * a mixed update scheme (i.e. for a number of iterations W and U are updated synchronously)
 * we have an unmixed scheme where W is updated for a number of iterations, followed by
 * U for a number of iterations continuing as a whole for a number of iterations
 * 
 * The implementation does the following:
 * 	- When an X,Y is recieved:
 * 		- Update currently held batch
 * 		- If the batch is full:
 * 			- While There is a great deal of change in U and W:
 * 				While There is a great deal of change in W:
 * 					- Calculate the gradient of W holding U fixed
 * 					- Proximal update of W
 * 					- Calculate the gradient of Bias holding U and W fixed
 *				While There is a great deal of change in U:
 * 					- Calculate the gradient of U holding W fixed
 * 					- Proximal update of U
 * 					- Calculate the gradient of Bias holding U and W fixed
 * 			
 * 			- flush the batch
 * 		- return current U and W (same as last time is batch isn't filled yet)
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class BilinearUnmixedSparseOnlineLearner extends BilinearSparseOnlineLearner{
	
	static Logger logger = Logger.getLogger(BilinearUnmixedSparseOnlineLearner.class);
	
	@Override
	protected Matrix updateW(Matrix currentW, double wLossWeighted,double weightedLambda) {
		Matrix current = currentW;
		int iter = 0;
		Double biconvextol = this.params.getTyped(BilinearLearnerParameters.BICONVEX_TOL);
		Integer maxiter = this.params.getTyped(BilinearLearnerParameters.BICONVEX_MAXITER);
		while(true){
			Matrix newcurrent = super.updateW(current, wLossWeighted, weightedLambda);
			double sumchange = SandiaMatrixUtils.absSum(current.minus(newcurrent));
			double total = SandiaMatrixUtils.absSum(current);
			double ratio = sumchange/total;
			current = newcurrent;
			if(ratio < biconvextol || iter >= maxiter) {
				logger.debug("W tolerance reached after iteration: " + iter);
				break;
			}
			iter++;
		}
		return current;
	}
	
	protected Matrix updateU(Matrix currentU, double uLossWeighted,double weightedLambda) {
		Matrix current = currentU;
		int iter = 0;
		Double biconvextol = this.params.getTyped(BilinearLearnerParameters.BICONVEX_TOL);
		Integer maxiter = this.params.getTyped(BilinearLearnerParameters.BICONVEX_MAXITER);
		while(true){
			Matrix newcurrent = super.updateU(current, uLossWeighted, weightedLambda);
			double sumchange = SandiaMatrixUtils.absSum(current.minus(newcurrent));
			double total = SandiaMatrixUtils.absSum(current);
			double ratio = sumchange/total;
			current = newcurrent;
			if(ratio < biconvextol || iter >= maxiter) {
				logger.debug("U tolerance reached after iteration: " + iter);
				break;
			}
			iter++;
		}
		return current;
	}
}
