package org.openimaj.experiment.validation;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.ResultAggregator;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.FixedSizeChunkPartitioner;

public class ValidationRunner {
	public interface Round<DATASET extends Dataset<?>, ANALYSIS_RESULT> {
		public abstract ANALYSIS_RESULT evaluate(DATASET training, DATASET validation);		
	}
	
	private ThreadPoolExecutor pool;

	public ValidationRunner() {
		this.pool = GlobalExecutorPool.getPool();
	}
	
	public ValidationRunner(int nThreads) {
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
	}
	
	public <DATASET extends Dataset<?>,
			ANALYSIS_RESULT,
			AGGREGATE_ANALYSIS_RESULT extends AnalysisResult
			>
		AGGREGATE_ANALYSIS_RESULT 
		run(
				final ResultAggregator<ANALYSIS_RESULT, AGGREGATE_ANALYSIS_RESULT> aggregator, 
				final Iterable<ValidationData<DATASET>> cvIterable, 
				final Round<DATASET, ANALYSIS_RESULT> round) 
	{
		Parallel.ForEach(new FixedSizeChunkPartitioner<ValidationData<DATASET>>(cvIterable, 1),
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
