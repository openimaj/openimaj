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
package org.openimaj.experiment.validation;

import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.data.dataset.Dataset;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.ResultAggregator;
import org.openimaj.experiment.validation.cross.CrossValidator;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.FixedSizeChunkPartitioner;

/**
 * Utility methods for performing validation and cross validation.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ValidationRunner {
	private ValidationRunner() {}
	
	/**
	 * Perform cross validation using the given cross validation scheme
	 * on the given data. The results of operation from each round
	 * are aggregated by the given results aggregator.
	 * <p>
	 * Rounds of the validation are performed in parallel using
	 * threads from the {@link GlobalExecutorPool}.
	 * 
	 * @param <DATASET> The type of the dataset
	 * @param <ANALYSIS_RESULT> The type of the analysis result from each round
	 * @param <AGGREGATE_ANALYSIS_RESULT> The type of the aggregated analysis result 
	 * @param aggregator the results aggregator
	 * @param dataset the dataset
	 * @param cv the cross-validation scheme
	 * @param round the operation to perform in each round
	 * @return the aggregated analysis result from all rounds
	 */
	public static <DATASET extends Dataset<?>,
			ANALYSIS_RESULT,
			AGGREGATE_ANALYSIS_RESULT extends AnalysisResult
			>
		AGGREGATE_ANALYSIS_RESULT 
		run(
				final ResultAggregator<ANALYSIS_RESULT, AGGREGATE_ANALYSIS_RESULT> aggregator, 
				final DATASET dataset,
				final CrossValidator<DATASET> cv, 
				final ValidationOperation<DATASET, ANALYSIS_RESULT> round) {
		return run(aggregator, dataset, cv, round, GlobalExecutorPool.getPool());
	}
	
	/**
	 * Perform cross validation using the given cross validation scheme
	 * on the given data. The results of operation from each round
	 * are aggregated by the given results aggregator.
	 * <p>
	 * Rounds of the validation can be performed in parallel, using
	 * the available threads in the given pool. 
	 * 
	 * @param <DATASET> The type of the dataset
	 * @param <ANALYSIS_RESULT> The type of the analysis result from each round
	 * @param <AGGREGATE_ANALYSIS_RESULT> The type of the aggregated analysis result 
	 * @param aggregator the results aggregator
	 * @param dataset the dataset
	 * @param cv the cross-validation scheme
	 * @param round the operation to perform in each round
	 * @param pool a thread-pool for parallel processing
	 * @return the aggregated analysis result from all rounds
	 */
	public static <DATASET extends Dataset<?>,
			ANALYSIS_RESULT,
			AGGREGATE_ANALYSIS_RESULT extends AnalysisResult
			>
		AGGREGATE_ANALYSIS_RESULT 
		run(
				final ResultAggregator<ANALYSIS_RESULT, AGGREGATE_ANALYSIS_RESULT> aggregator, 
				final DATASET dataset,
				final CrossValidator<DATASET> cv, 
				final ValidationOperation<DATASET, ANALYSIS_RESULT> round,
				ThreadPoolExecutor pool) 
	{
		Parallel.forEach(new FixedSizeChunkPartitioner<ValidationData<DATASET>>(cv.createIterable(dataset), 1),
				new Operation<ValidationData<DATASET>>() {
					@Override
					public void perform(ValidationData<DATASET> cv) {
						ANALYSIS_RESULT result = round.evaluate(cv.getTrainingDataset(), cv.getValidationDataset());
						synchronized (aggregator) {
							aggregator.add(result);
						}
					}
				}, 
				pool);
		
		return aggregator.getAggregatedResult();
	}
}
