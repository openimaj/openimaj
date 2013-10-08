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
