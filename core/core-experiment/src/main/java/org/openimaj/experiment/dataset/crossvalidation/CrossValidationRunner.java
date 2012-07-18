package org.openimaj.experiment.dataset.crossvalidation;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.ResultAggregator;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.FixedSizeChunkPartitioner;

public class CrossValidationRunner {
	public interface Round<DATASET extends Dataset<?>, ANALYSIS_RESULT> {
		public abstract ANALYSIS_RESULT evaluate(DATASET training, DATASET validation);		
	}
	
	private ThreadPoolExecutor pool;

	public CrossValidationRunner() {
		this.pool = GlobalExecutorPool.getPool();
	}
	
	public CrossValidationRunner(int nThreads) {
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
	}
	
	public <DATASET extends Dataset<?>,
			ANALYSIS_RESULT,
			AGGREGATE_ANALYSIS_RESULT extends AnalysisResult
			>
		AGGREGATE_ANALYSIS_RESULT 
		run(
				final ResultAggregator<ANALYSIS_RESULT, AGGREGATE_ANALYSIS_RESULT> aggregator, 
				final Iterable<CrossValidationData<DATASET>> cvIterable, 
				final Round<DATASET, ANALYSIS_RESULT> round) 
	{
		Parallel.ForEach(new FixedSizeChunkPartitioner<CrossValidationData<DATASET>>(cvIterable, 1),
				new Operation<CrossValidationData<DATASET>>() {
					@Override
					public void perform(CrossValidationData<DATASET> cv) {
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
