package org.openimaj.experiment.validation;

import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.ResultAggregator;
import org.openimaj.experiment.validation.cross.CrossValidator;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.parallel.Operation;
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
