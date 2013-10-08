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

import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

/**
 * Consumes Y and X instances for an {@link IncrementalBilinearSparseOnlineLearner} which are used
 * to measure loss of an underlying model, and then used to train the underlying model.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class IncrementalLearnerFunction implements Function<
		IndependentPair<
			Map<String,Map<String,Double>>,
			Map<String,Double>
		>,
		IncrementalBilinearSparseOnlineLearner> {

	final IncrementalBilinearSparseOnlineLearner learner;

	/**
	 * Constructs the underlying learner to train
	 */
	public IncrementalLearnerFunction() {
		this.learner = new IncrementalBilinearSparseOnlineLearner();
	}

	/**
	 * Feeds the parameters to a new learner to train
	 * @param params
	 */
	public IncrementalLearnerFunction(BilinearLearnerParameters params) {
		this.learner = new IncrementalBilinearSparseOnlineLearner(params);
	}
	/**
	 * Takes an existing learner and continues training it.
	 * @param learner
	 */
	public IncrementalLearnerFunction(IncrementalBilinearSparseOnlineLearner learner) {
		this.learner = learner;
	}



	@Override
	public IncrementalBilinearSparseOnlineLearner apply(IndependentPair<Map<String, Map<String, Double>>,Map<String, Double>> in)
	{
		learner.process(in.getFirstObject(),in.getSecondObject());
		System.out.printf("Learner has learnt %d words\n",learner.getVocabulary().size());
		System.out.printf("Learner has learnt %d users\n",learner.getUsers().size());
		return this.learner;
	}

}

