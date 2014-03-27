/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
	protected Matrix updateU(Matrix currentU, Matrix neww, double uLossWeighted, double weightedLambda) {
		Matrix current = currentU;
		int iter = 0;
		final Double biconvextol = this.params.getTyped(BilinearLearnerParameters.BICONVEX_TOL);
		final Integer maxiter = this.params.getTyped(BilinearLearnerParameters.BICONVEX_MAXITER);
		while (true) {
			final Matrix newcurrent = super.updateU(current, neww, uLossWeighted, weightedLambda);
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
