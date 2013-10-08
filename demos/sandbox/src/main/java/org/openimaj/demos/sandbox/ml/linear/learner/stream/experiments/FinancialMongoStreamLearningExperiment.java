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
package org.openimaj.demos.sandbox.ml.linear.learner.stream.experiments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openimaj.demos.sandbox.ml.linear.learner.stream.IncrementalLearnerFunction;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.IncrementalLearnerWorldSelectingEvaluator;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.ModelStats;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.StockPriceAggregator;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.USMFStatusBagOfWords;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.USMFTickMongoDBQueryStream;
import org.openimaj.ml.linear.evaluation.SumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.init.FirstValueInitStrat;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.context.ContextFunctionAdaptor;
import org.openimaj.util.stream.window.WindowAverage;

import com.mongodb.ServerAddress;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FinancialMongoStreamLearningExperiment {
	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		BilinearLearnerParameters params = new BilinearLearnerParameters();
		params.put(BilinearLearnerParameters.ETA0_U, 0.02);
		params.put(BilinearLearnerParameters.ETA0_W, 0.02);
		params.put(BilinearLearnerParameters.LAMBDA, 0.001);
		params.put(BilinearLearnerParameters.BICONVEX_TOL, 0.01);
		params.put(BilinearLearnerParameters.BICONVEX_MAXITER, 10);
		params.put(BilinearLearnerParameters.BIAS, true);
		params.put(BilinearLearnerParameters.ETA0_BIAS, 0.5);
		params.put(BilinearLearnerParameters.WINITSTRAT, new SingleValueInitStrat(0.1));
		params.put(BilinearLearnerParameters.UINITSTRAT, new SparseZerosInitStrategy());
		params.put(BilinearLearnerParameters.EXPANDEDUINITSTRAT, new SparseZerosInitStrategy());
		params.put(BilinearLearnerParameters.EXPANDEDWINITSTRAT, new SingleValueInitStrat(0.05));
		FirstValueInitStrat biasInitStrat = new FirstValueInitStrat();
		params.put(BilinearLearnerParameters.BIASINITSTRAT, biasInitStrat);

		List<ServerAddress> serverList = Arrays.asList(
				new ServerAddress("rumi", 27017),
				new ServerAddress("hafez", 27017)
				);
		// Get the USMF status objects and financial ticks from the mongodb
		new USMFTickMongoDBQueryStream(serverList)
		// Transform the usmf status instances to bags of words
		.map(
			new ContextFunctionAdaptor<List<USMFStatus>, Map<String,Map<String,Double>>>(
				new USMFStatusBagOfWords(new StopwordMode()),"usmfstatuses",
				"bagofwords"
			)
		)
		// transform the financial ticks to the average tick
		.map(
			new ContextFunctionAdaptor<List<Map<String,Double>>,Map<String,Double>>(new WindowAverage(),"ticks","averageticks")
		)
		// Group together identical stock ticks
		.transform(new StockPriceAggregator(0.0001))
		// Train the model
		.map(
			new IncrementalLearnerWorldSelectingEvaluator(
				new SumLossEvaluator(),
				new IncrementalLearnerFunction(params)
			)
		)
		// Consume the model statistics
		.forEach(new Operation<Context>() {

			@Override
			public void perform(Context context) {
				ModelStats object = context.getTyped("modelstats");
				object.printSummary();
			}
		});

	}
}
