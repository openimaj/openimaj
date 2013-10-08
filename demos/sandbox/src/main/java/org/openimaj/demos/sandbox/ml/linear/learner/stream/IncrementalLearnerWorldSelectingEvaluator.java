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
package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.Map;

import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

/**
 * Given a new state from which to train an {@link IncrementalBilinearSparseOnlineLearner},
 * This function:
 * 	- evaluates an old learner on the new state,
 * 	- selects important words from the new learner
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public final class IncrementalLearnerWorldSelectingEvaluator
		implements
			Function<Context,Context>
{

	BilinearEvaluator eval;
	private IncrementalLearnerFunction func;
	private IncrementalBilinearSparseOnlineLearner learner ;


	/**
	 * The evaluation to apply before learning, the function to feed examples for learning
	 * @param eval
	 * @param func
	 */
	public IncrementalLearnerWorldSelectingEvaluator(BilinearEvaluator eval, IncrementalLearnerFunction func) {
		this.eval = eval;
		this.func = func;
		this.learner = null;
	}
	@Override
	public Context apply(Context in)
	{
		ModelStats modelStats = null;
		if(learner!=null){
			modelStats = new ModelStats(eval, learner, in);
		}
		Map<String,Map<String,Double>> x = in.getTyped("bagofwords");
		Map<String,Double> y = in.getTyped("averageticks");
		learner = func.apply(IndependentPair.pair(x, y));
		if(modelStats == null) modelStats = new ModelStats();

		in.put("modelstats",modelStats);
		return in;
	}

}